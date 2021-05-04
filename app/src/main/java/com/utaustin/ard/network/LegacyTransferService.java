package com.utaustin.ard.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.utaustin.ard.constants.Constants;
import com.utaustin.ard.util.PermissionsUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: REMOVE ME!!! See https://medium.com/@shoaibsaikat/using-android-channelclient-f5b4fd346374 for base logic
public class LegacyTransferService extends Service {
    private static final String DEBUG = Constants.DEBUG_OTS;

    // Unique name for audio data channel
    private static final String CHANNEL_MSG = "com.utaustin.ard.network.ots.audio";

    private Set<String> connectedDevices;

    private File[] transferFiles;

    // Alarm Manager
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    public static final long ALARM_INTERVAL = 1000 * 60 * 10;

    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG, "File upload service -> Offline File Transfer");

        setConnectedDeviceList();
//        Wearable.getCapabilityClient(getApplicationContext()).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
//        Task<ChannelClient.Channel> outChannelTask = Wearable.getChannelClient(getApplicationContext()).openChannel(node, )

        if(PermissionsUtils.isAllPermissionGranted(getApplicationContext())) {
            // TODO: Add conditional offline/online functionality

            // Initialize transfer client and recipient
            Log.d(DEBUG, "Client prepared for transfer.");
            File outputSummaryDir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.ROOT_DIR + File.separator + Constants.OUTPUT_SUMMARY_DIR);
            if (outputSummaryDir.exists()) {
                Log.d(DEBUG, "Output directory exists -> prepared for transfer.");

                transferFiles = outputSummaryDir.listFiles();
                Log.d(DEBUG, "Transfer size: " + transferFiles.length + " files.");

                for (File file : transferFiles) {
                    if (file.getName().endsWith(".mp3")) {
                        // TODO: Transfer audio data to connected device offline
                    } else if (file.getName().endsWith(".csv")) {
                        // TODO: Add feature extraction and transmit summarized audio data
                        continue;
                    }
                }
            } else {
                PermissionsUtils.writeSharedPreference(getApplicationContext(), Constants.UPLOAD_STATE, Constants.OFF);
            }
        }

        stopSelf();
    }

    public void onDestroy() {
        super.onDestroy();
        scheduleNextOfflineTransferService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Schedule the next offline transfer to occur
    private void scheduleNextOfflineTransferService() {
        // TODO: Tweak to accommodate our experiment
        // Repeat the recording services every 3min (It will vary according to test results)
        alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent upload_Service = new Intent(getApplicationContext(), LegacyTransferService.class);
        pendingIntent = PendingIntent.getService(getApplicationContext(), 1, upload_Service, PendingIntent.FLAG_UPDATE_CURRENT);

         // Alarm set repeat is not exact and can have significant drift
        if(Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ALARM_INTERVAL, pendingIntent);
            Log.d("Tiles", "Schedule OpenSMILE Alarm Service");
        }
        else if(Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + ALARM_INTERVAL, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + ALARM_INTERVAL, pendingIntent);
        }

    }

    // Set connected list of devices for application
    private void setConnectedDeviceList() {
        connectedDevices = new HashSet<>();

        Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
        try {
            List<Node> nodes = Tasks.await(nodeListTask);
            for(Node n : nodes) {
                connectedDevices.add(n.getId());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

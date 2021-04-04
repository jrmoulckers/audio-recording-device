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
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.utaustin.ard.constants.Constants;
import com.utaustin.ard.util.Permissions;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: REMOVE ME!!! See https://github.com/android/wear-os-samples/blob/main/DataLayer/Application/src/main/java/com/example/android/wearable/datalayer/MainActivity.java for base logic
public class OfflineTransferActivity extends WearableActivity implements CapabilityClient.OnCapabilityChangedListener {
    private final static String DEBUG = Constants.DEBUG_OTA;
    private CustomRecyclerAdapter mCustomRecyclerAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getCapabilityClient(this).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(DEBUG, "onCapabilityChanged: " + capabilityInfo);
        mCustomRecyclerAdapter.appendToDataEventLog(
                "onCapabilityChanged", capabilityInfo.toString());
    }
}

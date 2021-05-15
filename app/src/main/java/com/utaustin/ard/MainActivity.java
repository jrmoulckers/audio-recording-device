package com.utaustin.ard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.wear.ambient.AmbientModeSupport;

import com.utaustin.ard.constants.Constants;
import com.utaustin.ard.services.VADService;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = Constants.DEBUG_MAIN;

    private Context context = null;

    // UI Components
    private Button btnRecord, btnStop, btnInd;
    private EditText etFilenameInput;


    // Backend vars
    private AmbientModeSupport.AmbientController mAmbientController;
    private MainActivityAmbientCallback mViewModel;
    private VADService mVADService;

    private final int ALL_PERMISSIONS_CODE = 1;
    private final String[] PERMISSIONS_NAMES = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private ArrayList<String> ungrantedPermissions;

    // Backend flags
    private boolean isVADServiceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();

//        checkPermissions();

        initServiceObservers();

        initServices();

        initUIComponents();
    }

    private void printText(){
        if(mVADService != null) {
            mVADService.mainLogic();
            Log.d(TAG, "printText: WE DID IT!!!");
        } else {
            Log.d(TAG, "printText: failed");
        }
    }

    private void checkPermissions() {
        if(ungrantedPermissions == null ) {
            ungrantedPermissions = new ArrayList<>();

            for (String permission : PERMISSIONS_NAMES) {
                if (!checkPermission(permission)) {
                    ungrantedPermissions.add(permission);
                }
            }
        }

        if(ungrantedPermissions.size() > 0) {
            Log.d(TAG, "checkPermissions: Requesting permissions");
            requestPermissions((String[]) ungrantedPermissions.toArray(), ALL_PERMISSIONS_CODE);
        }
    }

    private boolean checkPermission(String permission) {
        boolean granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkPermission: Permission " + permission + (granted ? " GRANTED" : " DENIED"));
        return granted;
    }

    private void initUIComponents() {
        Log.d(TAG, "initUIComponents: Assigning all UI components");
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnInd = (Button) findViewById(R.id.btnInd);
        etFilenameInput = (EditText) findViewById(R.id.etFilename);

        Log.d(TAG, "initUIComponents: Assigning all UI listeners");
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printText();
            }
        });
    }

    private void initServices() {
        Log.d(TAG, "initServices: Initializing services");
        startServices();
    }

    // Set observers on services to monitor bound services
    private void initServiceObservers() {
        Log.d(TAG, "initServiceObservers: Initializing service observers");
        mAmbientController = AmbientModeSupport.attach(this);
        mViewModel = (MainActivityAmbientCallback) getAmbientCallback();
        mViewModel.getBinder().observe(this, new Observer<VADService.VADServiceBinder>() {
            @Override
            public void onChanged(VADService.VADServiceBinder vsb) {
                if(vsb == null) {
                    Log.d(TAG, "onChanged: unbound from service");
                } else {
                    Log.d(TAG, "onChanged: bound to service");
                    mVADService = vsb.getService();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: restarting services");
        startServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: unbinding services");
        if(mViewModel.getBinder() != null) {
            unbindService(mViewModel.getServiceConnection());
        }
    }

    private void startServices() {
        Log.d(TAG, "startServices: starting all services");
        startVADService();
        bindServices();
    }

    private void startVADService() {
        Log.d(TAG, "startVADService: Starting Voice Activity Detection Service");
        Intent serviceIntent = new Intent(context, VADService.class);
        startService(serviceIntent);
    }

    private void bindServices() {
        bindVADService();
    }

    private void bindVADService() {
        Intent serviceBindIntent =  new Intent(this, VADService.class);
        bindService(serviceBindIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MainActivityAmbientCallback();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_CODE: {
                ungrantedPermissions = new ArrayList<>();
                for(int p = 0; p < permissions.length; p++) {
                    if(grantResults[p] != PackageManager.PERMISSION_GRANTED)
                        ungrantedPermissions.add(permissions[p]);
                }

                if(ungrantedPermissions.size() > 0) {
                    checkPermissions();
                }
            }
        }
    }



//    Context context = null;
//
//    Button btnRecord, btnStop, btnInd;
//    EditText etFilenameInput;
//    MediaRecorder mediaRecorder;
//    File RootFolder, AudioFile;
//    com.utaustin.ard.services.VADService myVADService;
//
//    public static final int REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE = 200;
//
//    // Permissions for writing to file and recording audio
//    private String[] permissions = {
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.RECORD_AUDIO
//    };
//
//    //    final int REQUEST_PERMISSION_CODE = 1000; REMOVED
//    final int audioSampleRate = 22050;
//
//    @Override
//    // App events to be completed upon creation (main)
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d(Constants.DEBUG_MAIN, "RRRR recording audio.");
//
//        setContentView(R.layout.activity_main);
//
//        // Enables Always-on
//        setAmbientEnabled();
//
//        context = getApplicationContext();
//        if (!checkPermissonFromDeviceGranted())
//            requestAudioRecordingPermission();
//        folderCheck();
//
//        btnRecord = (Button) findViewById(R.id.btnRecord);
//        btnStop = (Button) findViewById(R.id.btnStop);
//        btnInd = (Button) findViewById(R.id.btnInd);
//        etFilenameInput = (EditText) findViewById(R.id.etFilename);
//
//        btnStop.setEnabled(false);
//
//        startVADService();
//
//        // Begin recording audio
//        btnRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(Constants.DEBUG_MAIN, "Starting recording audio.");
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (fileCheck()) {
//                            setupMediaRecorder();
//                            try {
//                                mediaRecorder.prepare();
//                                mediaRecorder.start();
//                                Log.d(Constants.DEBUG_MAIN, "Successfully started media recorder.");
//                            } catch (IOException e) {
//                                Log.d(Constants.DEBUG_MAIN, "Failed to start media recorder.");
//                                e.printStackTrace();
//                            }
//
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    etFilenameInput.setEnabled(false);
//                                    btnStop.setEnabled(true);
//                                    btnRecord.setEnabled(false);
//                                    btnInd.setBackground(getDrawable(R.drawable.indicator_r));
//                                    Log.d(Constants.DEBUG_MAIN, "Recording audio...");
//                                    Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    }
//                }).start();
//                myVADService.mainLogic();
//            }
//        });
//
//        // Stop recording audio
//        btnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mediaRecorder.stop();
//                btnStop.setEnabled(false);
//                btnRecord.setEnabled(true);
//                etFilenameInput.setEnabled(true);
//                btnInd.setBackground(getDrawable(R.drawable.indicator_g));
//                Log.d(Constants.DEBUG_MAIN, "Finished recording audio.");
//            }
//        });
//    }
//
//    private void startVADService() {
//        Log.d(Constants.DEBUG_MAIN, "Starting VAD service");
//        Intent intent = new Intent(this , com.utaustin.ard.services.VADService.class);
//        startService(intent);
//        bindService(intent , myVADServiceConnection, BIND_AUTO_CREATE);
//    }
//
//    // Create media recorder, set output, encoder, sampling rate, output file
//    private void setupMediaRecorder() {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mediaRecorder.setAudioEncodingBitRate(128000);
//        mediaRecorder.setAudioSamplingRate(audioSampleRate);
//        mediaRecorder.setOutputFile(AudioFile);
//        Log.d(Constants.DEBUG_MAIN, "Completed media recorder setup.");
//    }
//
//    // Request permission to record audio and write to files
//    private void requestAudioRecordingPermission() {
//        ActivityCompat.requestPermissions(this, permissions, REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE);
//        Log.d(Constants.DEBUG_MAIN, "Permissions previously not met. Requesting audio and file write permissions.");
//    }
//
//    @Override
//    // Callback after permission request to handle granted/denied cases
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d("Permission", "onRequestPermissionsResult: " + grantResults[0] + grantResults[1]);
//        switch (requestCode) {
//            case REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE:
//                // Confirm both permissions granted
//                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                    Log.d(Constants.DEBUG_MAIN, "Audio and file write permission granted.");
//                } else {
//                    // TODO: Fix to better user experience (https://developer.android.com/training/permissions/requesting#handle-denial)
//                    Toast.makeText(this, "Permission Denied, EXITING APP", Toast.LENGTH_SHORT).show();
//                    Log.d(Constants.DEBUG_MAIN, "Permission denied... Exiting app...");
//                    System.exit(0);
//                }
//                break;
//            default:
//                Toast.makeText(this, "Unreachable default case reached DEBUG ME PLS!!!???", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Confirm all permissions granted from device for app
//    private boolean checkPermissonFromDeviceGranted() {
//        int write_external_storage_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int record_audio_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        Log.d(Constants.DEBUG_MAIN, "Checking device permissions.");
//        return (write_external_storage_permission == PackageManager.PERMISSION_GRANTED) && (record_audio_permission == PackageManager.PERMISSION_GRANTED);
//    }
//
//    // Create audio output folder if it does not yet exist
//    private void folderCheck() {
//        RootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                + File.separator + Constants.ROOT_DIR);
//        Log.d(Constants.DEBUG_MAIN, "To see audio output, navigate to sdcard/AudioRecordingData/");
//
//        if (!RootFolder.exists()) {
//            Log.d(Constants.DEBUG_MAIN, "Directory: " + File.separator + Constants.ROOT_DIR + " does not yet exist.");
//            boolean success = RootFolder.mkdirs();
//            if (success) {
//                Log.d(Constants.DEBUG_MAIN, "Created directory: " + File.separator + Constants.ROOT_DIR + ".");
//                Toast.makeText(MainActivity.this, "Made folder!", Toast.LENGTH_SHORT).show();
//            } else
//                Log.d(Constants.DEBUG_MAIN, "Failed to create directory: " + File.separator + Constants.ROOT_DIR + ".");
//            Toast.makeText(MainActivity.this, "Folder failed!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Check for valid file name
//    private boolean fileCheck() {
//        String fname = etFilenameInput.getText().toString();
//        if (fname.equals("")) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(MainActivity.this, "No name!", Toast.LENGTH_SHORT).show();
//                }
//            });
//            Log.d(Constants.DEBUG_MAIN, "No text input for new file name.");
//            return false;
//        }
//        AudioFile = new File(RootFolder.getAbsolutePath()
//                + File.separator + fname + ".mp3");
//
//        if (AudioFile.exists()) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(MainActivity.this, "File exists!", Toast.LENGTH_SHORT).show();
//                }
//            });
//            Log.d(Constants.DEBUG_MAIN, "Text input for file name: \"" + fname + ".mp3\" matches existing file.");
//            return false;
//        } else {
//            Log.d(Constants.DEBUG_MAIN, "Created file: \"" + RootFolder.getAbsolutePath()
//                    + File.separator + fname + ".mp3\"");
//            return true;
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unbindService(myVADServiceConnection);
//    }
//
//    private ServiceConnection myVADServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//
//            VADService.VADServiceBinder binderBridge = (VADService.VADServiceBinder) service ;
//            myVADService = binderBridge.getService();
////            isBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
////            isBound = false;
//            myVADService = null;
//        }
//    };
}

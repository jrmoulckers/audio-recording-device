package com.utaustin.ard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.utaustin.ard.constants.Constants;

import java.io.File;
import java.io.IOException;

public class MainActivity extends WearableActivity {
    Context context = null;

    Button btnRecord, btnStop, btnInd;
    EditText etFilenameInput;
    MediaRecorder mediaRecorder;
    File RootFolder, AudioFile;

    public static final int REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE = 200;

    // Permissions for writing to file and recording audio
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    //    final int REQUEST_PERMISSION_CODE = 1000; REMOVED
    final int audioSampleRate = 22050;

    @Override
    // App events to be completed upon creation (main)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        // 1. Grant Permissions
//        ungrantedPermissions = new HashMap<>();
        for(String permission : permissions) {
//            if(!checkPermission(permission)) {
//
//            }
        }

        context = getApplicationContext();
        if (!checkPermissonFromDeviceGranted())
            requestAudioRecordingPermission();
        folderCheck();

        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnInd = (Button) findViewById(R.id.btnInd);
        etFilenameInput = (EditText) findViewById(R.id.etFilename);

        btnStop.setEnabled(false);

        // Begin recording audio
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fileCheck()) {
                            setupMediaRecorder();
                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                Log.d(Constants.DEBUG_MAIN, "Successfully started media recorder.");
                            } catch (IOException e) {
                                Log.d(Constants.DEBUG_MAIN, "Failed to start media recorder.");
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    etFilenameInput.setEnabled(false);
                                    btnStop.setEnabled(true);
                                    btnRecord.setEnabled(false);
                                    btnInd.setBackground(getDrawable(R.drawable.indicator_r));
                                    Log.d(Constants.DEBUG_MAIN, "Recording audio...");
                                    Toast.makeText(MainActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        // Stop recording audio
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                btnStop.setEnabled(false);
                btnRecord.setEnabled(true);
                etFilenameInput.setEnabled(true);
                btnInd.setBackground(getDrawable(R.drawable.indicator_g));
                Log.d(Constants.DEBUG_MAIN, "Finished recording audio.");
            }
        });
    }

    // Create media recorder, set output, encoder, sampling rate, output file
    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(audioSampleRate);
        mediaRecorder.setOutputFile(AudioFile);
        Log.d(Constants.DEBUG_MAIN, "Completed media recorder setup.");
    }

    // Request permission to record audio and write to files
    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE);
        Log.d(Constants.DEBUG_MAIN, "Permissions previously not met. Requesting audio and file write permissions.");
    }

    @Override
    // Callback after permission request to handle granted/denied cases
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("Permission", "onRequestPermissionsResult: " + grantResults[0] + grantResults[1]);
        switch (requestCode) {
            case REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE:
                // Confirm both permissions granted
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.d(Constants.DEBUG_MAIN, "Audio and file write permission granted.");
                } else {
                    // TODO: Fix to better user experience (https://developer.android.com/training/permissions/requesting#handle-denial)
                    Toast.makeText(this, "Permission Denied, EXITING APP", Toast.LENGTH_SHORT).show();
                    Log.d(Constants.DEBUG_MAIN, "Permission denied... Exiting app...");
                    System.exit(0);
                }
                break;
            default:
                Toast.makeText(this, "Unreachable default case reached DEBUG ME PLS!!!???", Toast.LENGTH_SHORT).show();
        }
    }

    // Confirm all permissions granted from device for app
    private boolean checkPermissonFromDeviceGranted() {
        int write_external_storage_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        Log.d(Constants.DEBUG_MAIN, "Checking device permissions.");
        return (write_external_storage_permission == PackageManager.PERMISSION_GRANTED) && (record_audio_permission == PackageManager.PERMISSION_GRANTED);
    }

    // Create audio output folder if it does not yet exist
    private void folderCheck() {
        RootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + Constants.ROOT_DIR);
        Log.d(Constants.DEBUG_MAIN, "To see audio output, navigate to sdcard/AudioRecordingData/");

        if (!RootFolder.exists()) {
            Log.d(Constants.DEBUG_MAIN, "Directory: " + File.separator + Constants.ROOT_DIR + " does not yet exist.");
            boolean success = RootFolder.mkdirs();
            if (success) {
                Log.d(Constants.DEBUG_MAIN, "Created directory: " + File.separator + Constants.ROOT_DIR + ".");
                Toast.makeText(MainActivity.this, "Made folder!", Toast.LENGTH_SHORT).show();
            } else
                Log.d(Constants.DEBUG_MAIN, "Failed to create directory: " + File.separator + Constants.ROOT_DIR + ".");
            Toast.makeText(MainActivity.this, "Folder failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // Check for valid file name
    private boolean fileCheck() {
        String fname = etFilenameInput.getText().toString();
        if (fname.equals("")) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "No name!", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(Constants.DEBUG_MAIN, "No text input for new file name.");
            return false;
        }
        AudioFile = new File(RootFolder.getAbsolutePath()
                + File.separator + fname + ".mp3");

        if (AudioFile.exists()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "File exists!", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(Constants.DEBUG_MAIN, "Text input for file name: \"" + fname + ".mp3\" matches existing file.");
            return false;
        } else {
            Log.d(Constants.DEBUG_MAIN, "Created file: \"" + RootFolder.getAbsolutePath()
                    + File.separator + fname + ".mp3\"");
            return true;
        }
    }
}

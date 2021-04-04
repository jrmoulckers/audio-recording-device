package com.example.mnmwear;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.wearable.activity.WearableActivity;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends WearableActivity {
    Context context = null;

    Button btnRecord, btnStop, btnInd;
    EditText etFilenameInput;
    MediaRecorder mediaRecorder;
    File RootFolder, AudioFile;

    public static final String LOG_TAG = "WatchRecordingDevice";
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

        context = getApplicationContext();
        Log.d(LOG_TAG, "Fake logging.");
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
                                Log.d(LOG_TAG, "Successfully started media recorder.");
                            } catch (IOException e) {
                                Log.d(LOG_TAG, "Failed to start media recorder.");
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    etFilenameInput.setEnabled(false);
                                    btnStop.setEnabled(true);
                                    btnRecord.setEnabled(false);
                                    btnInd.setBackground(getDrawable(R.drawable.indicator_r));
                                    Log.d(LOG_TAG, "Recording audio...");
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
                Log.d(LOG_TAG, "Finished recording audio.");
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
        Log.d(LOG_TAG, "Completed media recorder setup.");
    }

    // Request permission to record audio and write to files
    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE);
        Log.d(LOG_TAG, "Permissions previously not met. Requesting audio and file write permissions.");
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
                    Log.d(LOG_TAG, "Audio and file write permission granted.");
                } else {
                    // TODO: Fix to better user experience (https://developer.android.com/training/permissions/requesting#handle-denial)
                    Toast.makeText(this, "Permission Denied, EXITING APP", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "Permission denied... Exiting app...");
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
        Log.d(LOG_TAG, "Checking device permissions.");
        return (write_external_storage_permission == PackageManager.PERMISSION_GRANTED) && (record_audio_permission == PackageManager.PERMISSION_GRANTED);
    }

    // Create audio output folder if it does not yet exist
    private void folderCheck() {
        RootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "AudioMotionData");
        Log.d(LOG_TAG, "To see audio output, navigate to sdcard/AudioMotionData/");

        if (!RootFolder.exists()) {
            Log.d(LOG_TAG, "Directory: sdcard/AudioMotionData does not yet exist.");
            boolean success = RootFolder.mkdirs();
            if (success) {
                Log.d(LOG_TAG, "Created directory: sdcard/AudioMotionData/.");
                Toast.makeText(MainActivity.this, "Made folder!", Toast.LENGTH_SHORT).show();
            } else
                Log.d(LOG_TAG, "Failed to create directory: sdcard/AudioMotionData/.");
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
            Log.d(LOG_TAG, "No text input for new file name.");
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
            Log.d(LOG_TAG, "Text input for file name: \"" + fname + ".mp3\" matches existing file.");
            return false;
        } else {
            Log.d(LOG_TAG, "Created file: \"" + RootFolder.getAbsolutePath()
                    + File.separator + fname + ".mp3\"");
            return true;
        }
    }
}

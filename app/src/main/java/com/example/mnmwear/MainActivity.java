package com.example.mnmwear;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.wearable.Asset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends WearableActivity {

    Button btnRecord, btnStop, btnInd;
    EditText etFilename;
    MediaRecorder mediaRecorder;
    File RootFolder, AudioFile, MotionFile;
    private FileWriter writer;

//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
//    String accData;
//    private Sensor mGyroscope;

    public static final String LOG_TAG = "WatchRecordingDevice";
    public static final int REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE = 200;

    // Permissions for writing to file and recording audio
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    //    final int REQUEST_PERMISSION_CODE = 1000; REMOVED
    final int audioSampleRate = 22050;

    // choose values of 6.25, 12.5, 25, 50, 100, 200 Hz only
    // any value in between get promoted to higher sampling freq
//    final float motionFrequency = 200.0f;
//    private int sampleTime;
//    long prev_ts=0;

    @Override
    // App events to be completed upon creation (main)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        if (!checkPermissonFromDeviceGranted())
            requestAudioRecordingPermission();
        folderCheck();

//        sampleTime = (int) (1000000.0 / motionFrequency);

        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnInd = (Button) findViewById(R.id.btnInd);
        etFilename = (EditText) findViewById(R.id.etFilename);

//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        btnStop.setEnabled(false);

        // Begin recording audio
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (fileCheck()) {
//                            motionFileWriter();
                            setupMediaRecorder();
                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

//                            mSensorManager.registerListener(MainActivity.this, mAccelerometer, sampleTime);
//                            mSensorManager.registerListener(MainActivity.this, mGyroscope, sampleTime);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    etFilename.setEnabled(false);
                                    btnStop.setEnabled(true);
                                    btnRecord.setEnabled(false);
                                    btnInd.setBackground(getDrawable(R.drawable.indicator_r));
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
//                mSensorManager.unregisterListener(MainActivity.this);
                btnStop.setEnabled(false);
                btnRecord.setEnabled(true);
                etFilename.setEnabled(true);
                btnInd.setBackground(getDrawable(R.drawable.indicator_g));
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    }

    // Request permission to record audio and write to files
    private void requestAudioRecordingPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE);
    }

    @Override
    // Callback after permission request to handle granted/denied cases
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("Permission", "onRequestPermissionsResult: " + grantResults[0] + grantResults[1]);
        switch (requestCode) {
            case REQUEST_AUDIO_AND_FILE_WRITE_PERMISSION_CODE:
                // Confirm both permissions granted
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else {
                    // TODO: Fix to better user experience (https://developer.android.com/training/permissions/requesting#handle-denial)
                    Toast.makeText(this, "Permission Denied, EXITING APP", Toast.LENGTH_SHORT).show();
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
        return (write_external_storage_permission == PackageManager.PERMISSION_GRANTED) && (record_audio_permission == PackageManager.PERMISSION_GRANTED);
    }

    // Create audio output folder if it does not yet exist
    private void folderCheck() {
        RootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "AudioMotionData");
        Log.d(LOG_TAG, "To see audio output, navigate to sdcard/AudioMotionData/");

        if (!RootFolder.exists()) {
            boolean success = RootFolder.mkdirs();
            if (success) {
                Toast.makeText(MainActivity.this, "Made folder!", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(MainActivity.this, "Folder failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // Check for valid file name
    private boolean fileCheck() {
        String fname = etFilename.getText().toString();
        if (fname.equals("")) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "No name!", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        System.out.println(RootFolder.getAbsolutePath());
        AudioFile = new File(RootFolder.getAbsolutePath()
                + File.separator + fname + ".mp3");
//        MotionFile = new File(RootFolder.getAbsolutePath()
//                + File.separator + fname + ".csv");

        if (AudioFile.exists() /*|| MotionFile.exists()*/) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "File exists!", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        } else
            return true;
    }

//    // Assign writer for motion CSV file output
//    private void motionFileWriter() {
//        try {
//            writer = new FileWriter(MotionFile, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    // Assign current sensor data from accelerometer and gyroscope
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.sensor == mAccelerometer) {
//            accData = String.format("%.3f", sensorEvent.values[0]) + "," + String.format("%.3f", sensorEvent.values[1]) + "," + String.format("%.3f", sensorEvent.values[2]) + ",";
////            Log.d("acc", "ts : " + (sensorEvent.timestamp-prev_ts)/1000);
////            prev_ts = sensorEvent.timestamp;
//        } else if (sensorEvent.sensor == mGyroscope) {
//            String gyroData = String.format("%.3f", sensorEvent.values[0]) + "," + String.format("%.3f", sensorEvent.values[1]) + "," + String.format("%.3f", sensorEvent.values[2]) + "\n";
//            try {
//                writer.write(accData + gyroData);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//
//    }
}

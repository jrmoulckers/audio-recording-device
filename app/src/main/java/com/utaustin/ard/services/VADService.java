package com.utaustin.ard.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.utaustin.ard.constants.Constants;

public class VADService extends Service {
    private static final String TAG = Constants.DEBUG_VAD_SERVICE;

    private final IBinder mBinder = new VADServiceBinder();
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "VAD Service destroyed.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onDestroy();
        Log.d(TAG, "VAD Service task removed... stopping service.");
        stopSelf();
    }

    public void mainLogic() {
        Log.d(TAG, "Running the main main logic!!!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Running the inner thread logic!!!");
            }
        }).start();
    }

    public class VADServiceBinder extends Binder {
        public VADService getService() {
            return VADService.this;
        }
    }
}

package com.utaustin.ard;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.wear.ambient.AmbientMode;
import androidx.wear.ambient.AmbientModeSupport;

import com.utaustin.ard.services.VADService;

public class MainActivityAmbientCallback extends AmbientModeSupport.AmbientCallback {
    private static final String TAG = "MainActivityViewModel";
    private MutableLiveData<Boolean> mIsProgressBarUpdating = new MutableLiveData<>();
    private MutableLiveData<VADService.VADServiceBinder> mBinder = new MutableLiveData<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            VADService.VADServiceBinder binder = (VADService.VADServiceBinder) iBinder;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            mBinder.postValue(null);
        }
    };


    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public LiveData<VADService.VADServiceBinder> getBinder() {
        return mBinder;
    }


    public LiveData<Boolean> getIsProgressBarUpdating() {
        return mIsProgressBarUpdating;
    }

    public void setIsProgressBarUpdating(boolean isUpdating) {
        mIsProgressBarUpdating.postValue(isUpdating);
    }
}

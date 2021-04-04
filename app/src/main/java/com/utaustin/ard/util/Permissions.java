package com.utaustin.ard.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.utaustin.ard.constants.Constants;

public class Permissions {
    private static final String DEBUG = Constants.DEBUG_PERMISSIONS;

    // Return whether or not all permissions are granted for the current context
    public static boolean isAllPermissionGranted(Context context) {
        String[] permissionNames = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        int numGrantedPermissions = 0;

        // Check context preference for each permission
        for(String permissionName : permissionNames) {
            if(retrieveSharedPreference(context, permissionName).equals(Constants.PERMISSION_ENABLE)) {
                Log.d(DEBUG, "Permission granted: " + permissionName);
                numGrantedPermissions++;
            }
        }

        // Check if all permissions granted
        if(numGrantedPermissions == permissionNames.length) {
            Log.d(DEBUG, "Storing preference for all permissions granted.");
            writeSharedPreference(context, Constants.ALL_PERMISSIONS_STATUS, Constants.ALL_PERMISSIONS_GRANTED);
        }

        // Return whether all or no permissions granted
        if(retrieveSharedPreference(context, Constants.ALL_PERMISSIONS_STATUS).equals(Constants.ALL_PERMISSIONS_GRANTED)) {
            Log.d(DEBUG, "All permissions granted.");
            return true;
        } else {
            Log.d(DEBUG, "All permissions not granted.");
            return false;
        }
    }

    // Add shared preference for the provided context
    public static void writeSharedPreference(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Get shared preference for the provided context under the provided key
    public static String retrieveSharedPreference(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, Constants.NO_PREF_EXISTS);
    }
}

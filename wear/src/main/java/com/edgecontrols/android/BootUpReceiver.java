package com.edgecontrols.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Sve on 10/8/14.
 */
public class BootUpReceiver extends BroadcastReceiver {
    private static final String tag = "edge.brightness.wearable.listener";
    private String wearPreferences = "MyWearPrefs";
    private boolean isStarted;

    @Override
    public void onReceive(Context context, Intent intent) {
        isStarted = getFlagFromSharedPreferences(context);
        Log.d(tag, "Flag: isStarted = " + isStarted);

        if(isStarted) {
            Intent serviceIntent = new Intent(context, FloatingService.class);
            context.startService(serviceIntent);
        }
    }

    private boolean getFlagFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(wearPreferences, 0);
        return sharedPreferences.getBoolean("isStarted", isStarted);
    }


}

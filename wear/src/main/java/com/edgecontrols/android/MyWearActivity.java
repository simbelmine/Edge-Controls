package com.edgecontrols.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.sve.module.Variables;
import com.google.android.gms.common.api.GoogleApiClient;

public class MyWearActivity extends Activity {
    private static final String tag = "edge.brightness.wearable.listener";
    private String wearPrefs = "MyWearPrefs";
    private SharedPreferences wearSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wearSharedPreferences = getSharedPreferences(wearPrefs, 0);

        serviceAction();

        finish();
    }

    private void serviceAction() {
        boolean isStarted;
        if(!wearSharedPreferences.getBoolean("serviceOn", false)) {
            isStarted = true;
            saveFlagToPreferences(isStarted);
            new SendMessageToPhoneHelper(this, Variables.STARTED);
        }
        else {
            isStarted = false;
            saveFlagToPreferences(isStarted);
            new SendMessageToPhoneHelper(this, Variables.STOPPED);
        }
    }

    private void saveFlagToPreferences(boolean isStarted) {
        SharedPreferences wearSharedPreferences= getSharedPreferences(wearPrefs, 0);
        SharedPreferences.Editor editor = wearSharedPreferences.edit();
        editor.putBoolean("isStarted", isStarted);
        editor.commit();
    }
}

package com.edgecontrols.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
        if(!wearSharedPreferences.getBoolean("serviceOn", false)) {
            new SendMessageToPhoneHelper(this, "STARTED");
        }
        else {
            new SendMessageToPhoneHelper(this, "STOPPED");
        }
    }
}

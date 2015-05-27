package com.edgecontrols.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MyWearActivity extends Activity {
    private static final String tag = "edge.brightness.wearable.listener";
    private String wearPrefs = "MyWearPrefs";
    private SharedPreferences wearSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wearSharedPreferences = getSharedPreferences(wearPrefs, 0);

        if(!wearSharedPreferences.getBoolean("serviceOn", false)) {
            startService(new Intent(getApplicationContext(), FloatingService.class));
            new SendMessageToPhoneHelper(this, "STARTED");
        }
        else {
            stopService(new Intent(getApplicationContext(), FloatingService.class));
            new SendMessageToPhoneHelper(this, "STOPPED");
        }


        finish();
    }
}

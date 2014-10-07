package com.example.sve.edgecontrols;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.sve.module.Variables;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Sve on 10/6/14.
 */
public class ListeningService extends WearableListenerService {
    public static final String tag = "edge.brightness";
    private SharedPreferences sharedPreferences;
    private Boolean isReceived = false;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String messagePath = messageEvent.getPath();

        if(messagePath.equals("DUMMY")) {
            Log.e(tag, "####### it's RECEIVED....");
            isReceived = true;

            sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isReceived", isReceived);
            editor.commit();
        }
        else {
            Log.e(tag, "####### NOT RECEIVED.... :(");
        }

    }
}

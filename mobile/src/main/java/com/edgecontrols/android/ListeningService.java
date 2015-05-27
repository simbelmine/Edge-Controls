package com.edgecontrols.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sve.module.Variables;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Sve on 10/6/14.
 */
public class ListeningService extends WearableListenerService {
    public static final String tag = "edgecontrols.brightness";
    private SharedPreferences sharedPreferences;
    private Boolean isReceived = false;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String messagePath = messageEvent.getPath();

        if ("STARTED".equals(messagePath)) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setAction("com.edgecontrols.receiver.STARTED");
            sendBroadcast(intent);
            Log.e(tag, "####### STARTED it's RECEIVED....");
        }
        if ("STOPPED".equals(messagePath)) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setAction("com.edgecontrols.receiver.STOPPED");
            sendBroadcast(intent);
            Log.e(tag, "####### STOPPED it's RECEIVED....");
        }

    }
}

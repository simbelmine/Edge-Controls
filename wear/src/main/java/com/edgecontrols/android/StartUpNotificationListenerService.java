package com.edgecontrols.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sve.module.Variables;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sve on 10/3/14.
 */
public class StartUpNotificationListenerService extends WearableListenerService {
    private Intent serviceIntent;
    private static final String tag = "edge.brightness.wearable.listener";

    private Set<String> edgeStatusList;
    private String edgeStatus;
    private SharedPreferences sharedPreferences;

    private boolean isStarted = false;
    private String wearPreferences = "MyWearPrefs";

    @Override
    public void onCreate() {
        super.onCreate();
        fillEdgesList();

        sharedPreferences = getSharedPreferences(wearPreferences, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("edgeStatusList", edgeStatusList);
        editor.commit();
    }

    private void fillEdgesList() {
        edgeStatusList = new HashSet<String>();
        edgeStatusList.add(Variables.UPLEFTVISIBLE);
        edgeStatusList.add(Variables.UPLEFTGONE);

        edgeStatusList.add(Variables.MIDDLELEFTVISIBLE);
        edgeStatusList.add(Variables.MIDDLELEFTGONE);

        edgeStatusList.add(Variables.DOWNLEFTVISIBLE);
        edgeStatusList.add(Variables.DOWNLEFTGONE);

        edgeStatusList.add(Variables.UPRIGHTVISIBLE);
        edgeStatusList.add(Variables.UPRIGHTGONE);

        edgeStatusList.add(Variables.RIGHTVISIBLE);
        edgeStatusList.add(Variables.RIGHTGONE);

        edgeStatusList.add(Variables.DOWNRIGHTVISIBLE);
        edgeStatusList.add(Variables.DOWNRIGHTGONE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }



    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.v(tag, "on message received: " + messageEvent.getPath());

        serviceIntent = new Intent(this, FloatingService.class);
        String messagePath = messageEvent.getPath();
        Log.e(tag, "******************===== Message Path ======*********************** = " + messagePath);

        if (messagePath.equals(Variables.START)) {
            Log.e(tag, "*********************** START (on WEAR) *****************************");
            new SendMessageToPhoneHelper(this, Variables.STARTED);
            isStarted = true;
            saveFlagToPreferences();
        }
        else if (messagePath.equals(Variables.STOP)) {
            new SendMessageToPhoneHelper(this, Variables.STOPPED);
            isStarted = false;
            saveFlagToPreferences();
        }
        else if(messagePath.equals(Variables.CHECK_STATUS)) {
            if(sharedPreferences.getBoolean("serviceOn", false)) {
                new SendMessageToPhoneHelper(this, Variables.STARTED);
            }
            else {
                new SendMessageToPhoneHelper(this, Variables.STOPPED);
            }
        }
        else {
            edgeStatus = getEdgeStatusIfCompatible(messagePath);
            if(edgeStatus != null) {
                relaunchService(edgeStatus);
            }
        }

        super.onMessageReceived(messageEvent);
    }

    private void saveFlagToPreferences() {
        SharedPreferences wearSharedPreferences= getSharedPreferences(wearPreferences, 0);
        SharedPreferences.Editor editor = wearSharedPreferences.edit();
        editor.putBoolean("isStarted", isStarted);
        editor.commit();
    }

    private String getEdgeStatusIfCompatible(String messagePath) {
        for(String status : edgeStatusList) {
            if(areCompatible(messagePath, status)) {
                return status;
            }
        }
        return null;
    }

    private boolean areCompatible(String messagePath, String status) {
        if(messagePath.equals(status)) {
            return true;
        }
        return false;
    }

    private void relaunchService(String edge_status) {
        stopService(serviceIntent);
        serviceIntent.putExtra(edge_status,edge_status);
        startService(serviceIntent);
    }
}

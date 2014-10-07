package com.edgecontrols.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sve.module.Variables;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private String nodeId;
    private GoogleApiClient googleApiClient;
    private List<Node> connectedNodes;
    private int numWearables;

    @Override
    public void onCreate() {
        super.onCreate();
        fillEdgesList();

        sharedPreferences = getSharedPreferences("MyWearPrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("edgeStatusList", edgeStatusList);
        editor.commit();

        connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    private void fillEdgesList() {
        edgeStatusList = new HashSet<String>();
        edgeStatusList.add(Variables.UPLEFTVISIBLE);
        edgeStatusList.add(Variables.UPLEFTGONE);

        edgeStatusList.add(Variables.MIDDLELEFTVISIBLE);
        edgeStatusList.add(Variables.MIDDLELEFTGONE);

        edgeStatusList.add(Variables.DOWNLEFTVISIBLE);
        edgeStatusList.add(Variables.DOWNLEFTGONE);

        edgeStatusList.add(Variables.RIGHTVISIBLE);
        edgeStatusList.add(Variables.RIGHTGONE);
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

        if(messagePath.equals("DUMMY")) {
            onWearableConnected();
        }
        else if (messagePath.equals(Variables.START)) {
            Log.e(tag, "*********************** START (on WEAR) *****************************");
            startService(serviceIntent);
            //sendMessageToPhone("STARTED");
            onWearableConnected();
        }
        else if (messagePath.equals(Variables.STOP)) {
            stopService(serviceIntent);
            sendMessageToPhone("STOPPED");
        }
        else {
            edgeStatus = getEdgeStatusIfCompatible(messagePath);
            if(edgeStatus != null) {
                relaunchService(edgeStatus);
            }
        }

        super.onMessageReceived(messageEvent);
    }





    private void onWearableConnected() {
        connectedNodes = new ArrayList<Node>();
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                boolean isWearableConnected = isWearableConnected(getConnectedNodesResult);
                if (isWearableConnected) {
                    nodeId = getConnectedNodesResult.getNodes().get(0).getId();

                    sharedPreferences = getSharedPreferences("MyPrefs", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nodeId", nodeId);
                    editor.commit();

                    sendMessageToPhone("STARTED");
                }
                googleApiClient.disconnect();
            }
        });
    }

    private boolean isWearableConnected(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        connectedNodes = getConnectedNodesResult.getNodes();
        numWearables = connectedNodes.size();
        return numWearables > 0;
    }

    private void sendMessageToPhone(final String variable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionResult connectionResult = googleApiClient.blockingConnect();
                if(connectionResult.isSuccess()) {
                    Log.e(tag, "----------- nodeID = " + nodeId);
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, nodeId, variable, null).await();
                    if (result.getStatus().isSuccess()) {
                        Log.e(tag, "SUCCESS to send the Message (to PHONE): " + variable);
                    } else {
                        Log.e(tag, "Failed to send the Message (to PHONE): " + variable);
                    }
                }
                else {
                    Log.e(tag, "Failed to establish Connection(to PHONE): "
                            + connectionResult.getErrorCode() + "     ***    " + variable);
                }
                googleApiClient.disconnect();
            }
        }).start();
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

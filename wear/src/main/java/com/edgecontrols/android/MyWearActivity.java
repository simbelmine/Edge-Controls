package com.edgecontrols.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.example.sve.module.Variables;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class MyWearActivity extends Activity {
    private static final String tag = "edge.brightness.wearable.listener";
    private String wearPrefs = "MyWearPrefs";
    private SharedPreferences wearSharedPreferences;
    private GoogleApiClient googleApiClient;
    private List<Node> connectedNodes;
    private String nodeId;
    private SharedPreferences sharedPreferences;
    private int numWearables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectGoogleApiClient();
        wearSharedPreferences = getSharedPreferences(wearPrefs, 0);

        sendMessageOnWearableConnected();

        finish();
    }

    private void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    private void sendMessageOnWearableConnected() {
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

                    if(!wearSharedPreferences.getBoolean("serviceOn", false)) {
                        startService(new Intent(getApplicationContext(), FloatingService.class));
                        sendMessageToPhone("STARTED");
                    }
                    else {
                        stopService(new Intent(getApplicationContext(), FloatingService.class));
                        sendMessageToPhone("STOPPED");
                    }
                }
                googleApiClient.disconnect();
            }
        });
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

    private boolean isWearableConnected(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        connectedNodes = getConnectedNodesResult.getNodes();
        numWearables = connectedNodes.size();
        return numWearables > 0;
    }
}

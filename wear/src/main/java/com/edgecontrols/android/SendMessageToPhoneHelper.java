package com.edgecontrols.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 5/27/15.
 */
public class SendMessageToPhoneHelper {
    private static final String tag = "edge.brightness.wearable.listener";
    private Context context;
    private GoogleApiClient googleApiClient;
    private List<Node> connectedNodes;
    private String nodeId;
    private SharedPreferences sharedPreferences;
    private int numWearables;

    public SendMessageToPhoneHelper(Context context, String variable) {
        this.context = context;
        connectGoogleApiClient();
        sendMessageOnWearableConnected(variable);
    }

    private void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    private void sendMessageOnWearableConnected(final String variable) {
        connectedNodes = new ArrayList<Node>();
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                boolean isWearableConnected = isWearableConnected(getConnectedNodesResult);
                if (isWearableConnected) {
                    nodeId = getConnectedNodesResult.getNodes().get(0).getId();

                    sharedPreferences = context.getSharedPreferences("MyPrefs", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nodeId", nodeId);
                    editor.commit();

                    sendMessageToPhone(variable);
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

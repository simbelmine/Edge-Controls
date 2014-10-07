package com.edgecontrols.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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

/**
 * Created by Sve on 10/6/14.
 */
public class WearableMessageSender implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Activity cntx;
    private GoogleApiClient mGoogleApiClient;
    private List<Node> connectedNodes;
    private SharedPreferences senderPreferences;
    private int numWearables;
    private String nodeId;
    private SharedPreferences.Editor editor;

    public static final String tag = "edge.brightness";


    public WearableMessageSender(Activity cntx) {
        this.cntx = cntx;
        mGoogleApiClient = new GoogleApiClient.Builder(cntx)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        onWearableConnected();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onWearableConnected() {
        connectedNodes = new ArrayList<Node>();
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                boolean isWearableConnected = isWearableConnected(getConnectedNodesResult);
                if (isWearableConnected) {
                    nodeId = getConnectedNodesResult.getNodes().get(0).getId();

                    editor = getPreferencesEditor();
                    editor.putString("nodeId", nodeId);
                    editor.commit();
                }
                mGoogleApiClient.disconnect();
            }
        });
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        senderPreferences = cntx.getSharedPreferences("MySenderPrefs", 0);
        SharedPreferences.Editor editor = senderPreferences.edit();
        return editor;
    }

    private boolean isWearableConnected(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        connectedNodes = getConnectedNodesResult.getNodes();
        numWearables = connectedNodes.size();
        editor = getPreferencesEditor();
        editor.putInt("connectedNodes", numWearables);
        editor.commit();

        return numWearables > 0;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}


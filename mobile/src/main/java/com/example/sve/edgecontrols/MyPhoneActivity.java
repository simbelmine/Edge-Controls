package com.example.sve.edgecontrols;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


public class MyPhoneActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private List<Node> connectedNodes;
    private Button settings;
    private Button start;
    private Button stop;

    public static final String tag = "edge.brightness";

    private int numWearables;
    private String nodeId;

    private static boolean isStarted = false;
    private static boolean isStopEnabled = false;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TextView title = (TextView) findViewById(android.R.id.title);
        if (title != null) {
            title.setPadding(10, 0, 0, 0);
            title.setCompoundDrawablePadding(10);
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo, 0, 0, 0);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
//        boolean new_isStarted = sharedPreferences.getBoolean("isStarted", isStarted);
        boolean isStopEnabled_new = sharedPreferences.getBoolean("isStopEnabled", isStopEnabled);
//        isStarted = new_isStarted;
        isStopEnabled = isStopEnabled_new;

        Log.v(tag, "main activity on create");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        Log.e(tag, "onStart ......... ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.e(tag, "onResume .........");
        super.onResume();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(tag, "onConnected ....");

        onWearableConnected();
    }

    @Override
    protected void onPause() {
        Log.e(tag, "onPAUSE .........");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e(tag, "onStop .........");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isStopEnabled", isStopEnabled);
        editor.commit();

        super.onStop();
    }

    private void onWearableConnected() {
        connectedNodes = new ArrayList<Node>();
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                boolean isWearableConnected = isWearableConnected(getConnectedNodesResult);
                if (isWearableConnected) {
                    nodeId = getConnectedNodesResult.getNodes().get(0).getId();

                    sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nodeId", nodeId);
                    editor.putInt("numWearables", numWearables);
                    editor.commit();
                    startWearableFloatingService();
                } else {
                    showWearableNotConnected();
                }
                mGoogleApiClient.disconnect();
            }
        });
    }

    private boolean isWearableConnected(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        connectedNodes = getConnectedNodesResult.getNodes();
        numWearables = connectedNodes.size();
        return numWearables > 0;
    }

    private void startWearableFloatingService() {

        Log.e(tag, "startTheCorrectScreen ........");
        setContentView(R.layout.main);

        settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPhoneActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        start = (Button) findViewById(R.id.start_btn);
        stop = (Button) findViewById(R.id.stop_btn);


        if (!isStopEnabled) {
            start.setEnabled(true);
            start.setBackground(getResources().getDrawable(R.drawable.button_style_up));
            stop.setEnabled(false);
            stop.setTextColor(getResources().getColor(R.color.gray));
        } else {
            start.setEnabled(false);
            start.setTextColor(getResources().getColor(R.color.gray));
            stop.setEnabled(true);
            stop.setBackground(getResources().getDrawable(R.drawable.button_style_up));
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToWear(Variables.START);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToWear(Variables.STOP);
            }
        });
//
//        if (!isStarted) {
//            sendMessageToWear(Variables.START);
//        }
    }

    private void showWearableNotConnected() {
        setContentView(R.layout.no_device);
    }

    private void sendMessageToWear(final String variable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
                if(connectionResult.isSuccess()) {
                        //Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null);
                    if(!isStarted) {
                        sendDummy();
                        if(isReceived()) {
                            isStarted = true;
                            sendMessage(variable);
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Waiting to connect. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    else {
                        sendMessage(variable);
                        Log.e(tag, "Connection established : "
                                + connectionResult.getErrorCode() + "     ***    " + variable);
                        }
                }
                else {
                    Log.e(tag, "Failed to establish Connection: "
                            + connectionResult.getErrorCode() + "     ***    " + variable);
                }
                mGoogleApiClient.disconnect();
            }
        }).start();


    }

    private void sendMessage(String variable) {
        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null).await();
        if (result.getStatus().isSuccess()) {
            if (variable.equals(Variables.START)) {
                isStopEnabled = true;
            } else if (variable.equals(Variables.STOP)) {
                isStopEnabled = false;
            }
        } else {
            Log.e(tag, "Failed to send the Message: " + variable);
            isStopEnabled = true;
        }
    }

    private void sendDummy() {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "DUMMY", null);
    }

    private boolean isReceived() {
        sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
        if(sharedPreferences.getBoolean("isReceived", false)) {
            return true;
        }
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

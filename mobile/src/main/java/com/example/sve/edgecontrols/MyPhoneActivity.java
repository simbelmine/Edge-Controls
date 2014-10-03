package com.example.sve.edgecontrols;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
        boolean new_isStarted = sharedPreferences.getBoolean("isStarted", isStarted);
        boolean isStopEnabled_new = sharedPreferences.getBoolean("isStopEnabled", isStopEnabled);
        isStarted = new_isStarted;
        isStopEnabled = isStopEnabled_new;

        Log.v(tag, "main activity on create");

//         onWearableConnected();

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        Log.e("TAG", "onStart ......... ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.e("TAG", "onResume .........");
        super.onResume();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("TAG", "onConnected ....");
        onWearableConnected();
    }

    @Override
    protected void onPause() {
        Log.e("TAG", "onPAUSE .........");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e("TAG", "onStop .........");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        sharedPreferences = getSharedPreferences("MyMobilePrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isStarted", isStarted);
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

                    sharedPreferences = getSharedPreferences("MyPrefs", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nodeId", nodeId);
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

        Log.e("TAG", "startTheCorrectScreen ........");
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
            stop.setEnabled(false);
        } else {
            start.setEnabled(false);
            stop.setEnabled(true);
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
//        if (!mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
////            Log.e("TAG", " ...... isConnected = " + mGoogleApiClient.isConnected());
//        }

        Log.e("TAG", "sendMessage to Wear....   " + variable + " ...... isConnected = " + mGoogleApiClient.isConnected());

        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
                if(connectionResult.isSuccess()) {
                    //Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null);
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null).await();
                    Log.e("TAG", "MEssage result = " + result.getStatus());
                    if(result.getStatus().isSuccess()) {
                        Log.e("TAG", "*********************** Message Send to WEAR *****************************");
                        if(variable.equals(Variables.START)) {
                            //isStarted = false;
                            isStopEnabled = true;
                        }
                        else if(variable.equals(Variables.STOP)) {
                            isStopEnabled = false;
                        }
                    }
                    else {
                        Log.e("TAG", "Failed to send the Message: " + variable);
                        isStarted = false;
                        isStopEnabled = true;
                    }

                    Log.e("TAG", "Connection established : "
                            + connectionResult.getErrorCode() + "     ***    " + variable);
                }
                else {
                    Log.e("TAG", "Failed to establish Connection: "
                            + connectionResult.getErrorCode() + "     ***    " + variable);
                }
                mGoogleApiClient.disconnect();
            }
        }).start();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

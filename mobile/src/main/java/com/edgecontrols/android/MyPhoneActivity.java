package com.edgecontrols.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sve.edgecontrols.R;
import com.example.sve.module.Variables;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class MyPhoneActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private List<Node> connectedNodes;
    private Button settings;
    private Button startBtn;
    private Button stopBtn;
    private ImageView imageView;

    public static final String tag = "edgecontrols.brightness";
    private String myMobilePrefs = "MyMobilePrefs";

    private int numWearables;
    private String nodeId;

    private static boolean serviceStarted = false;
    private SharedPreferences sharedPreferences;
    boolean stopThread = false;
    boolean connected = false;
    boolean isStopPressed = false;
    private int numMessages = 0;

    private Object lock = new Object();

    private class StarterThread extends Thread {
        @Override
        public void run() {
            super.run();

            updateButtons();

            if (!connected) {
                Log.v(tag, "thread waits for connection!");
                waitToConnect();
            }
            Log.v(tag, "thread notified for connection!");

            slowDown(5000);

            while (!stopThread) {
                Log.v(tag, "trying to start the service.... " + serviceStarted);
                if (!serviceStarted) {
                    sendSingleMessage(Variables.START);
                } else {
                    stopThread = true;
                }
                numMessages++;
                stopThreadBeforeEnd();
                updateButtons();
                slowDown(3000);
            }
        }

        private void slowDown(int sec) {
            try {
                Thread.currentThread().sleep(sec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void waitToConnect() {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Log.v(tag, "exception:" + e.getMessage());
                }
            }
        }

        public void updateButtons() {
            MyPhoneActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStartStopButtons();
                }
            });
        }

        private void stopThreadBeforeEnd() {
            if(numMessages >= 3) {
                stopThread = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Title Bar's Logo
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

        initializeViews();
        getCurrentServiceStatus();

        if (!serviceStarted) {
            StarterThread thread = new StarterThread();
            thread.start();
        }



        Log.v(tag, "onCreate ..........................");
    }

    private void getCurrentServiceStatus() {
        sharedPreferences = getSharedPreferences(myMobilePrefs, 0);
        connected = sharedPreferences.getBoolean("connected", connected);
        serviceStarted = sharedPreferences.getBoolean("serviceStarted", serviceStarted);
        isStopPressed = sharedPreferences.getBoolean("isStopPressed", isStopPressed);
        stopThread = sharedPreferences.getBoolean("stopThread", stopThread);

        Log.e(tag, "******** onCreate ******* connected = " + connected);
        Log.e(tag, "******** onCreate ******* serviceStarted = " + serviceStarted);
    }

    private void registerStartedBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.edgecontrols.receiver.STARTED");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(tag, "phone sent STARTED");
                serviceStarted = true;
                initializeViews();
            }
        };
        registerReceiver(receiver, filter);
    }

    private void registerStoppedBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.edgecontrols.receiver.STOPPED");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(tag, "phone sent STOPPED");
                serviceStarted = false;
                updateStartStopButtons();
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean isServiceStarted() {
        return serviceStarted;
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

        registerStartedBroadcastReceiver();

        new Thread() {
            @Override
            public void run() {
                saveNodes();
                notifyStartThread();
            }
        }.start();
    }

    private void notifyStartThread() {
        if (nodeId != null) {
            connected = true;
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    protected void onPause() {
        Log.e(tag, "onPAUSE .........");
        super.onPause();

        stopThread = true;
    }

    @Override
    protected void onStop() {
        Log.e(tag, "onStop .........");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        saveServiceStarted();

        super.onStop();
    }

    private void saveServiceStarted() {
        SharedPreferences sharedPreferences = getSharedPreferences(myMobilePrefs, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("serviceStarted", serviceStarted);
        editor.putBoolean("connected", connected);
        editor.putBoolean("isStopPressed", isStopPressed);
        editor.putBoolean("stopThread", stopThread);
        editor.commit();

        Log.e(tag, "******** onCreate ******* connected = " + sharedPreferences.getBoolean("connected",connected));
        Log.e(tag, "******** onCreate ******* serviceStarted = " + sharedPreferences.getBoolean("serviceStarted",serviceStarted));
    }

    private void saveNodes() {
        Log.e(tag, "================ SAve Nodes....... ");
        NodeApi.GetConnectedNodesResult connectedNodesResult =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        boolean isWearableConnected = isWearableConnected(connectedNodesResult);
        if (isWearableConnected) {
            nodeId = connectedNodesResult.getNodes().get(0).getId();

            sharedPreferences = getSharedPreferences(myMobilePrefs, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nodeId", nodeId);
            editor.putInt("numWearables", numWearables);
            editor.commit();

            Log.e(tag, "================  NODES ... MAIN view....... ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initializeViews();
                }
            });

        } else {
            Log.e(tag, "================ NODES ... NO nodes ....... ");
            showWearableNotConnected();
        }
        mGoogleApiClient.disconnect();
    }

    private boolean isWearableConnected(NodeApi.GetConnectedNodesResult connectedNodesResult) {
        return connectedNodesResult.getNodes() != null &&
                connectedNodesResult.getNodes().size() > 0;
    }

    private void initializeViews() {

        Log.e(tag, "Init the MAIN view ........");
        setContentView(R.layout.main);

        imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });

        settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startSettingsActivity();
            }
        });

        startBtn = (Button) findViewById(R.id.start_btn);
        stopBtn = (Button) findViewById(R.id.stop_btn);

        updateStartStopButtons();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToWear(Variables.START);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToWear(Variables.STOP);
            }
        });
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(MyPhoneActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void updateStartStopButtons() {
        if (!serviceStarted || isStopPressed) {
            if (!stopThread) {
                startBtn.setEnabled(false);
                startBtn.setText("Starting...");
            } else {
                startBtn.setEnabled(true);
                startBtn.setBackground(getResources().getDrawable(R.drawable.button_style_up));
            }
            stopBtn.setEnabled(false);
            stopBtn.setTextColor(getResources().getColor(R.color.gray));
        } else if(!isStopPressed){
            startBtn.setEnabled(false);
            startBtn.setTextColor(getResources().getColor(R.color.gray));
            stopBtn.setEnabled(true);
            stopBtn.setBackground(getResources().getDrawable(R.drawable.button_style_up));
        }
    }

    private void showWearableNotConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.no_device);
            }
        });
    }

    private void sendMessageToWear(final String variable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSingleMessage(variable);
            }
        }).start();


    }

    private void sendSingleMessage(String variable) {
//        try {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                Log.v(tag, "connected to send a message");
                MessageApi.SendMessageResult result;
                result = Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null).await();
                if (result.getStatus().isSuccess()) {
                    Log.e(tag, "sending message to wear success: " + variable);
                    if(Variables.START.equals(variable)) {
                        isStopPressed = false;
                    }
                    if(Variables.STOP.equals(variable)) {
                        isStopPressed = true;
                    }
                } else {
                    Log.e(tag, "Failed to send the Message: " + variable);
                }
                //mGoogleApiClient.disconnect();
            } else {
                Log.v(tag, "could not connect: " + connectionResult.getErrorCode() + " " + connectionResult.getResolution());
                return;
            }
//        } finally {
//            mGoogleApiClient.disconnect();
//        }

    }

    private void sendDummy() {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "DUMMY", null);
    }

    private boolean isReceived() {
        sharedPreferences = getSharedPreferences(myMobilePrefs, 0);
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
        Log.e(tag, "onConnectionFailed....");
        showWearableNotConnected();
    }
}

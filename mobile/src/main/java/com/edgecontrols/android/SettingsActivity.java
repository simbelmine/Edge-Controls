package com.edgecontrols.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.sve.edgecontrols.R;
import com.example.sve.module.Variables;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Sve on 10/3/14.
 */
public class SettingsActivity extends Activity {
    private GoogleApiClient mGoogleApiClient;
    private ImageView settings_img;
    private View upLeftView;
    private View middleLeftView;
    private View downLeftView;
    private View rightView;
    private boolean upLeft_clicked;
    private boolean middleLeft_clicked;
    private boolean downLeft_clicked;
    private boolean right_clicked;

    private String nodeId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        upLeft_clicked = false;
        middleLeft_clicked = false;
        downLeft_clicked = false;
        right_clicked = true;
        settings_img = (ImageView) findViewById(R.id.settings_img);
        upLeftView = findViewById(R.id.up_left_corner);
        middleLeftView = findViewById(R.id.middle_left);
        downLeftView = findViewById(R.id.down_left_corner);
        rightView = findViewById(R.id.middle_right);

        upLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewColor(upLeftView, upLeft_clicked);
                if(!upLeft_clicked) {
                    sendUpdateMessageToWear(Variables.UPLEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.UPLEFTGONE);
                }
                upLeft_clicked = getNewFlag(upLeft_clicked);
            }
        });

        middleLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewColor(middleLeftView, middleLeft_clicked);
                if(!middleLeft_clicked) {
                    sendUpdateMessageToWear(Variables.MIDDLELEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.MIDDLELEFTGONE);
                }
                middleLeft_clicked = getNewFlag(middleLeft_clicked);
            }
        });

        downLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewColor(downLeftView, downLeft_clicked);
                if(!downLeft_clicked) {
                    sendUpdateMessageToWear(Variables.DOWNLEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.DOWNLEFTGONE);
                }
                downLeft_clicked = getNewFlag(downLeft_clicked);
            }
        });

        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewColor(rightView, right_clicked);
                if(!right_clicked) {
                    sendUpdateMessageToWear(Variables.RIGHTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.RIGHTGONE);
                }
                right_clicked = getNewFlag(right_clicked);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void changeViewColor(View view, boolean flag) {
        if(flag) {
            view.setBackgroundColor(getResources().getColor(R.color.light_blue_transparent));
        }
        else {
            view.setBackgroundColor(getResources().getColor(R.color.light_blue));
        }
    }

    private void sendUpdateMessageToWear(final String variable) {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        sharedPreferences = getSharedPreferences("MyPrefs", 0);
        nodeId = sharedPreferences.getString("nodeId","");
        Log.e("TAG", "------- nodeId = " + nodeId);

        if (nodeId != null && nodeId != "") {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
                    if(connectionResult.isSuccess()) {
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, variable, null);
                    }
                    else {
                        Log.e("TAG", "Failed to establish Connection: "
                                + connectionResult.getErrorCode() + "     ***    " + variable);
                    }
                    mGoogleApiClient.disconnect();
                }
            }).start();
        }
    }

    private boolean getNewFlag(boolean flag) {
        return !flag;
    }
}

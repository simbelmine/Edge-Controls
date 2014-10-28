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
    private View upRightView;
    private View rightView;
    private View downRightView;

    private boolean upLeft_clicked;
    private boolean middleLeft_clicked;
    private boolean downLeft_clicked;
    private boolean upRight_clicked;
    private boolean right_clicked;
    private boolean downRight_clicked;

    private String nodeId;
    private SharedPreferences sharedPreferences;
    private String mobilePrefs = "MyMobilePrefs";
    public static final String tag = "edgecontrols.brightness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        settings_img = (ImageView) findViewById(R.id.settings_img);
        upLeftView = findViewById(R.id.up_left_corner);
        middleLeftView = findViewById(R.id.middle_left);
        downLeftView = findViewById(R.id.down_left_corner);
        upRightView =findViewById(R.id.up_right_corner);
        rightView = findViewById(R.id.middle_right);
        downRightView = findViewById(R.id.down_right_corner);

        updateFlagsFromPreferences();
        updateViewColor();
        setViewsClickListener();
    }

    private void updateFlagsFromPreferences() {
        SharedPreferences sharedPrefs = getSharedPreferences(mobilePrefs, 0);
        upLeft_clicked = sharedPrefs.getBoolean("upLeft_clicked", true);
        middleLeft_clicked = sharedPrefs.getBoolean("middleLeft_clicked", true);
        downLeft_clicked = sharedPrefs.getBoolean("downLeft_clicked", true);
        upRight_clicked = sharedPrefs.getBoolean("upRight_clicked", true);
        right_clicked = sharedPrefs.getBoolean("right_clicked", false);
        downRight_clicked = sharedPrefs.getBoolean("downRight_clicked", true);
    }

    private void updateViewColor() {
        changeViewColor(upLeftView, upLeft_clicked);
        changeViewColor(middleLeftView, middleLeft_clicked);
        changeViewColor(downLeftView, downLeft_clicked);
        changeViewColor(upRightView, upRight_clicked);
        changeViewColor(rightView, right_clicked);
        changeViewColor(downRightView, downRight_clicked);
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
        saveFlagsToPreferences();
        super.onStop();
    }

    private void saveFlagsToPreferences() {
        SharedPreferences sharedPrefs = getSharedPreferences(mobilePrefs, 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("upLeft_clicked", upLeft_clicked);
        editor.putBoolean("middleLeft_clicked", middleLeft_clicked);
        editor.putBoolean("downLeft_clicked", downLeft_clicked);
        editor.putBoolean("upRight_clicked", upRight_clicked);
        editor.putBoolean("right_clicked", right_clicked);
        editor.putBoolean("downRight_clicked", downRight_clicked);
        editor.commit();
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
//        if (!mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
//        }

        sharedPreferences = getSharedPreferences(mobilePrefs, 0);
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

    private void setViewsClickListener() {
        upLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(upLeft_clicked) {
                    sendUpdateMessageToWear(Variables.UPLEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.UPLEFTGONE);
                }
                upLeft_clicked = getNewFlag(upLeft_clicked);
                changeViewColor(upLeftView, upLeft_clicked);
            }
        });

        middleLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(middleLeft_clicked) {
                    sendUpdateMessageToWear(Variables.MIDDLELEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.MIDDLELEFTGONE);
                }
                middleLeft_clicked = getNewFlag(middleLeft_clicked);
                changeViewColor(middleLeftView, middleLeft_clicked);
            }
        });

        downLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downLeft_clicked) {
                    sendUpdateMessageToWear(Variables.DOWNLEFTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.DOWNLEFTGONE);
                }
                downLeft_clicked = getNewFlag(downLeft_clicked);
                changeViewColor(downLeftView, downLeft_clicked);
            }
        });

        upRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(upRight_clicked) {
                    sendUpdateMessageToWear(Variables.UPRIGHTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.UPRIGHTGONE);
                }
                upRight_clicked = getNewFlag(upRight_clicked);
                changeViewColor(upRightView, upRight_clicked);
            }
        });

        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(right_clicked) {
                    sendUpdateMessageToWear(Variables.RIGHTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.RIGHTGONE);
                }
                right_clicked = getNewFlag(right_clicked);
                changeViewColor(rightView, right_clicked);
            }
        });

        downRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downRight_clicked) {
                    sendUpdateMessageToWear(Variables.DOWNRIGHTVISIBLE);
                }
                else {
                    sendUpdateMessageToWear(Variables.DOWNRIGHTGONE);
                }
                downRight_clicked = getNewFlag(downRight_clicked);
                changeViewColor(downRightView, downRight_clicked);
            }
        });
    }
}

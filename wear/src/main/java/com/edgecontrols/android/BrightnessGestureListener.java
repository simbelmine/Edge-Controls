package com.edgecontrols.android;

import android.content.Context;
import android.graphics.Point;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by Sve on 10/3/14.
 */
public class BrightnessGestureListener implements GestureDetector.OnGestureListener {
    private static final String tag = "edge.brightness.wearable.listener";
    Context cntx;
    WindowManager windowManager;
    CustomView view;
    float brightness;
    int viewHeight;
    int MIN_DIST;
    float distY_old = 0;
    float distY_diff = 0;
    float initialBrightness;
    boolean isGoingUp = false;
    float initialY;

    BrightnessGestureListener(Context cntx, WindowManager windowManager, float brightness, CustomView view) {
        this.cntx = cntx;
        this.windowManager = windowManager;
        this.brightness = brightness;
        this.view = view;
        viewHeight = getDisplayHeight(windowManager);
        MIN_DIST = viewHeight / 10;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.v(tag, "###--- onDown() ---###");
        initialBrightness = brightness;
        initialY = e.getY();
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v(tag, "###--- onLongPress() ---###");
        putAutoBrightness();
        view.indicator.setText("Auto Mode");
    }

    @Override
    public boolean onScroll(MotionEvent firstEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
        Log.v(tag, "###--- onScroll() ---###");
        float e2Y = currentEvent.getY();
        float distY = Math.round(e2Y - initialY);

        int deltaBrightness = distanceToBrightness(distY);
        float newBrightness = initialBrightness - deltaBrightness;
        if (newBrightness > 100) {
            initialY = currentEvent.getY();
            initialBrightness = 100;
            newBrightness = 100;
        }
        if (newBrightness < 0) {
            initialY = currentEvent.getY();
            initialBrightness = 0;
            newBrightness = 0;
        }
        if (brightness != newBrightness) {
            brightness = newBrightness;
            putManualBrightness();
            setDisplayBrightness(brightness);
            showBrightnessMsg("", brightness);
        }
        return true;
    }

    private int distanceToBrightness(float distY) {
        return ((int)Math.round(distY / MIN_DIST)) * 10;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        return false;
    }

    private int getDisplayHeight(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();
        Point display_size = new Point();
        display.getSize(display_size);

        return display_size.y;
    }

    private void putManualBrightness() {
        try {
            int autoMode = Settings.System.getInt(cntx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if(autoMode == 1) {
                Settings.System.putInt(cntx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }
        catch(Settings.SettingNotFoundException ex) {
            Log.e("Exception", "Cannot read Brightness Mode  " + ex);
        }
    }

    private void putAutoBrightness() {
        try {
            int autoMode = Settings.System.getInt(cntx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if(autoMode != 1) {
                Settings.System.putInt(cntx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }
        }
        catch (Settings.SettingNotFoundException ex) {
            Log.e("Exception","Cannot read Brightness Mode  " + ex);
        }
    }

    private void setDisplayBrightness(final float brightness) {
        //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int)brightness);
        view.setBrightness(brightness);
    }

    private void showBrightnessMsg(String s, float brightness) {
        view.indicator.setText(((int)brightness) + "%");
    }

    private void printLog(boolean isGoingUp, float distY, float distY_diff, int MIN_DIST, float brightness) {
        if(isGoingUp)
            Log.v("sve", "UP .................................................");
        else
            Log.v("sve", "DOWN .................................................");
        Log.v("sve", "distY = " + distY);
        Log.v("sve", "distY_diff = " + distY_diff);
        Log.v("sve", "MIN_DIST = " + MIN_DIST);
        Log.v("sve", "brightness = " + brightness);
    }
}

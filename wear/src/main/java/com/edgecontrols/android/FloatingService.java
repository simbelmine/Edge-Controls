package com.edgecontrols.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.example.sve.edgecontrols.R;
import com.example.sve.module.Variables;

import java.util.Set;

/**
 * Created by Sve on 10/3/14.
 */
public class FloatingService extends Service {
    private WindowManager windowManager;
    private CustomView upRightCornerView;
    private CustomView rightView;
    private CustomView downRightCornerView;
    private CustomView upLeftCornerView;
    private CustomView middleLeftView;
    private CustomView downLeftCornerView;
    private TextView tv;

    private WindowManager.LayoutParams paramsTextView;
    private WindowManager.LayoutParams left_params;
    private WindowManager.LayoutParams upLeft_params;
    private WindowManager.LayoutParams downLeft_params;
    private WindowManager.LayoutParams right_params;
    private WindowManager.LayoutParams upRight_params;
    private WindowManager.LayoutParams downRight_params;

    private float brightness = 0;

    private Set<String> edgesStatusSet;
    private String edgeVisibilityParam;
    private SharedPreferences localSharedPrefs;
    private String localPrefs = "MyLocalPrefs";
    private String wearPrefs = "MyWearPrefs";
    private Context context;
    private static final String tag = "edge.brightness.wearable.listener";
    private boolean viewsAddedToWM = false;

    private int viewRightWidth = 30;
    private int viewLeftWidth = 25;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //super.onCreate();

        context = getApplicationContext();
        localSharedPrefs = context.getSharedPreferences(localPrefs, 0);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        initializeViews();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences(wearPrefs, 0);
        edgesStatusSet = sharedPreferences.getStringSet("edgeStatusList", null);

        if (edgesStatusSet != null) {
            edgeVisibilityParam = getStatusFromExtras(intent);
        }

        Log.e(tag, "******** EDGE status = " + edgeVisibilityParam + "***************");

        if(edgeVisibilityParam != null) {
            setEdgeVisibility();
            updateViewsVisibilities();
        }
        else {
            updateViewsVisibilities();
        }

        if (!viewsAddedToWM) {
            windowManager.addView(tv, paramsTextView);
            windowManager.addView(middleLeftView, left_params);
            windowManager.addView(upLeftCornerView, upLeft_params);
            windowManager.addView(downLeftCornerView, downLeft_params);
            windowManager.addView(rightView, right_params);
            windowManager.addView(upRightCornerView, upRight_params);
            windowManager.addView(downRightCornerView, downRight_params);

            viewsAddedToWM = true;
        }

        return Service.START_NOT_STICKY;
    }

    private String getStatusFromExtras(Intent intent) {
        for(String status : edgesStatusSet) {
            if(intent.hasExtra(status)){
                return status;
            }
        }
        return null;
    }

    private void updateViewsVisibilities() {
        //localSharedPrefs = context.getSharedPreferences("MyLocalPrefs", 0);
        Log.e("TAG", "************** INITIAL ***************");

        boolean upLeft = localSharedPrefs.getBoolean("upLeft", false);
        boolean middleLeft = localSharedPrefs.getBoolean("middleLeft", false);
        boolean downLeft = localSharedPrefs.getBoolean("downLeft", false);
        boolean upRight = localSharedPrefs.getBoolean("upRight", false);
        boolean right = localSharedPrefs.getBoolean("right", true);
        boolean downRight = localSharedPrefs.getBoolean("downRight", false);

        changeViewVisibility(upLeft, upLeftCornerView);
        changeViewVisibility(middleLeft, middleLeftView);
        changeViewVisibility(downLeft, downLeftCornerView);
        changeViewVisibility(upRight, upRightCornerView);
        changeViewVisibility(right, rightView);
        changeViewVisibility(downRight, downRightCornerView);
    }

    private void changeViewVisibility(boolean active, CustomView view) {
        if (active) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void saveUpLeft(boolean active) {
        save("upLeft", active);
    }
    private void saveMiddleLeft(boolean active) {
        save("middleLeft", active);
    }
    private void saveDownLeft(boolean active) {
        save("downLeft", active);
    }
    private void saveUpRight(boolean active) { save("upRight", active); }
    private void saveRight(boolean active) {
        save("right", active);
    }
    private void saveDownRight(boolean active) { save("downRight", active); }

    private void save(String controlName, boolean active) {
        localSharedPrefs = context.getSharedPreferences(localPrefs, 0);
        SharedPreferences.Editor editor = localSharedPrefs.edit();
        editor.putBoolean(controlName, active);
        editor.commit();
    }

    private void setEdgeVisibility() {
        // ***** Switch-Case it's not working for Strings *****//

        if (Variables.UPLEFTVISIBLE.equals(edgeVisibilityParam)) {
            saveUpLeft(true);
        } else if (edgeVisibilityParam.equals(Variables.UPLEFTGONE)) {
            saveUpLeft(false);
        } else if (edgeVisibilityParam.equals(Variables.MIDDLELEFTVISIBLE)) {
            saveMiddleLeft(true);
        } else if (edgeVisibilityParam.equals(Variables.MIDDLELEFTGONE)) {
            saveMiddleLeft(false);
        } else if (edgeVisibilityParam.equals(Variables.DOWNLEFTVISIBLE)) {
            saveDownLeft(true);
        } else if (edgeVisibilityParam.equals(Variables.DOWNLEFTGONE)) {
            saveDownLeft(false);
        } else if(edgeVisibilityParam.equals(Variables.UPRIGHTVISIBLE)) {
            saveUpRight(true);
        } else if(edgeVisibilityParam.equals(Variables.UPRIGHTGONE)) {
            saveUpRight(false);
        } else if (edgeVisibilityParam.equals(Variables.RIGHTVISIBLE)) {
            saveRight(true);
        } else if (edgeVisibilityParam.equals(Variables.RIGHTGONE)) {
            saveRight(false);
        } else if(edgeVisibilityParam.equals(Variables.DOWNRIGHTVISIBLE)) {
            saveDownRight(true);
        } else if(edgeVisibilityParam.equals(Variables.DOWNRIGHTGONE)) {
            saveDownRight(false);
        }

        updateViewsVisibilities();
    }

    private int getDisplayHeight(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();
        Point display_size = new Point();
        display.getSize(display_size);

        return display_size.y;
    }

    @Override
    public void onDestroy() {

        if(tv != null) windowManager.removeViewImmediate(tv);
        if(middleLeftView != null) windowManager.removeViewImmediate(middleLeftView);
        if(upLeftCornerView != null) windowManager.removeViewImmediate(upLeftCornerView);
        if(downLeftCornerView != null) windowManager.removeViewImmediate(downLeftCornerView);
        if(rightView != null) windowManager.removeViewImmediate(rightView);
        if(upRightCornerView != null) windowManager.removeViewImmediate(upRightCornerView);
        if(downRightCornerView != null) windowManager.removeViewImmediate(downRightCornerView);

        //Toast.makeText(this, "Destroy ...", Toast.LENGTH_LONG).show();
        stopSelf();
        super.onDestroy();
    }

    private void initializeViews() {

        ////////// *** Middle Text View *** //////////
        tv = new TextView(this);
        paramsTextView = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        tv.setLayoutParams(paramsTextView);
        paramsTextView.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        tv.setVisibility(View.GONE);
        tv.setTypeface(Typeface.DEFAULT_BOLD);


        //////////////////////////////////////////////
        ////////// *** Left Middle View *** //////////
        //////////////////////////////////////////////
        middleLeftView = new CustomView(this, tv);
        //middleLeftView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        left_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        left_params.gravity = Gravity.LEFT | Gravity.CENTER_HORIZONTAL;
        left_params.x = 0;
        left_params.y = 0;
        left_params.width = viewLeftWidth;
        left_params.height = getDisplayHeight(windowManager)/2; // WHOLE SCREEEN (if HALF divide by 2)

        final GestureDetector myGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, middleLeftView));
        middleLeftView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if (event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myGesture.onTouchEvent(event);
            }
        });
        middleLeftView.setClickable(true);


        //////////////////////////////////////////////
        ////////// *** Left Up View *** //////////
        //////////////////////////////////////////////
        upLeftCornerView = new CustomView(this, tv);
        //upLeftCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        upLeft_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        upLeft_params.gravity = Gravity.LEFT | Gravity.TOP;
        upLeft_params.x = 0;
        upLeft_params.y = 0;
        upLeft_params.width = viewLeftWidth;
        upLeft_params.height = getDisplayHeight(windowManager)/4;

        final GestureDetector myUpLeftGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, upLeftCornerView));
        upLeftCornerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myUpLeftGesture.onTouchEvent(event);
            }
        });
        upLeftCornerView.setClickable(true);


        //////////////////////////////////////////////
        ////////// *** Left Down View *** //////////
        //////////////////////////////////////////////
        downLeftCornerView = new CustomView(this, tv);
        //downLeftCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        downLeft_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        downLeft_params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        downLeft_params.x = 0;
        downLeft_params.y = 0;
        downLeft_params.width = viewLeftWidth;
        downLeft_params.height = getDisplayHeight(windowManager)/4;

        final GestureDetector myDownLeftGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, downLeftCornerView));
        downLeftCornerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myDownLeftGesture.onTouchEvent(event);
            }
        });
        downLeftCornerView.setClickable(true);


        //////////////////////////////////////////////
        ////////// *** Right MIDDLE View *** //////////
        //////////////////////////////////////////////
        rightView = new CustomView(this, tv);
//        rightView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        right_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        right_params.gravity = Gravity.RIGHT | Gravity.CENTER_HORIZONTAL;
        right_params.x = 0;
        right_params.y = 0;
        right_params.width = viewRightWidth;
        right_params.height = getDisplayHeight(windowManager)/2;

        final GestureDetector myRightGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, rightView));
        rightView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myRightGesture.onTouchEvent(event);
            }
        });
        rightView.setClickable(true);


        //////////////////////////////////////////////
        ////////// *** Right Up View *** //////////
        //////////////////////////////////////////////
        upRightCornerView = new CustomView(this, tv);
//        upRightCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        upRight_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        upRight_params.gravity = Gravity.RIGHT | Gravity.TOP;
        upRight_params.x = 0;
        upRight_params.y = 0;
        upRight_params.width = viewRightWidth;
        upRight_params.height = getDisplayHeight(windowManager)/4;

        final GestureDetector myUpRightGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, upRightCornerView));
        upRightCornerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myUpRightGesture.onTouchEvent(event);
            }
        });
        upRightCornerView.setClickable(true);



        //////////////////////////////////////////////
        ////////// *** Right Down View *** //////////
        //////////////////////////////////////////////

        downRightCornerView = new CustomView(this, tv);
//        downRightCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        downRight_params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        downRight_params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        downRight_params.x = 0;
        downRight_params.y = 0;
        downRight_params.width = viewRightWidth;
        downRight_params.height = getDisplayHeight(windowManager)/4;

        final GestureDetector myDownRightGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, downRightCornerView));
        downRightCornerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myDownRightGesture.onTouchEvent(event);
            }
        });
        downRightCornerView.setClickable(true);
    }
}

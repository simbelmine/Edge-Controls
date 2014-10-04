package com.example.sve.edgecontrols;

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


import com.example.sve.module.Variables;

import java.util.Set;

/**
 * Created by Sve on 10/3/14.
 */
public class FloatingService extends Service {
    private WindowManager windowManager;
    private CustomView leftView;
    private CustomView rightView;
    private CustomView upLeftCornerView;
    private CustomView downLeftCornerView;
    private TextView tv;

    private WindowManager.LayoutParams paramsTextView;
    private WindowManager.LayoutParams left_params;
    private WindowManager.LayoutParams upLeft_params;
    private WindowManager.LayoutParams downLeft_params;
    private WindowManager.LayoutParams right_params;

    private float brightness = 0;

    private Set<String> edgesStatusSet;
    private String edgeStatus;
    private SharedPreferences localSharedPrefs;
    private Context context;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //super.onCreate();

        context = getApplicationContext();
        localSharedPrefs = context.getSharedPreferences("MyLocalPrefs", 0);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

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
        leftView = new CustomView(this, tv);
        leftView.setBackgroundColor(getResources().getColor(R.color.light_blue));
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
        left_params.width = 25;
        left_params.height = getDisplayHeight(windowManager)/2; // WHOLE SCREEEN (if HALF divide by 2)

        final GestureDetector myGesture = new GestureDetector(this,
                new BrightnessGestureListener(this, windowManager, brightness, leftView));
        leftView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    tv.setVisibility(View.VISIBLE);
                else if(event.getActionMasked() == MotionEvent.ACTION_UP)
                    tv.setVisibility(View.GONE);
                return myGesture.onTouchEvent(event);
            }
        });
        leftView.setClickable(true);


        //////////////////////////////////////////////
        ////////// *** Left Up View *** //////////
        //////////////////////////////////////////////
        upLeftCornerView = new CustomView(this, tv);
        upLeftCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
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
        upLeft_params.width = 25;
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
        downLeftCornerView.setBackgroundColor(getResources().getColor(R.color.light_blue));
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
        downLeft_params.width = 25;
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
        ////////// *** Right View *** //////////
        //////////////////////////////////////////////
        rightView = new CustomView(this, tv);
        rightView.setBackgroundColor(getResources().getColor(R.color.light_blue));
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
        right_params.width = 25;
        right_params.height = getDisplayHeight(windowManager);

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyWearPrefs", 0);
        edgesStatusSet = sharedPreferences.getStringSet("edgeStatusList", null);

        if(edgesStatusSet != null) {
            edgeStatus = getStatusFromExtras(intent);
        }

        Log.e("TAG", "******** Edge status = " + edgeStatus);
        Log.e("TAG", "************** SHARED PREFS = " + String.valueOf(localSharedPrefs.getInt("upLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("middLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("downLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("right",11)));
        if(edgeStatus != null) {
            setEdgeVisibility();
            setInitialVisability();
        }
        else {
            setInitialVisability();
        }


        windowManager.addView(tv, paramsTextView);
        windowManager.addView(leftView, left_params);
        windowManager.addView(upLeftCornerView, upLeft_params);
        windowManager.addView(downLeftCornerView, downLeft_params);
        windowManager.addView(rightView, right_params);



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

    private void setInitialVisability() {
        //localSharedPrefs = context.getSharedPreferences("MyLocalPrefs", 0);
        Log.e("TAG", "************** INITIAL ***************");

        //------ UP - LEFT
        if(localSharedPrefs.getInt("upLeft", 11) != 11) {
            if(localSharedPrefs.getInt("upLeft", 11) == View.VISIBLE) {
                upLeftCornerView.setVisibility(View.VISIBLE);
            }
            else {
                upLeftCornerView.setVisibility(View.GONE);
            }
        }
        else {
            upLeftCornerView.setVisibility(View.GONE);
            Log.e("TAG", "------ UP INIT ----");
        }

        //------ MIDDLE - LEFT
        if(localSharedPrefs.getInt("middLeft", 11) != 11) {
            if(localSharedPrefs.getInt("middLeft", 11) == View.VISIBLE) {
                leftView.setVisibility(View.VISIBLE);
                Log.e("TAG", "------ MIDDLE visible into Init method ----");
            }
            else {
                leftView.setVisibility(View.GONE);
            }
        }
        else {
            leftView.setVisibility(View.GONE);
            Log.e("TAG", "------ MIDDLE INIT ----");
        }

        //----- DOWN - LEFT
        if(localSharedPrefs.getInt("downLeft", 11) != 11) {
            if(localSharedPrefs.getInt("downLeft", 11) == View.VISIBLE) {
                downLeftCornerView.setVisibility(View.VISIBLE);
            }
            else {
                downLeftCornerView.setVisibility(View.GONE);
            }
        }
        else {
            downLeftCornerView.setVisibility(View.GONE);
            Log.e("TAG", "------ DOWN INIT ----");
        }

        //---- RIGHT
        if(localSharedPrefs.getInt("right", 11) != 11) {
            if(localSharedPrefs.getInt("right", 11) == View.VISIBLE) {
                rightView.setVisibility(View.VISIBLE);
                Log.e("TAG", "------ RIGHT VISIBLE ----");
            }
            else {
                rightView.setVisibility(View.GONE);
                Log.e("TAG", "------ RIGHT GONE ----");
            }
        }
        else {
            rightView.setVisibility(View.VISIBLE);
            Log.e("TAG", "------ RIGHT INIT ----");
        }


        putInSharedPreferences(upLeftCornerView.getVisibility(), leftView.getVisibility(),
                downLeftCornerView.getVisibility(), rightView.getVisibility());

        Log.e("TAG", "------------- shared preferences = " + String.valueOf(localSharedPrefs.getInt("upLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("middLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("downLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("right",11)));

    }


    private void putInSharedPreferences(int upLeft, int middLeft, int downLeft, int right) {
        localSharedPrefs = context.getSharedPreferences("MyLocalPrefs", 0);
        SharedPreferences.Editor editor = localSharedPrefs.edit();
        if(upLeft== 0 || upLeft == 8) editor.putInt("upLeft", upLeft);
        if(middLeft== 0 || middLeft == 8) editor.putInt("middLeft", middLeft);
        if(downLeft== 0 || downLeft == 8) editor.putInt("downLeft", downLeft);
        if(right== 0 || right == 8) editor.putInt("right", right);
        editor.commit();
        Log.e("TAG", "------------- SHARED preferences = " + String.valueOf(localSharedPrefs.getInt("upLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("middLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("downLeft",11)) + "  " +
                String.valueOf(localSharedPrefs.getInt("right",11)));
    }


    private void setEdgeVisibility() {

        // ***** Switch-Case it's not working for Strings *****//

        // Up - Left
        if(edgeStatus.equals(Variables.UPLEFTVISIBLE)) {
            upLeftCornerView.setVisibility(View.VISIBLE);
            putInSharedPreferences(upLeftCornerView.getVisibility(), 11, 11, 11);
        }
        else if(edgeStatus.equals(Variables.UPLEFTGONE)) {
            upLeftCornerView.setVisibility(View.GONE);
            putInSharedPreferences(upLeftCornerView.getVisibility(), 11, 11, 11);
        }
        // Middle - Left
        else if(edgeStatus.equals(Variables.MIDDLELEFTVISIBLE)) {
            leftView.setVisibility(View.VISIBLE);
            putInSharedPreferences(11, leftView.getVisibility(), 11, 11);
            Log.e("TAG", "------ MIDDLE visible INTO edgeVisability() ----");
        }
        else if(edgeStatus.equals(Variables.MIDDLELEFTGONE)){
            leftView.setVisibility(View.GONE);
            putInSharedPreferences(11, leftView.getVisibility(), 11, 11);
        }
        // Down - Left
        else if(edgeStatus.equals(Variables.DOWNLEFTVISIBLE)) {
            downLeftCornerView.setVisibility(View.VISIBLE);
            putInSharedPreferences(11, 11, downLeftCornerView.getVisibility(), 11);
        }
        else if(edgeStatus.equals(Variables.DOWNLEFTGONE)) {
            downLeftCornerView.setVisibility(View.GONE);
            putInSharedPreferences(11, 11, downLeftCornerView.getVisibility(), 11);
        }
        // Right
        else if(edgeStatus.equals(Variables.RIGHTVISIBLE)) {
            rightView.setVisibility(View.VISIBLE);
            putInSharedPreferences(11, 11, 11, rightView.getVisibility());
            Log.e("TAG", "------ RIGHT is VISIBLE -------     " + localSharedPrefs.getInt("middLeft", 55));
            Log.e("TAG", "------------- shared preferences = " + String.valueOf(localSharedPrefs.getInt("upLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("middLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("downLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("right",11)));
        }
        else if(edgeStatus.equals(Variables.RIGHTGONE)) {
            rightView.setVisibility(View.GONE);
            putInSharedPreferences(11, 11, 11, rightView.getVisibility());
            Log.e("TAG", "------ RIGHT is INVISIBLE -------     " + localSharedPrefs.getInt("middLeft", 55));
            Log.e("TAG", "------------- shared preferences = " + String.valueOf(localSharedPrefs.getInt("upLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("middLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("downLeft",11)) + "  " +
                    String.valueOf(localSharedPrefs.getInt("right",11)));
        }
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
        if(leftView != null) windowManager.removeViewImmediate(leftView);
        if(upLeftCornerView != null) windowManager.removeViewImmediate(upLeftCornerView);
        if(downLeftCornerView != null) windowManager.removeViewImmediate(downLeftCornerView);
        if(rightView != null) windowManager.removeViewImmediate(rightView);

        //Toast.makeText(this, "Destroy ...", Toast.LENGTH_LONG).show();
        stopSelf();
        super.onDestroy();
    }
}

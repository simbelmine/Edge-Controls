package com.example.sve.edgecontrols;

import android.content.Context;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Sve on 10/3/14.
 */
public class CustomView extends View {
    private static final float VERTICAL_THRESHOLD = 10;
    private static final float HORIZONTAL_THRESHOLD = 10;
    private VelocityTracker mVelocityTracker = null;

    private float startX = 0;
    private float startY = 0;
    private float brightness;
    protected boolean up_flag;
    protected TextView indicator;

    int display_size = getContext().getResources().getDisplayMetrics().heightPixels;

    //**************************//
    //**************************//

    public CustomView(Context context, TextView tv) {
        super(context);
        this.indicator = tv;
    }
//
//    public LeftCustomView(Context context, AttributeSet attrs) {
//        super(context,attrs);
//
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
//                R.styleable.LovelyView, 0, 0);
//
//        try {
//            // get the text and colors specified using the names in attrs.xml
//            a.getColor(R.styleable.LovelyView_bgColor, Color.WHITE);
//        } finally {
//            a.recycle();
//        }
//    }

    public boolean isUp_flag() {
        return up_flag;
    }

    public void setUp_flag(boolean up_flag) {
        this.up_flag = up_flag;
    }

    public float getBrightness() {
        brightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
        return brightness;
    }

    public void setBrightness(float brightness) {
        int newBrightness = (int)((brightness/100)*255);
        Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newBrightness);
        this.brightness = brightness;
    }

    private boolean isAboveOrBelow(MotionEvent event) {
        int half_display = display_size/2;
        int above_free_space = half_display/2;
        int below_free_space = display_size - above_free_space;

//        Log.v("sve", "above_free_space = " +  above_free_space);
//        Log.v("sve", "below_free_space = " +  below_free_space);

        if(event.getY() < above_free_space || event.getY() > below_free_space) {
            return true;
        }

        return false;
    }

    private boolean isMovedRight(MotionEvent event) {
        int view_width = getMeasuredWidth();
        if(event.getX() > view_width) {
            return true;
        }
        return false;
    }
}

package com.estherhlai.kidsapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FloatingWidgetService extends Service {

    int LAYOUT_FLAG;
    View mFloatingView;
    WindowManager windowManager;
    ImageView imageClose;
    TextView textView;
    float height, width;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // inflate widget layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout_widget, null);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // initial position
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 100;

        // layout params for close button
        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(
                140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        imageParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        imageParams.x = 0;
        imageParams.y = 100;

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        imageClose = new ImageView(this);
        imageClose.setImageResource(R.drawable.close_white);
        imageClose.setVisibility(View.INVISIBLE);
        windowManager.addView(imageClose, imageParams);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        textView = (TextView)mFloatingView.findViewById(R.id.text_widget);

        // show & update current time in textview
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                handler.postDelayed(this, 1000);
            }
        }, 10);

        // drag movement for widget
        textView.setOnTouchListener(new View.OnTouchListener() {

            int initialX, initialY;
            float initialTouchX, initialTouchY;
            long startClickTime;

            int MAX_CLICK_DURATION = 200;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        imageClose.setVisibility(View.VISIBLE);
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        // touch positiion
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        imageClose.setVisibility(View.GONE);
                        layoutParams.x = initialX+(int)(initialTouchX-event.getRawX());
                        layoutParams.y = initialY+(int)(event.getRawY()-initialTouchY);
                        if (clickDuration < MAX_CLICK_DURATION) {
                            Toast.makeText(
                                    FloatingWidgetService.this,
                                    "Time: " + textView.getText().toString(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Remove widget
                            if (layoutParams.y > height*0.6) {
                                stopSelf();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // calculate x & y coordinates of view
                        layoutParams.x = initialX+(int)(initialTouchX-event.getRawX());
                        layoutParams.y = initialY+(int)(event.getRawY()-initialTouchY);
                        // update layout with new coordinates
                        windowManager.updateViewLayout(mFloatingView, layoutParams);
                        if (layoutParams.y > height*0.6) {
                            imageClose.setImageResource(R.drawable.close);
                        } else {
                            imageClose.setImageResource(R.drawable.close_white);
                        }
                        return true;
                }

                return false;
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
        }
        if (imageClose != null) {
            windowManager.removeView(imageClose);
        }
    }
}

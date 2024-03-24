package com.dcp.floater;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;

public class FloatingWindow extends Service {

    private WindowManager wm;
    private LinearLayout ll;
    private Button stopBtn;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        stopBtn = new Button(this);

        ViewGroup.LayoutParams btnParameters = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stopBtn.setText("Stop");
        stopBtn.setLayoutParams(btnParameters);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(66, 255, 0, 0));
        ll.setLayoutParams(llParameters);

        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400, 250,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE , PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = 0;
        parameters.gravity = Gravity.CENTER | Gravity.CENTER;

        ll.addView(stopBtn);
        wm.addView(ll, parameters);

        ll.setOnTouchListener(new View.OnTouchListener() {

            private  WindowManager.LayoutParams updatedParameters = parameters;
            int x, y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        touchedX = event.getRawX();
                        touchedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - touchedX);
                        int Ydiff = (int) (event.getRawY() - touchedY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {

                            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                            if (activityManager != null) {
                                List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(1);
                                if (!taskInfoList.isEmpty()) {
                                    ComponentName topActivity = taskInfoList.get(0).topActivity;
                                    if (topActivity != null && topActivity.getPackageName().equals(getApplicationContext().getPackageName())) {
                                        // If the app is already in the foreground, move it to the front
                                        int taskId = taskInfoList.get(0).id;
                                        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME);
                                    }
                                }
                            }

                            // Close the floating window
                            wm.removeView(ll);
                            stopSelf();

                        }

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));

                        wm.updateViewLayout(ll, updatedParameters);

                    default:
                        break;
                }

                return false;
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(ll);
                stopSelf();
            }
        });

    }
}

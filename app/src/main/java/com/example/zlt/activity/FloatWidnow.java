package com.example.zlt.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.permission.FloatWindowManager;
import com.example.zlt.utils.SPHelper;

/**
 * Created by zhulanting zwx414544 on 16/11/11.
 */
public class FloatWidnow {

    private static WindowManager windowManager;
    private static WindowManager.LayoutParams layoutParams;
    private static LayoutInflater inflater;
    private static View floatView;
    private static TextView pkName;
    private static TextView acName;

    private static boolean showing = false;

    public static void init(Context context) {
        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.type = WindowManager.LayoutParams .TYPE_SYSTEM_ERROR;
        layoutParams.format = PixelFormat.TRANSLUCENT;

        inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        floatView = inflater.inflate(R.layout.float_viewgroup, null);
        pkName = (TextView) floatView.findViewById(R.id.package_name);
        acName = (TextView) floatView.findViewById(R.id.activity_name);
    }

    public static void show(Context context, CharSequence packageName, CharSequence activityName) {

        if (!FloatWindowManager.getInstance().checkPermission(context)) {
            FloatWindowManager.getInstance().applyPermission(context);
            return;
        }

        if (windowManager == null) {
            init(context);
        }

        if (!showing) {
            windowManager.addView(floatView, layoutParams);
            showing = true;
        }

        String color = SPHelper.getTextColor(context);

        if (SPHelper.isShowPackageName(context)) {
            pkName.setVisibility(View.VISIBLE);
            pkName.setText(packageName);
            pkName.setTextColor(Color.parseColor(color));
        } else {
            pkName.setVisibility(View.GONE);
        }
        acName.setText(activityName);
        acName.setTextColor(Color.parseColor(color));
    }

    public static void dismiss() {
        if (showing) {
            windowManager.removeView(floatView);
            showing = false;
        }
    }
}

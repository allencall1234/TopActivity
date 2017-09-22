package com.example.zlt.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.TextUtilsCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.zlt.activity.FloatWidnow;
import com.example.zlt.utils.SPHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhulanting zwx414544 on 16/12/19.
 */
public class DetectionService extends AccessibilityService {
    private String packageName;
    private String activityName;

    private ActivityManager mActivityManager;
    public static final String ACTION_UPDATE_UI = "action_update_ui";
    private BroadcastReceiver receiver = new InnerBroadcastReceiver();

    private static final String CLASS_NAME_SUFFIX = ".VerifyPwdActivity";
    private static final String CLASS_DEFAULT_SUFFIX = ".VerifyPwdActivity";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("zlt", "onServiceConnected!");
        LocalBroadcastManager.getInstance(DetectionService.this).registerReceiver(receiver, new IntentFilter(ACTION_UPDATE_UI));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            String pkName = event.getPackageName().toString();
            String acName;
            if (event.getClassName() != null) {
                //解决对话框获取不到类名问题
                acName = event.getClassName().toString();
            } else {
                return;
            }

            updateUI(pkName, acName);

            if (SPHelper.isEnableAutoCompelete(DetectionService.this)) {
                //如果打开了自动填充开关,那么就启用自动填充功能
                autoComplete(acName);
            }

        }

    }

    private void autoComplete(String acName) {
        if (acName.endsWith(CLASS_NAME_SUFFIX) || acName.endsWith(CLASS_DEFAULT_SUFFIX)) {
            String id_corder_pwd = "com.giveu.corder:id/et_pwd";//com.giveu.corder:id/tv_userId
            String id_mall_pwd = "com.giveu.shoppingmall:id/et_pwd";//com.giveu.shoppingmall:id/tv_userId
            String id_corder_userId = "com.giveu.corder:id/tv_userId";
            String id_mall_userId = "com.giveu.shoppingmall:id/tv_userId";
            AccessibilityNodeInfo accessibilityNodeInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                accessibilityNodeInfo = getRootInActiveWindow();
            }

            if (accessibilityNodeInfo != null) {
                List<AccessibilityNodeInfo> nodeInfos = null;
                List<AccessibilityNodeInfo> userIdInfos = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    nodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id_corder_pwd);
                    userIdInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id_corder_userId);

                    if (nodeInfos == null || nodeInfos.size() == 0) {
                        nodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id_mall_pwd);
                    }

                    if (userIdInfos == null || userIdInfos.size() == 0) {
                        userIdInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id_mall_userId);
                    }

                    CharSequence userId = "";
                    CharSequence pwd = "";
                    if (userIdInfos != null && userIdInfos.size() != 0) {
                        userId = userIdInfos.get(0).getText();
                        String result = SPHelper.getAutoCompeleteText(DetectionService.this);
                        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(result)) {
                            Pattern pattern = Pattern.compile(userId + "::(.*?)(:|$)");
                            Matcher matcher = pattern.matcher(result);
                            while (matcher.find()) {
                                pwd = matcher.group(1);
                            }
                        }
                    }

                    if (nodeInfos != null && nodeInfos.size() != 0) {
                        CharSequence text = nodeInfos.get(0).getText();
                        Pattern pattern = Pattern.compile("\\d+");
                        if (TextUtils.isEmpty(text) ||
                                (!pattern.matcher(text).find() && !text.toString().startsWith("•"))) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("text", pwd);
                            clipboardManager.setPrimaryClip(clipData);
                            nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        }
                    }
                }
            }
        }
    }

    private void updateUI(String pkName, String acName) {
        if (SPHelper.isShowWindow(DetectionService.this)) {
            packageName = pkName;
            activityName = acName;
            FloatWidnow.show(DetectionService.this, packageName, activityName);
        } else {
            FloatWidnow.dismiss();
        }
    }

    @Override
    public void onInterrupt() {
        onDestroy();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(DetectionService.this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    class InnerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE_UI.equals(intent.getAction())) {
                if (packageName == null || activityName == null) {
                    List<ActivityManager.RunningTaskInfo> rtis = mActivityManager.getRunningTasks(1);
                    packageName = rtis.get(0).topActivity.getPackageName();
                    activityName = rtis.get(0).topActivity.getClassName();
                }
                updateUI(packageName, activityName);
            }
        }
    }
}

package com.michelle.accessibilityservice.service;

import android.view.accessibility.AccessibilityEvent;

/**
 * author Created by michelle on 2018/12/3.
 * email: 1031983332@qq.com
 * 在app应用的设置界面，点击，停止运行
 */

public class CleanBackgroundProcessAccessibilityService extends BaseAccessbilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getPackageName().equals("com.android.settings")) {
            CharSequence className = event.getClassName();
            if (className.equals("com.android.settings.applications.InstalledAppDetailsTop")) {
                findAndPerformActionButton("强行停止","强行停止");
            }
            if (className.equals("android.app.AlertDialog")) {
                findAndPerformActionButton("确定","确定");
                doBack();
            }
        }
    }
}

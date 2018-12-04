package com.michelle.accessibilityservice.util;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * author Created by michelle on 2018/11/29.
 * email: 1031983332@qq.com
 * log工具类
 */
public class PrintUtils {

    public static void log(CharSequence log) {
        Log.i("test", log + "");
    }

    /**
     * 打印AccessibilityEvent事件
     * @param event
     */
    public static void printEvent(AccessibilityEvent event) {
        log("-------------------------------------------------------------");
        int eventType = event.getEventType();
        log("packageName:" + event.getPackageName() + ""); //com.tencent.mm
        log("source:" + event.getSource() + "");
        log("source class:" + event.getClassName() + "");//activity
        log("event type(int):" + eventType + "");//32

        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                log("event type:TYPE_NOTIFICATION_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗体状态改变
                log("event type:TYPE_WINDOW_STATE_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED://View获取到焦点
                log("event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                log("event type:TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                log("event type:TYPE_GESTURE_DETECTION_END");
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                log("event type:TYPE_WINDOW_CONTENT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                log("event type:TYPE_VIEW_CLICKED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                log("event type:TYPE_VIEW_TEXT_CHANGED");
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                log("event type:TYPE_VIEW_SCROLLED");
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                log("event type:TYPE_VIEW_TEXT_SELECTION_CHANGED");
                break;
        }

        for (CharSequence txt : event.getText()) {
            log("text:" + txt);//微信，应用名
        }

        log("-------------------------------------------------------------");
    }
}

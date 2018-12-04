package com.michelle.accessibilityservice.service;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 微信运动自动点赞器
 */
public class WeChatMotionMonitor {
    private static String TAG="WeChatMotionMonitor.class";

    public static void policy(AccessibilityNodeInfo nodeInfo, String packageName, String className) {
        if (nodeInfo == null) {
            return;
        }

        if (false == "com.tencent.mm".equals(packageName)) {
            return;
        }

        // 该界面下所有 ViewId 节点   ，   com.tencent.mm:id/b6a为控件id
        List<AccessibilityNodeInfo> list = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b6a");
        }
        for (int i = 0; i < list.size() ; i++) {
            if (i == 0) {
                // 防止点赞自己，跳转到其他界面
                continue;
            }

            if (list.get(i).isClickable()) {
                list.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "clickBtnByResId = " + list.get(i).toString());
            }
        }
    }
}

package com.michelle.accessibilityservice.util;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class CommonUtil {
    private static String TAG="";


    /**
     * 查找界面是否包含id的组件，有的话点击该组件
     * @param nodeInfo
     * @param id
     */
    public static void clickBtnByResId(AccessibilityNodeInfo nodeInfo, String id) {
        if (nodeInfo != null) {
            // 该界面下所有 ViewId 节点
            List<AccessibilityNodeInfo> list = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
            }
            for (AccessibilityNodeInfo item : list) {
                if (item.isClickable()) {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "clickBtnByResId = " + item.toString());
                    break;
                }
            }
        }
    }
}

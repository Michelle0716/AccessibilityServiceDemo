package com.michelle.accessibilityservice.service;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;


import java.util.List;

/**
 * 对keep进行点赞
 */
public class KeepAppMonitor {
    private  static  String TAG="KeepAppMonitor.class";
    public static void policy(AccessibilityNodeInfo nodeInfo, String packageName, String className) {
        if (!("com.gotokeep.keep".equals(packageName) &&
                "android.support.v7.widget.RecyclerView".equals(className))) {
            return;
        }

        // 关注界面的点赞
        keepAppPraise(nodeInfo,"com.gotokeep.keep:id/item_cell_praise_container");

        // 好友界面的点赞
        keepAppPraise(nodeInfo,"com.gotokeep.keep:id/stroke_view");

        // 热点界面的点赞
        keepAppPraise(nodeInfo,"com.gotokeep.keep:id/layout_like");
    }

    /**
     * 查找是否有控件ID，如果有就进行点击
     * @param nodeInfo
     * @param id
     */
    public static void keepAppPraise(AccessibilityNodeInfo nodeInfo, String id) {
        if (nodeInfo != null) {
            // 该界面下所有 ViewId 节点
            List<AccessibilityNodeInfo> list = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
                for (AccessibilityNodeInfo item : list) {
                    if (item.isClickable()) {
                        Log.d(TAG, "keepAppPraise = " + item.getClassName());
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }

        }
    }
}

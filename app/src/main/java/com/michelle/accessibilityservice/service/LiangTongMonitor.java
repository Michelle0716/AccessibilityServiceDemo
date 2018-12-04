package com.michelle.accessibilityservice.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.michelle.accessibilityservice.util.CommonUtil;


/**
 * 手机联通营业厅自动签到器
 */
public class LiangTongMonitor {

    private static String TAG=" LiangTongMonitor.class";

    /**
     * 联通
     * @param mContext
     */
    public static void startLiangTongUI(Context mContext) {
        Log.e(TAG,"startLiangTongUI");
        Intent intent = new Intent();
        intent.setPackage("com.sinovatech.unicom.ui");
        intent.setClassName("com.sinovatech.unicom.ui", "com.sinovatech.unicom.basic.ui.MainActivity");
        mContext.startActivity(intent);
    }

    /**
     * 跳转到签到界面
     * @param nodeInfo
     * @param packageName
     * @param className
     */
    public static void startLiangTongQianDaoUI(AccessibilityNodeInfo nodeInfo, String packageName, String className) {
        Log.e(TAG,"startLiangTongQianDaoUI");

        if (nodeInfo == null) {
            return;
        }

        if (false == "com.sinovatech.unicom.ui".equals(packageName)) {
            return;
        }

        CommonUtil.clickBtnByResId(nodeInfo, "com.sinovatech.unicom.ui:id/home_header_long_qiandao_image");
    }

    /**
     * 点击自动签到
     *
     * @param nodeInfo
     * @param packageName
     * @param className
     */
    public static void policy(AccessibilityNodeInfo nodeInfo, String packageName, String className) {
        Log.e(TAG,"policy");

        if (nodeInfo == null) {
            return;
        }

        if ("com.sinovatech.unicom.ui".equals(packageName)) {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo child = nodeInfo.getChild(i);
                if ("android.webkit.WebView".equals(child.getClassName())) {
                    Log.d(TAG, "nodeInfo = " + nodeInfo.toString());
                    findEveryViewNode(child);
                    break;
                }
            }
        }
    }

    public static void findEveryViewNode(AccessibilityNodeInfo node) {
        Log.e(TAG,"findEveryViewNode");

        if (null != node && node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child =  node.getChild(i);
                // 有时 child 为空
                if (child == null) {
                    continue;
                }

                String className = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    className = child.getViewIdResourceName();
                }
                if ("qd_xq".equals(className)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Log.d(TAG, "Button 的节点数据 text = " + child.getText() + ", descript = " + child.getContentDescription() + ", className = " + child.getClassName() + ", resId = " + child.getViewIdResourceName());
                    }

                    boolean isClickable = child.isClickable();

                    if ( isClickable) {
                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d(TAG, "联通签到 成功点击");
                    }
                }

                // 递归调用
                findEveryViewNode(child);
            }
        }
    }
}

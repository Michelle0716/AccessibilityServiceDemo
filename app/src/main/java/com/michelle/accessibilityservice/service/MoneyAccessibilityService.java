package com.michelle.accessibilityservice.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * author Created by michelle on 2018/12/3.
 * email: 1031983332@qq.com
 * 抢红包
 */

public class MoneyAccessibilityService extends BaseAccessbilityService {
    private static final String TAG = "MyAccessibilityService";
    private static final String OPEN_BUTTON_ID = "com.tencent.mm:id/bi3";
    private static final String BACK_BUTTON_ID = "com.tencent.mm:id/gv";
    private static final String SLOW_TEXT_ID = "com.tencent.mm:id/bg6";
    private List<AccessibilityNodeInfo> parents;


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
        Log.d(TAG, "onServiceConnected:  timeOut: " + getTimeOut());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
//          通知栏发生变化
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.d(TAG, "onAccessibilityEvent: Notification " + content);
                        if (content.contains("[微信红包]")) {
                            if (event.getParcelableData() != null &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    isAccessibility = true;
                                    Log.d(TAG, "onAccessibilityEvent: start Accessibility");
                                    wakeAndUnlock();
                                    pendingIntent.send();
//                                  如果开启抢红包之前就处在LauncherUI则不会回调WindowChange,
//                                  2秒后手动getLastPacket();如果之前不处在LauncherUI，
//                                  pendingIntent.send()之后会回调onAccessibilityEvent，
//                                  2秒sleep之后就执行不到。（why？）
                                    try {
                                        Thread.sleep(2000);
                                        Log.d(TAG, "onAccessibilityEvent: 手动getLastPacket()");
                                        getLastPacket();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
//            窗口状态发生变化
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d(TAG, "onAccessibilityEvent: TYPE_WINDOWS_CHANGED" + event.getClassName().toString());
                if (!isAccessibility) {
                    return;
                }
                String className = event.getClassName().toString();
                switch (className) {
                    case "com.tencent.mm.ui.LauncherUI":
                        Log.d(TAG, "自动 getLastPacket()");
                        getLastPacket();
                        break;
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                        //手慢了
                        nodeInfosByViewIdClick(OPEN_BUTTON_ID);
                        Log.d(TAG, "onAccessibilityEvent: 开红包");
                        slow(SLOW_TEXT_ID);
                        break;
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                        nodeInfosByViewIdClick(BACK_BUTTON_ID);
                        Log.d(TAG, "onAccessibilityEvent: 退出红包");
                        isAccessibility = false;
                        doBack();
                        autoLock();
                        Log.d(TAG, "onAccessibilityEvent: stop Accessibility");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * 点击最后一个红包
     */
    public void getLastPacket() {

        try {
            Thread.sleep(getTimeOut());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getLastPacket: 进入微信");
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        parents.clear();
        recycle(rootNode);
        if (parents.size() > 0) {
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.d(TAG, "onAccessibilityEvent: 点击最后一个红包");
        } else {
            Log.d(TAG, "getLastPacket: 没有红包");
            doBack();
            isAccessibility = false;
            autoLock();
        }
    }


    /**
     * 重新获取本页数据
     * @param rootNode
     */
    private void recycle(AccessibilityNodeInfo rootNode) {
//        把本页面红包全部存进 parents list
        if (rootNode.getChildCount() == 0) {
            //一个
            if (rootNode.getText() != null) {
                if ("领取红包".equals(rootNode.getText().toString())) {
                    if (rootNode.isClickable()) {
                        rootNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = rootNode.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            Log.d(TAG, "recycle: list add parent");
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            //多个红包
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                if (rootNode.getChild(i) != null) {
                    recycle(rootNode.getChild(i));
                }
            }
        }
    }


    /**
     * @param id
     */
    private void slow(String id) {
//        手慢了情况下
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
                for (AccessibilityNodeInfo accessibilityNodeInfo : list) {
                    String s = accessibilityNodeInfo.getText().toString();
                    if (s.contains("手慢了")) {
                        Log.d(TAG, "slow: 手慢了");
                        doBack();
                        doBack();
                        autoLock();
                        isAccessibility = false;
                        Log.d(TAG, "slow: stop Accessibilty");
                    }
                }
            }

        }
    }


}
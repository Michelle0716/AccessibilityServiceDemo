package com.michelle.accessibilityservice.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.List;

import com.michelle.accessibilityservice.util.PrintUtils;


/**
 * author Created by michelle on 2018/12/3.
 * email: 1031983332@qq.com
 *
 * AccessibilityService实际是一个过程：查找界面是否含有我们的目标对象，然后对目标对象进行操作
 * 如，搜索，跳转到对应类，查找"搜索"关键控件，对输入框进行粘贴，点击搜索
 * 如，抢红包L：黑屏状态下有红包通知，自动亮屏-解锁(无密码的屏锁)-抢红包-锁屏
 * 如，自动点赞，自动回复
 */




public class BaseAccessbilityService extends AccessibilityService {

    private String TAG = "BaseAccessbilityService.class";
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private KeyguardManager km;
    public boolean locked = false;
    private boolean isMeUnLock = false;
    public boolean isAccessibility = false;

    private AccessibilityManager mAccessibilityManager;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        PrintUtils.printEvent(event);
    }

    @Override
    public void onInterrupt() {
        PrintUtils.log("onInterrupt");
    }

    @Override
    protected boolean onGesture(int gestureId) {
        PrintUtils.log("onGesture");
        return super.onGesture(gestureId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        PrintUtils.log("onServiceConnected");
        mAccessibilityManager = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

//        //可用代码配置当前Service的信息
//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        info.packageNames = new String[]{"com.android.packageinstaller", "com.tencent.mobileqq", "com.trs.gygdapp"}; //监听过滤的包名
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; //监听哪些行为
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN; //反馈
//        info.notificationTimeout = 100; //通知的时间
//        setServiceInfo(info);
    }

    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    public boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 唤醒屏幕，解锁
     */
    public void wakeAndUnlock() {
        //获取电源管理器对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");


        if (!pm.isScreenOn()) {
            //点亮屏幕
            wl.acquire(1000);
            Log.d(TAG, "onAccessibilityEvent: 亮屏");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (km.isDeviceLocked()) {
                Log.d(TAG, "autoUnlock: sdk >= 22: 屏幕被密码锁柱");
                wl.release();
            } else {
                if (km.inKeyguardRestrictedInputMode()) {
                    //解锁
                    kl.disableKeyguard();
                    Log.d(TAG, "onAccessibilityEvent: 尝试解锁");
                    isMeUnLock = true;
                }
            }
        }


    }


    /**
     * 自动锁屏
     */
    public void autoLock() {
        if (isMeUnLock) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            kl.reenableKeyguard();
            Log.d(TAG, "autoLock: 自动锁");
            if (wl != null && pm.isScreenOn()) {
                wl.release();
                Log.d(TAG, "autoLock: 自动灭");
            }
            isMeUnLock = false;
        }
    }


    /**
     * 释放控件
     */
    public void release() {
        if (isScreenLocked() && kl != null) {
            android.util.Log.d("maptrix", "release the lock");
            //得到键盘锁管理器对象
            kl.reenableKeyguard();
            locked = false;
        }
    }


    /**
     * 模拟back按键
     */
    public void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 返回
     */
    public void doBack() {
        try {
            Thread.sleep(getTimeOut());
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        } catch (InterruptedException e) {
            e.printStackTrace();
            isAccessibility = false;
        }
    }



    /**
     * 模拟下滑操作
     */
    public void performScrollBackward() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * 模拟上滑操作
     */
    public void performScrollForward() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }







    /**
     * 回到系统桌面
     */
    public void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);

        startActivity(home);
    }


    /**
     * 将当前应用运行到前台
     */
    public void bring2Front() {
        ActivityManager activtyManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (this.getPackageName().equals(runningTaskInfo.topActivity.getPackageName())) {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }

    /**
     * 判断指定的应用是否在前台运行
     *
     * @param packageName
     * @return
     */
    public boolean isAppForeground(String packageName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
            return true;
        }

        return false;
    }


    /**
     * 超时
     *
     * @return
     */
    public long getTimeOut() {
        SharedPreferences sp = getSharedPreferences("timeOut", MODE_PRIVATE);
        long timeOUt = sp.getLong("timeOut", 0);
        return timeOUt > 0 ? timeOUt : 500;
    }


    /**
     * 在屏幕上查询是否存在关键字为text的Button控件，
     * 如果有的话，执行点击动作
     *
     * @param text
     */
    public boolean findAndPerformActionButton(String text,String text2) {
        if (getRootInActiveWindow() == null)//取得当前激活窗体的根节点
        {
            return false;
        }
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //通过文字找到当前的节点
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);//中文
        if(nodes!=null&&nodes.size()>0){
            for (int i = 0; i < nodes.size(); i++) {
                AccessibilityNodeInfo node = nodes.get(i);
                // 执行按钮点击行为
                if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }else {
            List<AccessibilityNodeInfo> liste = nodeInfo
                    .findAccessibilityNodeInfosByText(text2);//英文
            if (liste != null && liste.size() > 0) {
                for (AccessibilityNodeInfo n : liste) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        performViewClick(n);

                    }
                }
            }
        }

        return  true;
    }


    /**
     * 点击问及
     * 在屏幕上查询是否存在关键字为text的TextView控件，
     * 如果有的话，执行点击动作
     * @param text
     */
    public void findAndPerformActionTextView(String text) {
        if (getRootInActiveWindow() == null)
            return;
        //通过文字找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
            // 执行按钮点击行为
            if (node.getClassName().equals("android.widget.TextView") && node.isEnabled()) {
                performViewClick(node);
            }
        }
    }


    /**
     * 点击ID
     * @param clickId
     */
  public void nodeInfosByViewIdClick(String clickId) {
//        点击指定id Node
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
                for (AccessibilityNodeInfo accessibilityNodeInfo : list) {
                    performViewClick(accessibilityNodeInfo);
                }
            }

        }
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }



    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    private boolean checkAccessibilityEnabled(String serviceName) {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public void goAccess(Context mContext) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


}

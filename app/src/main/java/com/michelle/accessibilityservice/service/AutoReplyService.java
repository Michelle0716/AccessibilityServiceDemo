package com.michelle.accessibilityservice.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import com.michelle.accessibilityservice.util.PrintUtils;

/**
 * author Created by michelle on 2018/11/29.
 * email: 1031983332@qq.com
 */
public class AutoReplyService extends BaseAccessbilityService {
    private final static String MM_PNAME = "com.tencent.mm";
    // private final static String MM_PNAME = "com.tencent.mobileqq";
    boolean hasAction = false;//是否接收到了通知栏信息，已经执行了sendNotifacationReply
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;

    private Handler handler = new Handler();


    /**
     * 必须重写的方法，响应各种事件。
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        PrintUtils.printEvent(event);
        int eventType = event.getEventType();
        android.util.Log.d("maptrix", "get event = " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件64
                android.util.Log.d("maptrix", "get notification event");
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        android.util.Log.d("maptrix", "事件+content：" + content);

                        if (!TextUtils.isEmpty(content)) {
                            if (isScreenLocked()) {
                                //锁屏
                                locked = true;
                                wakeAndUnlock();
                                android.util.Log.d("maptrix", "the screen is locked");
                                if (isAppForeground(MM_PNAME)) {
                                    //当前就在腾讯界面
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendNotifacationReply(event);
                                            if (fill()) {
                                                //发送自动回复
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                }
                            } else {
                                //未锁屏
                                locked = false;
                                android.util.Log.d("maptrix", "the screen is unlocked");
                                // 监听到微信红包的notification，打开通知
                                if (isAppForeground(MM_PNAME)) {
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fill()) {
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    //大多数情况
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                //界面改变事件32
                //当前手机在微信界面，则不会发送通知，而是屏幕出现消息
                android.util.Log.d("maptrix", "get type window down event");
                if (!hasAction) {
                    break;
                }
                itemNodeinfo = null;
                String className = event.getClassName().toString();
                android.util.Log.d("maptrix", "className +： " + className);
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    //启动页面
                    if (fill()) {
                        send();
                    } else {
                        if (itemNodeinfo != null) {
                            itemNodeinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (fill()) {
                                        send();
                                    }
                                    android.util.Log.d("maptrix", "窗体改变事件1release()+： ");

//                                    back2Home();
//                                    release();
                                    hasAction = false;
                                }
                            }, 1000);
                            break;
                        }
                    }
                }

//                bring2Front();
//                back2Home();
//                release();
                hasAction = false;
                break;
        }
    }

    private void send() {
        android.util.Log.i("maptrix", "发送send调用");//通知栏内容：联系人+信息

    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void sends() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            findAndPerformActionButton("发送","Send");
        }
        //如果没有发送按钮，就返回上一页
        pressBackButton();
    }


    /**
     * 获取通知栏信息，并尝试打开
     *
     * @param event
     */
    private void sendNotifacationReply(AccessibilityEvent event) {
        hasAction = true;
        if (event.getParcelableData() != null
                && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event
                    .getParcelableData();
            String content = notification.tickerText.toString();
            android.util.Log.i("maptrix", "content =" + content);//通知栏内容：联系人+信息

            String[] cc = content.split(":");
            name = cc[0].trim();
            scontent = cc[1].trim();

            android.util.Log.i("maptrix", "sender name =" + name);//联系人
            android.util.Log.i("maptrix", "sender content =" + scontent);//联系人所发内容


            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();//打开通知栏的通知链接=========相当于点击通知栏的通知 ！！！！
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否输入框是否有数据" 正在忙,稍后回复你   "
     *
     * @return
     */
    private boolean fill() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        //查找，屏幕是否含有该字样
        //     List<AccessibilityNodeInfo> wxList = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (rootNode != null) {
            return findEditText(rootNode, "正在忙,稍后回复你");
        }
        return false;
    }

    /**
     * 把 content，粘贴到与通知栏好友聊天的输入框
     *
     * @param rootNode 存储控件实例的集合
     * @param content
     * @return
     */
    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();

        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);//控件
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }

            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());//控件，即文本或图片
            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());//控件的描述，如imageview某个用户头像
            if (nodeInfo.getContentDescription() != null) {
                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);//查找通知栏的联系人
                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
                android.util.Log.e("maptrix", "nindex=" + nindex + " cindex=" + cindex);
                if (nindex != -1) {
                    itemNodeinfo = nodeInfo;
                    android.util.Log.i("maptrix", "find node info");
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                //当前的控件是输入框类
                android.util.Log.i("maptrix", "==================");
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

                //复制，描述和内容
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);//复制
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {

    }


}
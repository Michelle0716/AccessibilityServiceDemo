package com.michelle.accessibilityservice.service;

import android.view.accessibility.AccessibilityEvent;

import com.michelle.accessibilityservice.util.PrintUtils;

/**
 * author Created by michelle on 2018/11/29.
 * email: 1031983332@qq.com
 * 自动安装
 * 一般应用在，下载完后，自动安装最新应用
 */
public class AutoInstallService extends BaseAccessbilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        PrintUtils.printEvent(event);
        findAndPerformActionButton("继续","继续");
        findAndPerformActionTextView("下一步");
        findAndPerformActionTextView("安装");
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        PrintUtils.log("onServiceConnected");
    }

    @Override
    public void onInterrupt() {
        PrintUtils.log("onInterrupt");
    }


}

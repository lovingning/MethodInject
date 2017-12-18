package com.knowledge.mnlin.test;

import android.app.Application;

import com.knowledge.mnlin.methodinject.MethodInjectActivity;

/**
 * Created on 2017/12/18
 * function :
 *
 * @author ACChain
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //关闭methodinject自动初始化
        MethodInjectActivity.setAutoInject(false);
    }
}

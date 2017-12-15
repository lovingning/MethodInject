package com.knowledge.mnlin.methodinject;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created on 2017/12/12
 * function :
 *
 * @author ACChain
 */

public class ActivityDoNothing implements ActivityLifeCycle {
    /**
     * 静态成员
     * <p>
     * 若只有一个类加载器,则单例可保持
     */
    private static ActivityDoNothing singleInstance = new ActivityDoNothing();

    /**
     * 保持单例
     */
    protected ActivityDoNothing() {

    }

    public static ActivityDoNothing getInstance() {
        return singleInstance;
    }

    @Override
    public void before_onCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void after_onCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void before_onPostCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void after_onPostCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void before_onResume() {

    }

    @Override
    public void after_onResume() {

    }

    @Override
    public void before_onPostResume() {

    }

    @Override
    public void after_onPostResume() {

    }

    @Override
    public void before_onRestart() {

    }

    @Override
    public void after_onRestart() {

    }

    @Override
    public void before_onStart() {

    }

    @Override
    public void after_onStart() {

    }

    @Override
    public void before_onPause() {

    }

    @Override
    public void after_onPause() {

    }

    @Override
    public void before_onStop() {

    }

    @Override
    public void after_onStop() {

    }

    @Override
    public void before_onDestroy() {

    }

    @Override
    public void after_onDestroy() {

    }
}

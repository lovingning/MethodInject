package com.knowledge.mnlin.methodinject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.knowledge.mnlin.methodinject_annotations.annotations.RootActivity;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;

/**
 * Created on 2017/12/12
 * function : MethodInject框架
 *
 * @author ACChain
 */
@RootActivity
public abstract class MethodInjectActivity extends AppCompatActivity {
    /**
     * 控制框架是否在开启时就自动进行赋值
     */
    private static boolean autoInject = true;

    private final String TAG = getClass().getSimpleName();

    /**
     * 生命周期处理方法
     */
    private ActivityLifeCycle methodManager;

    /*动态代码块,用于为methodManager赋值*/ {
        if (autoInject) {
            Logger.v("自动注入对象");
            try {
                Class clazz = Class.forName(getClass().getCanonicalName() + "_MethodInject");
                if (clazz != null) {
                    Method method_getInstance = clazz.getMethod("getInstance", this.getClass());
                    methodManager = (ActivityLifeCycle) method_getInstance.invoke(clazz, this);
                    Logger.v("实例创建成功");
                }
            } catch (Exception e) {
                methodManager = ActivityDoNothing.getInstance();
                Logger.v("实例创建失败");
            }
        }
    }/*若未生成对象,说明子类没有需要调用的方法,因此返回无操作对象*/

    /**
     * 判断当前是否是自动赋值
     */
    public static boolean isAutoInject() {
        return autoInject;
    }

    /**
     * 设置是否自动赋值
     */
    public static void setAutoInject(boolean autoInject) {
        MethodInjectActivity.autoInject = autoInject;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.v("%s  :  onCreate: ", TAG);
        methodManager.before_onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        methodManager.after_onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Logger.v("%s  :  onPostCreate: ", TAG);
        methodManager.before_onPostCreate(savedInstanceState);
        super.onPostCreate(savedInstanceState);
        methodManager.after_onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Logger.v("%s  :  onResume: ", TAG);
        methodManager.before_onResume();
        super.onResume();
        methodManager.after_onResume();
    }

    @Override
    protected void onPostResume() {
        Logger.v("%s  :  onPostResume: ", TAG);
        methodManager.before_onPostResume();
        super.onPostResume();
        methodManager.after_onPostResume();
    }

    @Override
    protected void onRestart() {
        Logger.v("%s  :  onRestart: ", TAG);

        methodManager.before_onRestart();
        super.onRestart();
        methodManager.after_onRestart();
    }

    @Override
    protected void onStart() {
        Logger.v("%s  :  onStart: ", TAG);
        methodManager.before_onStart();
        super.onStart();
        methodManager.after_onStart();
    }

    @Override
    protected void onPause() {
        Logger.v("%s  :  onPause: ", TAG);
        methodManager.before_onPause();
        super.onPause();
        methodManager.after_onPause();
    }

    @Override
    protected void onStop() {
        Logger.v("%s  :  onStop: ", TAG);
        methodManager.before_onStop();
        super.onStop();
        methodManager.after_onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.v("%s  :  onDestroy: ", TAG);
        methodManager.before_onDestroy();
        super.onDestroy();
        methodManager.after_onDestroy();
    }

    /**
     * 设置该方法后,将替换已存在的MethodLifecycle对象
     */
    protected final void replaceMethodInject(@Nullable ActivityLifeCycle methodManager) {
        this.methodManager = methodManager == null ? ActivityDoNothing.getInstance() : methodManager;
    }
}

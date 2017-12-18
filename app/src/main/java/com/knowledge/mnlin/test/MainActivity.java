package com.knowledge.mnlin.test;

import android.os.Bundle;
import android.util.Log;

import com.knowledge.mnlin.methodinject_annotations.annotations.ActivityInject;
import com.knowledge.mnlin.methodinject_annotations.annotations.MethodInject;
import com.knowledge.mnlin.methodinject_annotations.enums.LifeCycleMethod;

@ActivityInject
public class MainActivity extends BaseActivity {

    {
        //不能在onCreate方法中调用，因为有可能之后会添加其他方法，有可能onCreate方法执行之前就需要该成员

        replaceMethodInject(MainActivity_MethodInject.getInstance(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @MethodInject(method = LifeCycleMethod.onPostCreate, priority = 4, inject = BuildConfig.DEBUG_INJECT_METHOD)
    void f1() {
        Log.e("f1:", "执行了");
    }
}

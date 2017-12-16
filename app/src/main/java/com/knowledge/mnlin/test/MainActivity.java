package com.knowledge.mnlin.test;

import android.os.Bundle;
import android.util.Log;

import com.knowledge.mnlin.methodinject.MethodInjectActivity;
import com.knowledge.mnlin.methodinject_annotations.annotations.ActivityInject;
import com.knowledge.mnlin.methodinject_annotations.annotations.MethodInject;
import com.knowledge.mnlin.methodinject_annotations.enums.LifeCycleMethod;

@ActivityInject
public class MainActivity extends MethodInjectActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @MethodInject(method = LifeCycleMethod.onPostCreate, priority = 4, inject = false)
    void f1() {
        Log.e("f1:", "执行了");
    }
}

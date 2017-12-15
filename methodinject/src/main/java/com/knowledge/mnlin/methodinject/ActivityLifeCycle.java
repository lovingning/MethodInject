package com.knowledge.mnlin.methodinject;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created on 2017/12/12
 * function :activity 声明周期方法
 *
 * @author ACChain
 */

public interface ActivityLifeCycle {

    void before_onCreate(@Nullable Bundle savedInstanceState);

    void after_onCreate(@Nullable Bundle savedInstanceState);


    void before_onPostCreate(@Nullable Bundle savedInstanceState);

    void after_onPostCreate(@Nullable Bundle savedInstanceState);


    void before_onResume();

    void after_onResume();


    void before_onPostResume();

    void after_onPostResume();


    void before_onRestart();

    void after_onRestart();


    void before_onStart();

    void after_onStart();


    void before_onPause();

    void after_onPause();


    void before_onStop();

    void after_onStop();


    void before_onDestroy();

    void after_onDestroy();
}

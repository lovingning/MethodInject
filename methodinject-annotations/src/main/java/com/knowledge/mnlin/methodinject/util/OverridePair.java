package com.knowledge.mnlin.methodinject.util;


import com.knowledge.mnlin.methodinject.annotations.MethodInject;

import javafx.util.Pair;

/**
 * Created on 2017/12/14
 * function : 保持一个全局单例的pair对象,用于 插入 与删除对象
 *
 * @author ACChain
 */

public class OverridePair {
    private static Pair<String, MethodInject> instance = new Pair<String, MethodInject>(".", OverridePriority.getInstance());

    public static Pair<String, MethodInject> getInstance() {
        return instance;
    }

    private OverridePair() {

    }
}

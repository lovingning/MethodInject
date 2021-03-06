package com.knowledge.mnlin.methodinject_annotations.util;

import com.knowledge.mnlin.methodinject_annotations.annotations.MethodInject;
import com.knowledge.mnlin.methodinject_annotations.enums.LifeCycleMethod;
import com.knowledge.mnlin.methodinject_annotations.enums.MethodInjectType;

import java.lang.annotation.Annotation;

/**
 * Created on 2017/12/13
 * function : 空权限,用于方便插入处理
 *
 * @author ACChain
 */

public final class OverridePriority implements MethodInject {
    private static MethodInject instance = new OverridePriority();

    private OverridePriority() {

    }

    public static MethodInject getInstance() {
        return instance;
    }

    @Override
    public LifeCycleMethod method() {
        return null;
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public boolean inject() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return MethodInject.class;
    }

    @Override
    public MethodInjectType type() {
        return null;
    }
}

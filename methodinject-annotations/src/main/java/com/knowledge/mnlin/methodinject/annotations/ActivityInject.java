package com.knowledge.mnlin.methodinject.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 小任
 * @date 2017/12/8
 * version 1.0
 * 描述:
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ActivityInject {
}

package com.knowledge.mnlin.methodinject_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2017/12/13
 * function :
 *
 * @author ACChain
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface MethodInject_CreateFile {
}

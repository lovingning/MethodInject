package com.knowledge.mnlin.methodinject_annotations.enums;

/**
 * Created on 2017/12/12
 * <p>
 * function : 方法注入的方式:在前,在后,覆盖
 * <p>
 * 在前则在对应的方法前调用
 * 在后则在对应的方法后调用
 * 覆盖的话,则会将权限低于自身的所有调用移除;
 * <p>
 * 覆盖只可以屏蔽自定义的一些方法,但super方法会保持,因为所有的声明周期方法必须由原系统进行一定的处理
 *
 * @author ACChain
 */

public enum MethodInjectType {
    /**
     * 之前
     */
    BEFORE,
    /**
     * 之后
     */
    AFTER,
    /**
     * 覆盖自定义方法,并进行重写
     */
    OVERRIDE;
}

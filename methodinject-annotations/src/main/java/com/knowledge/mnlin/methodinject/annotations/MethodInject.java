package com.knowledge.mnlin.methodinject.annotations;

import com.knowledge.mnlin.methodinject.enums.LifeCycleMethod;
import com.knowledge.mnlin.methodinject.enums.MethodInjectType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 小任
 * @date 2017/12/7
 * version 1.0
 * 描述:
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MethodInject {
    /**
     * 需要在哪个方法里面调用
     *
     * @see LifeCycleMethod
     */
    LifeCycleMethod method() default LifeCycleMethod.onCreate;

    /**
     * @deprecated
     *
     * 权限管理为全局操作, 即子类方法f1权限为1, 父类方法f2权限为2, 则方法执行顺序为:
     * obj.f1();
     * obj.f2();
     * 若父类子类方法权限相同,则默认: 权限小于5的情况下子类方法先执行;权限大于5的情况下子类方法后执行
     *
     */

    /**
     * 方法调用的权限
     * <p>
     * 取值为1-9
     * <p>
     * 1表示最高权限,1-4表示需要在super()方法之前调用
     * 9表示最低权限,6-9表示需要在super()方法之后调用
     * <p>
     * 5表示采取覆盖操作,即覆盖原有的super,目前暂不支持
     * <p>
     * 若子类和父类定义了方法名相同的方法,在子类对象上,是无法去调用父类同名方法的,这也符合java继承重载与覆盖的原理
     * <p>
     * 权限越高越先执行
     */
    int priority() default 9;

    /**
     * 是否要注入,如果为false,则默认进行添加操作
     * <p>
     * 可用于debug模式和release模式的单独处理
     */
    boolean inject() default true;

    /**
     * 方法已何种方式注入
     *
     * @see MethodInjectType
     */
//    @Deprecated
//    MethodInjectType type() default MethodInjectType.OVERRIDE;
}

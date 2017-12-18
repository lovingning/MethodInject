# MethodInject
[![License](https://img.shields.io/aur/license/yaourt.svg)](http://www.gnu.org/licenses/gpl-3.0.html)

Method注解处理器,主要功能:

 * 在子类中定义方法,添加MethodInject注解,可以保证在设定的生命周期内执行
 * 可全局管理被注解的方法,可拦截自定义方法的执行
 * 可以不进行额外的代码配置，每次创建activity只反射调用一次；也可以重写载入方法，实现无发射调用
  
## 1、 注解处理器实现
实现注解处理器，可以根据已有的java类生成相应的配置类，来执行相应方法。整个内容分为三部分：

 1. 注解module [![methodinject-annotations](https://api.bintray.com/packages/lovingning/maven/methodinject-annotations/images/download.svg) ](https://bintray.com/lovingning/maven/methodinject-annotations/_latestVersion) ：定义框架使用的所有注解内容，包括注解类中使用的枚举常量以及某些需要的工具类
 2. 处理器module [![methodinject-compiler](https://api.bintray.com/packages/lovingning/maven/methodinject-compiler/images/download.svg) ](https://bintray.com/lovingning/maven/methodinject-compiler/_latestVersion) ：用于在代码编译期间新生成java或class类，协助程序进行处理
 3. 框架module [![methodinject](https://api.bintray.com/packages/lovingning/maven/methodinject/images/download.svg) ](https://bintray.com/lovingning/maven/methodinject/_latestVersion) ：即正常框架使用时需要的代码部分
 
三者关系如下：

 1. 注解module：可单独存在，不依赖任何框架，主要用于向其他两者提供所需的class类
 2. 处理器module：依赖注解module，根据当前的java源码生成其他的java或者class类，节省编写代码时间
 3. 框架module：依赖注解module，在应用真正运行时作用。
 
注解module的处理逻辑可以通过 [**MethodInjectProcessor**](https://github.com/lovingning/MethodInject/blob/master/methodinject-compiler/src/main/java/com/knowledge/mnlin/methodinject_compiler/MethodJnjectProcessor.java)类的 **process** 查看：

`````
/**
 * 具体的注解处理类
 */
@Override
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // 获取 ActivityInject 注解标记的类与接口

    /*如果element是标注于activity(class)之上,则保留;否则即便时enum,interface,@interface,也删除不进行处理;*/

    //针对所有的被标注的activity进行树形遍历,保证他们的继承关系

    //获取根节点

    //设定MethodInjectActivity为根节点

    //去除activities中错误标记的element

    //将activities按照树的模型排列

    //根据实际的结构生成***Activity_MethodInject类,抽象类除外

    return true;
}
`````
 
## 2、使用方法

#### 1、改变项目中activity继承关系

框架要求项目中需要注解的activity必须为[**MethodInjectActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject/src/main/java/com/knowledge/mnlin/methodinject/MethodInjectActivity.java)的子类；

```
public class BaseActivity extends MethodInjectActivity {

}
```

**或者：**

将[**MethodInjectActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject/src/main/java/com/knowledge/mnlin/methodinject/MethodInjectActivity.java)中代码**整体复制**到当前定义的“**BaseActivity**”中，然后在**BaseActivity**上添加注解 [**@RootActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject-annotations/src/main/java/com/knowledge/mnlin/methodinject_annotations/annotations/RootActivity.java)

#### 2、标注activity

在有方法需要父类注入的activity上添加注解 [**@ActivityInject**](https://github.com/lovingning/MethodInject/blob/master/methodinject-annotations/src/main/java/com/knowledge/mnlin/methodinject_annotations/annotations/MethodInject.java)

```
//在类上添加注解
@ActivityInject
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

只有如此，注解处理器才能发现该类需要进行额外处理，才能生成对应的 **$_MethodInject类** 

#### 3、新建需要注入的方法

加入现在有个方法 **f1()** 需要在 **Activity** 的 **onResume** 的时候执行，则可以定义如下方法,并在方法上添加注解 [**@MethodInject**](https://github.com/lovingning/MethodInject/blob/master/methodinject-annotations/src/main/java/com/knowledge/mnlin/methodinject_annotations/annotations/MethodInject.java)：

````
@ActivityInject
public class MainActivity extends BaseActivity {

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
````

这个方法就表示：

 1. 需要在 onResume 的时候被调用;
 2. 该方法权限为4; (具体权限含义将在下方声明)
 3. 该方法是否需要注入; (该字段在某些情况下有特殊使用，之后会进行说明)
 
##### 1、method 字段
method 字段是枚举值，即为枚举类 [**LifeCycleMethod**](https://github.com/lovingning/MethodInject/blob/master/methodinject-annotations/src/main/java/com/knowledge/mnlin/methodinject_annotations/enums/LifeCycleMethod.java)的某个实例；根据该类源码查看详细说明；

注：该枚举类中包含了常用的一些声明周期方法，都无返回值；

##### 2、priority 字段

priority 字段用来处理方法执行的顺序。该字段规则为：

 1. 默认情况下父类方法在 **最里层**，这个含义可以在具体实例时候看到
 2. 同一个类中若权限相同，则按照源码中方法定义的顺序依次调用
 3. 权限取值：1-9，数值越小权限越高，越先执行
 
  * 5 表示无操作，若一个方法上该注解字段为5则不会执行；
  * 1-4 表示在[**MethodInjectActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject/src/main/java/com/knowledge/mnlin/methodinject/MethodInjectActivity.java)类对应的声明周期 **方法前** 执行；当前这个前提是子类执行先于父类；
  * 6-9 表示在[**MethodInjectActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject/src/main/java/com/knowledge/mnlin/methodinject/MethodInjectActivity.java)类对应的声明周期 **方法后** 执行；当前这个前提是父类执行先于子类；
  
 4. 权限取其他值可能出现意外情况；具体执行逻辑可以参考源码注释
 
##### 3、inject 字段

默认情况该字段为 **true**，表示需要注入，但有些情况下需要区分 **DEBUG模式** 和其他模式的逻辑，此时可以在AndroidStudio环境下 module 的build.gradle文件中添加如下内容：

```
android {

    ...
    
    buildTypes {
        release {
            ...
            
            //此处为添加内容
            buildConfigField('boolean','DEBUG_INJECT_METHOD','false')
        }

        debug {
            ...
            
            //此处为添加内容   
            buildConfigField('boolean','DEBUG_INJECT_METHOD',true)
        }
    }

    ...
    
}
```

此时编译生成的 **BuildConfig.java** 文件中会多出一个字段：

```
package com.knowledge.mnlin.test;

public final class BuildConfig {
  ...
  
  // Fields from build type: debug
  public static final boolean DEBUG_INJECT_METHOD = true;
}
```

然后在自定义的方法中如果修改了 inject 字段的取值：

```
@MethodInject(method = LifeCycleMethod.onPostCreate, priority = 4, inject = BuildConfig.DEBUG_INJECT_METHOD)
void f1() {
    Log.e("f1:", "执行了");
}
```

这样就可以在 DEBUG 和 RELEASE 模式下控制该方法的执行与否。

#### 4、注解载入或者手动添加载入器

框架在 [**MethodInjectActivity**](https://github.com/lovingning/MethodInject/blob/master/methodinject/src/main/java/com/knowledge/mnlin/methodinject/MethodInjectActivity.java) 类中默认通过反射来加载了处理器：

```
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
     * 设置该方法后,将替换已存在的MethodLifecycle对象
     *
     * 禁止重写该方法
     */
    protected final void replaceMethodInject(@Nullable ActivityLifeCycle methodManager) {
        this.methodManager = methodManager == null ? ActivityDoNothing.getInstance() : methodManager;
    }
}
```

可以看到，代码中会通过反射获取 **$_MethodInject** 类，然后通过 **getInstance** 方法获取实例，赋值给 **methodManager** 成员，这就相同于通过反射进行了初始化。

如果不想通过反射的方式进行赋值，则可以通过如下方法自定义进行初始化：

在 **BaseApplication** 中关闭默认的初始化操作，然后在 **BaseActivity** 中调用 **replaceMethodInject** 方法


source:**BaseApplication.java**

```
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        //关闭methodinject自动初始化
        MethodInjectActivity.setAutoInject(false);
    }
}
```

source:**BaseActivity.java**

```
public class MainActivity extends BaseActivity {
    
    //在动态代码块中进行赋值
    {
        //不能在onCreate方法中调用，因为有可能之后会添加其他方法，有可能onCreate方法执行之前就需要该成员
        replaceMethodInject(MainActivity_MethodInject.getInstance(this));
    }
}
```

此时就可以保证整个框架**100%**无反射操作。不过务必要保证在禁止反射的时候要调用**replaceMethodInject**方法，否则会出现空指针异常。

#### 5、扩展

可以看到**methodManager** 成员其实是一个接口对象，即完全可以 **new** 一个**ActivityLifeCycle**对象进行注入。

更多的可能，可以通过生成对应的动态代理类，监视这 **一簇activity(A extends B extends C extends ... extends MethodInjectActivity)** 中所有方法在声明周期中的调用。并实时的打开或者关闭某个生命周期方法的调用。

*everything @see blog [MethodInject](http://blog.csdn.net/lovingning/article/details/78835589)*

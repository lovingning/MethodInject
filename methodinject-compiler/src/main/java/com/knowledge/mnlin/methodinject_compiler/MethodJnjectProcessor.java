package com.knowledge.mnlin.test;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.service.AutoService;
import com.knowledge.mnlin.methodinject_annotations.annotations.ActivityInject;
import com.knowledge.mnlin.methodinject_annotations.annotations.MethodInject;
import com.knowledge.mnlin.methodinject_annotations.annotations.MethodInject_CreateFile;
import com.knowledge.mnlin.methodinject_annotations.annotations.RootActivity;
import com.knowledge.mnlin.methodinject_annotations.enums.LifeCycleMethod;
import com.knowledge.mnlin.methodinject_annotations.util.OverridePair;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import javafx.util.Pair;

/**
 * 注解处理器
 * <p>
 * 为每个activity生成 $1_MethodInject类;
 * 该类实现了LifeCycleMethod接口;
 */
@AutoService(Processor.class)
public class MethodJnjectProcessor extends AbstractProcessor {
    private static final String TAG = "MethodInject>>> ";

    /**
     * 获取工具类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    /**
     * jdk版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 可处理的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(ActivityInject.class.getCanonicalName());
        annotationTypes.add(RootActivity.class.getCanonicalName());
        annotationTypes.add(MethodInject.class.getCanonicalName());
        return annotationTypes;
    }

    /**
     * 具体的注解处理类
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "\n\n新一轮注解处理:");

        // 获取 ActivityInject 注解标记的类与接口
        Set<TypeElement> activities = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(ActivityInject.class);

        /*如果element是标注于activity(class)之上,则保留;否则即便时enum,interface,@interface,也删除不进行处理;*/
        for (Iterator<TypeElement> iterator = activities.iterator(); iterator.hasNext(); ) {
            Element injectActivity = iterator.next();
            if (!(injectActivity instanceof TypeElement && injectActivity.getKind() == ElementKind.CLASS)) {
                iterator.remove();
            }
        }
        if (activities.size() == 0) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "没有需要处理的类: 注解处理器关闭");
            return true;
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "需要处理的类有: " + activities.size() + " 个!");
        }

        /*针对所有的被标注的activity进行树形遍历,保证他们的继承关系*/

        /*获取根节点*/
        //先获取编注有RootActivity注解的类
        Set<TypeElement> roots = ((Set<TypeElement>) roundEnv.getElementsAnnotatedWith(RootActivity.class));
        //如果获取不到,则默认MethodInjectActivity为树的根
        if (roots.size() == 0) {
            roots.add(processingEnv.getElementUtils().getTypeElement("com.knowledge.mnlin.methodinject.MethodInjectActivity"));
        }
        if (roots.size() != 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "父Activity为0个或多个,无法进行逻辑处理");
            return true;
        }

        //设定MethodInjectActivity为根节点
        TreeNode<TypeElement> root = new TreeNode<>();
        root.data = roots.iterator().next();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "根节点为:" + root.data.getQualifiedName().toString());

        //去除activities中错误标记的element
        deleteElementError(activities, root);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "除去没有根节点的类,还剩下: " + activities.size() + " 个");

        //将activities按照树的模型排列
        addElementToRoot(activities, root);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "生成树形结构");

        //根据实际的结构生成***Activity_MethodInject类,抽象类除外
        generateJavaFile(root);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + "代码创建完毕");

        return true;
    }

    /**
     * 删除一组数据中,根本不会被添加到root树中的元素
     */
    private void deleteElementError(Set<TypeElement> activities, TreeNode<TypeElement> root) {
        for (Iterator<TypeElement> iterator = activities.iterator(); iterator.hasNext(); ) {
            TypeElement activity = iterator.next();
            TypeMirror parent = activity.asType();
            boolean error = true;
            while ((parent = ((TypeElement) processingEnv.getTypeUtils().asElement(parent)).getSuperclass()).getKind() != TypeKind.NONE) {
                /*如果在第n层parent的类型变成TypeKind.NONE之前,没有root.data存在,说明注解标注时错误的*/
                if (((DeclaredType) parent).asElement().toString().equals(root.data.toString())) {
                    error = false;
                    break;
                }
            }
            if (error) {
                iterator.remove();
            }
        }
    }

    /**
     * 判断其中是否有父类是root.data的类
     */
    private void addElementToRoot(Set<TypeElement> activities, TreeNode<TypeElement> root) {
        /*上面表示添加到root完成,接下来需要将activities中剩余部分添加到root.children(已root.children中item作为root)中*/
        if (activities.size() != 0) {
            //将当前activitys中符合条件的元素添加到root.children
            for (Iterator<TypeElement> iterator = activities.iterator(); iterator.hasNext(); ) {
                TypeElement activity = iterator.next();
                TypeMirror parent = activity.asType();

                //只要未到达最顶部java.lang.Object,就一直进行判断
                while ((parent = ((TypeElement) processingEnv.getTypeUtils().asElement(parent)).getSuperclass()).getKind() != TypeKind.NONE) {
                    //如果第n层parent正好是root,那么就添加到root.children
                    if (((DeclaredType) parent).asElement().toString().equals(root.data.toString())) {
                        if (root.children == null) {
                            root.children = new LinkedList<>();
                        }
                        TreeNode<TypeElement> child = new TreeNode<>();
                        child.data = activity;
                        child.parent = root;
                        root.children.add(child);
                        iterator.remove();
                        break;
                    }

                    //如果发现当前获取第n层parent不是root.data,而且第n层parent竟然有ActivityInject标志;说明当前element需要等待其他轮测试
                    if (processingEnv.getTypeUtils().asElement(parent).getAnnotation(ActivityInject.class) != null) {
                        break;
                    }
                }

                /*
                * 若程序运行到此处,有两种可能:
                * 1:说明该element的第n层parent还未被添加,因此需要等待下一轮来处理
                * 2:说明该element已经添加到了root.children上
                * */
            }

            for (TreeNode<TypeElement> child : root.children) {
                if (judgeHasElement(activities, child)) {
                    addElementToRoot(activities, child);
                }
            }
        }
    }

    /**
     * 判断一个结合中是否有能添加到root中的元素
     */
    private boolean judgeHasElement(Set<TypeElement> activities, TreeNode<TypeElement> root) {
        int count = 0;
        for (TypeElement activity : activities) {
            TypeMirror parent = activity.asType();
            boolean error = true;
            while ((parent = ((TypeElement) processingEnv.getTypeUtils().asElement(parent)).getSuperclass()).getKind() != TypeKind.NONE) {
                /*如果在第n层parent的类型变成TypeKind.NONE之前,没有root.data存在,说明注解标注时错误的*/
                if (((DeclaredType) parent).asElement().toString().equals(root.data.toString())) {
                    error = false;
                    break;
                }
            }
            if (error) {
                count++;
            }
        }

        return count < activities.size();
    }

    /**
     * 生成具体的java文件
     */
    private void generateJavaFile(@NonNull TreeNode<TypeElement> root) {
        /*如果节点有数据,并且不是根节点,并且不是abstract类型,那么就创建相应文件*/
        if (root.parent != null && !root.data.getModifiers().contains(Modifier.ABSTRACT)) {
            /*创建root对应的java文件*/

            //获取root以及root.parent...的所有的方法,生成一个list,每个list中存储一个方法队列;方法队列中元素保持有权限和是否执行调用参数
            //创建一个map容器,容量为LifeCycleMethod枚举的个数;键为LifeCycleMethod,值为Pair对象(该对象存储MethodInject实例和自定义的方法名称)
            Map<LifeCycleMethod, List<Pair<String, MethodInject>>> map = new HashMap<>(LifeCycleMethod.values().length);
            createMethodMap(map, root);

            //生成一个field,存储activity实例,用于方法调用
            FieldSpec activity = FieldSpec.builder(TypeName.get(root.data.asType()), "activity", Modifier.PRIVATE).build();

            //创建一个构造方法
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(TypeName.get(root.data.asType()), "activity")
                    .addCode("this.activity = activity;")
                    .build();

            //生成一个静态方法,用于获取生成类的实例
            MethodSpec getInstance = MethodSpec.methodBuilder("getInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(TypeName.get(root.data.asType()), "activity")
                    .addCode("return new " + root.data.getSimpleName().toString() + "_MethodInject(activity);")
                    .returns(TypeName.get(processingEnv.getElementUtils().getTypeElement("com.knowledge.mnlin.methodinject.ActivityLifeCycle").asType()))
                    .build();

            /*生成实例特需的MethodSpec对象*/
            List<MethodSpec> methodSpecs = generateMethodSpec(map);

            /*对于已经组装好了的所有的方法,开始创建类与其中的method*/
            TypeSpec javaFile = TypeSpec.classBuilder(root.data.getSimpleName() + "_MethodInject")
                    .superclass(TypeName.get(processingEnv.getElementUtils().getTypeElement("com.knowledge.mnlin.methodinject.ActivityDoNothing").asType()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(MethodInject_CreateFile.class)
                    .addField(activity)
                    .addMethod(constructor)
                    .addMethod(getInstance)
                    .addMethods(methodSpecs)
                    .build();

            /*java类创建完毕后,将文件写入本地,单个文件流程结束*/
            JavaFile.Builder builder = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(root.data).getQualifiedName().toString(), javaFile);
            try {
                builder.build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*如果root.children不为null,则依次创建root.children*/
        if (root.children != null) {
            for (TreeNode<TypeElement> child : root.children) {
                generateJavaFile(child);
            }
        }
    }

    /**
     * 获取方法列表组
     */
    private void createMethodMap(Map<LifeCycleMethod, List<Pair<String, MethodInject>>> map, @NonNull TreeNode<TypeElement> root) {
        //循环处理当前节点的父节点,只有当前节点的父节点不为空,代表不是根布局时,才会进行逻辑处理
        if (root.parent != null) {
            //针对root进行注解过的方法提取
            TypeElement element = root.data;

            List<? extends Element> enclosedElements = element.getEnclosedElements();
            MethodInject annotation;
            Comparator<Pair<String, MethodInject>> comparator = (o1, o2) -> {
                if (o1.getValue().priority() < o2.getValue().priority()) {
                    return -1;
                } else {
                    return 1;
                }
            };
            Set<Pair<String, MethodInject>> before = new TreeSet<Pair<String, MethodInject>>(comparator);
            Set<Pair<String, MethodInject>> after = new TreeSet<Pair<String, MethodInject>>(comparator);
            for (Element ele : enclosedElements) {
                //必须是Method类型,并且其上有相应注解才可以进行处理
                if (ele.getKind() == ElementKind.METHOD && (annotation = ele.getAnnotation(MethodInject.class)) != null) {
                    /*
                    * 如果有参数并且参数个数为1的话:
                    * 需要判断是否为LifeCycleMethod.onCreate或LifeCycleMethod.onPostCreate;
                    * 如果是两者之一,则将pari对象的key字段,最前添加一个".",表示有一个参数
                    * 两个方法如果参数不同,默认是不能直接覆盖的
                    * */
                    String param = "";
                    List<? extends VariableElement> params = ((ExecutableElement) ele).getParameters();
                    if (params.size() > 1) {
                        throw new RuntimeException("参数不符");
                    }
                    if (params.size() == 1) {
                        //如果只有一个参数,并且是两者之一,并且参数正好是android.os.Bundle类,那么说明参数匹配成功
                        if ((annotation.method() == LifeCycleMethod.onCreate || annotation.method() == LifeCycleMethod.onPostCreate) && "android.os.Bundle".equals(params.get(0).asType().toString())) {
                            param = ".";
                        } else {
                            throw new RuntimeException("参数不符");
                        }
                    }

                    //将方法进行添加
                    if (annotation.priority() < 5 && annotation.priority() > 0) {
                        before.add(new Pair<>(param + ele.getSimpleName().toString(), annotation));
                    }
                    if (annotation.priority() > 5 && annotation.priority() < 10) {
                        after.add(new Pair<>(param + ele.getSimpleName().toString(), annotation));
                    }
                }
            }

            //将method添加到map中
            for (LifeCycleMethod lifeCycleMethod : LifeCycleMethod.values()) {
                addListToMap(map, before, after, lifeCycleMethod);
            }

            //处理父root:当前root肯定不是根节点
            createMethodMap(map, root.parent);
        }
    }

    /**
     * 把符合条件的method添加到对应的map中的list集合里
     */
    private void addListToMap(Map<LifeCycleMethod, List<Pair<String, MethodInject>>> map, Set<Pair<String, MethodInject>> before, Set<Pair<String, MethodInject>> after, LifeCycleMethod lifeCycleMethod) {
        List<Pair<String, MethodInject>> items;
        if ((items = map.get(lifeCycleMethod)) == null) {
            items = new LinkedList<>();
            items.add(OverridePair.getInstance());
        }

        /*对于小于5的权限,依次插入到默认权限位置的前面*/
        Set<Pair<String, MethodInject>> add_before = new LinkedHashSet<>();
        for (Pair<String, MethodInject> pair : before) {
            if (pair.getValue().method() == lifeCycleMethod && !overrideMethod(items, pair.getKey())) {
                add_before.add(pair);
            }
        }
        items.addAll(findInsertPosition(items), add_before);

        /*对于大于5的权限,依次插入到默认权限位置的后面*/
        Set<Pair<String, MethodInject>> add_after = new LinkedHashSet<>();
        for (Pair<String, MethodInject> pair : after) {
            if (pair.getValue().method() == lifeCycleMethod && !overrideMethod(items, pair.getKey())) {
                add_after.add(pair);
            }
        }
        items.addAll(findInsertPosition(items) + 1, add_after);

        map.put(lifeCycleMethod, items);
    }

    /**
     * 查找 5保留权限 的位置
     */
    private int findInsertPosition(List<Pair<String, MethodInject>> items) {
        /*先寻找默认权限所在的位置*/
        int center = items.indexOf(OverridePair.getInstance());
        if (center < 0) {
            String error = "出现未知异常,无法执行 findInsertPosition 方法";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error);
            throw new RuntimeException(error);
        }

        return center;
    }

    /**
     * 判断是否出现了相同的方法
     */
    private boolean overrideMethod(List<Pair<String, MethodInject>> items, String methodName) {
        for (Pair<String, MethodInject> item : items) {
            if (item.getKey().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建文件对象
     */
    private List<MethodSpec> generateMethodSpec(Map<LifeCycleMethod, List<Pair<String, MethodInject>>> map) {
        //默认返回长度为0的List
        List<MethodSpec> methodSpecs = new LinkedList<>();

        /*
        * key表示的是在哪个 LifeCycleMethod 中调用子类方法
        * value表示该 LifeCycleMethod 对应需要调用多少个子类的方法
        * */
        LifeCycleMethod key;
        List<Pair<String, MethodInject>> value;

        /*
         * 针对 LifeLifeCycleMethod.values().length 多的声明周期方法,需要生成最多 2*LifeLifeCycleMethod.values().length个方法
         * */
        CodeBlock.Builder customBlock;
        for (Map.Entry<LifeCycleMethod, List<Pair<String, MethodInject>>> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();

            //移除之上自定义添加的 保留权限5
            value.remove(OverridePair.getInstance());
            if (value.size() == 0) {
                continue;
            }

            /*
            * 创建 before_${key} 方法
            * 创建 after_${key} 方法
            *
            * 返回值为void
            * 访问属性为public
            * 第一行执行super.before_${key}()
            * 然后依次写入list中的方法
            * */
            /*方法名称*/
            String methodName_before = "before_" + key.name();
            String methodName_after = "after_" + key.name();

            /*方法参数*/
            TypeName bundle = TypeName.get(processingEnv.getElementUtils().getTypeElement("android.os.Bundle").asType());
            ParameterSpec parameterSpec = (key == LifeCycleMethod.onCreate || key == LifeCycleMethod.onPostCreate)
                    ? ParameterSpec.builder(bundle, "savedInstanceState").addAnnotation(Nullable.class).build()
                    : null;

            /*
            * 需要根据实际情况添加的代码块
            *
            * 添加before方法
            * */
            customBlock = CodeBlock.builder();
            initCustomBlock(value, customBlock, true);
            //如果发现未生成任何代码,则不会生成相应的方法,也不会添加到 List<MethodSpec> 中
            addMethodSpec(key, methodSpecs, customBlock, methodName_before, parameterSpec);

            /*
            * 需要根据实际情况添加的代码块
            *
            * 添加after方法
            * */
            customBlock = CodeBlock.builder();
            initCustomBlock(value, customBlock, false);
            //如果发现未生成任何代码,则不会生成相应的方法,也不会添加到 List<MethodSpec> 中
            addMethodSpec(key, methodSpecs, customBlock, methodName_after, parameterSpec);
        }

        return methodSpecs;
    }

    /**
     * 添加自定义代码块
     */
    private void initCustomBlock(List<Pair<String, MethodInject>> value, CodeBlock.Builder customBlock, boolean isBefore) {
        for (Pair<String, MethodInject> pair : value) {
            /*权限小于(大于)5,并且需要注入时,才会添加相应代码*/
            if (isBefore && !(pair.getValue().priority() < 5)) {
                continue;
            }
            if (!isBefore && !(pair.getValue().priority() > 5)) {
                continue;
            }

            /*默认不进行注释,需要注释时前面会添加 "//" */
            String comment = pair.getValue().inject() ? "" : "//";
            if (pair.getKey().startsWith(".")) {
                customBlock.add(comment + "activity" + pair.getKey() + "(savedInstanceState);\n");
            } else {
                customBlock.add(comment + "activity." + pair.getKey() + "();\n");
            }
        }
    }

    /**
     * 添加方法到list集合
     */
    private void addMethodSpec(LifeCycleMethod method, List<MethodSpec> methodSpecs, CodeBlock.Builder customBlock, String methodName, ParameterSpec parameterSpec) {
        if (!customBlock.build().isEmpty()) {
            boolean hasParams = (method == LifeCycleMethod.onCreate || method == LifeCycleMethod.onPostCreate);

            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addCode("super." + methodName + "(" + (hasParams ? "savedInstanceState" : "") + ");\n")
                    .addCode(customBlock.build());

            if (parameterSpec != null) {
                builder.addParameter(parameterSpec);
            }
            methodSpecs.add(builder.build());
        }
    }
}

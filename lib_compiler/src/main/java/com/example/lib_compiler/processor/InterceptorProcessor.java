package com.example.lib_compiler.processor;

import static com.example.lib_compiler.util.Constants.ANNOTATION_TYPE_INTERCEPTOR;
import static com.example.lib_compiler.util.Utils.isEmpty;
import static com.example.lib_compiler.util.Utils.isNotEmpty;

import com.example.lib_annotation.Interceptor;
import com.example.lib_compiler.util.Constants;
import com.example.lib_compiler.util.Logger;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @Author winiymissl
 * @Date 2024-04-07 21:37
 * @Version 1.0
 * @Description
 * 人有三样东西是无法隐瞒的，咳嗽、穷困和爱；你想隐瞒却欲盖弥彰。
 * 人有三样东西是不该挥霍的，身体、金钱和爱；你想挥霍却得不偿失。
 * 人有三样东西是无法挽留的，时间、生命和爱；你想挽留却渐行渐远。
 * 人有三样东西是不该回忆的，灾难、死亡和爱；你想回忆却苦不堪言。
 * ——《 Lolita》
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTERCEPTOR)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class InterceptorProcessor extends AbstractProcessor {
    private Map<Integer, Element> interceptors = new HashMap<>();
    private Elements elementUtils;
    private Types types;
    private Filer filer;
    private TypeMirror iInterceptor;
    private Logger logger;
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        /**
         * 对应的是调用这个类的主注解，所在模块的processingEnv
         * */
        filer = processingEnv.getFiler();
        /**
         * processingEnv.getElementUtils() 返回的是 Elements 对象，是用于处理元素（Element）的工具类，它提供了一系列方法来获取和操作源代码中的元素，比如类、方法、字段等。常用的一些方法包括：
         getTypeElement(CharSequence name)：根据类的全限定名获取对应的 TypeElement。
         getPackageElement(CharSequence name)：根据包名获取对应的 PackageElement。
         getAllMembers(TypeElement type)：获取指定类及其所有继承的成员（字段、方法等）。
         getAllAnnotationMirrors(Element element)：获取指定元素上的所有注解镜像。
         getDocComment(Element e)：获取指定元素的文档注释。
         getTypeElement(CharSequence name)：根据类的全限定名获取对应的 TypeElement。
         getPackageElement(CharSequence name)：根据包名获取对应的 PackageElement。
         * */
        elementUtils = processingEnv.getElementUtils();
        /**
         * processingEnv.getTypeUtils() 返回的是 Types 对象，是用于处理类型的工具类。它提供了一系列方法来操作和比较类型，执行类型转换以及获取类型之间的关系等操作。

         一些常用的 Types 工具类方法包括：

         isSameType(TypeMirror t1, TypeMirror t2)：判断两个类型是否相同。
         isAssignable(TypeMirror t1, TypeMirror t2)：判断第一个类型是否可以分配给第二个类型。
         erasure(TypeMirror type)：获取给定类型的擦除类型（Erasure Type）。
         getPrimitiveType(PrimitiveType.Kind kind)：根据原始类型的种类获取对应的原始类型。
         asMemberOf(DeclaredType containing, Element element)：获取声明类型的成员元素。
         * */
        types = processingEnv.getTypeUtils();
        /**
         *通过 processingEnv.getTypeUtils() 获取到的 Types 对象，
         * 我们可以通过 getTypeElement() 方法获取到 IInterceptor 类型的 TypeElement 对象
         * ，然后通过 asType() 方法将其转换为 TypeMirror 对象。
         * 后面用于比较，是否为 IInterceptor 类型
         */
        iInterceptor = elementUtils.getTypeElement(Constants.IROUTE_INTERCEPTOR).asType();
        logger = new Logger(processingEnv.getMessager());
        /**
         * javaCompileOptions {
         annotationProcessorOptions {
         arguments = [
         ROUTER_MODULE_NAME      : project.getName(),
         //这个navigation表的id, 如<navigation android:id="@+id/nav_graph_module_one">
         ROUTER_MODULE_GRAPH_NAME: "community_graph"
         ]
         }
         }
         * 就是这两个参数
         * */
        Map<String, String> options = processingEnv.getOptions();
        if (isNotEmpty(options)) {
            moduleName = options.get(Constants.KEY_MODULE_NAME);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!isEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Interceptor.class);
            try {
                parseInterceptor(elements);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return true;
        }
        return false;
    }

    private void parseInterceptor(Set<? extends Element> elements) throws IOException {
        if (isNotEmpty(elements)) {
            for (Element element : elements) {
                if (verify(element)) {
                    Interceptor interceptor = element.getAnnotation(Interceptor.class);
                    Element lastInterceptor = interceptors.get(interceptor.priority());
                    if (null != lastInterceptor) { // Added, throw exceptions
                        throw new IllegalArgumentException(String.format(Locale.getDefault(), "More than one interceptors use same priority [%d], They are [%s] and [%s].", interceptor.priority(), lastInterceptor.getSimpleName(), element.getSimpleName()));
                    }
                    interceptors.put(interceptor.priority(), element);
                } else {
                    logger.error("A interceptor verify failed, its " + element.asType());
                }
            }

            TypeElement iInterceptor = elementUtils.getTypeElement(Constants.IROUTE_INTERCEPTOR);
            TypeElement iInterceptorRoot = elementUtils.getTypeElement(Constants.IROUTE_INTERCEPTOR_ROOT);
            // Map<String, Class<? extends IInterceptor></>>
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(Integer.class), ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(iInterceptor))));
            // Map<String, Class<? extends IInterceptor>> interceptors
            ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedTypeName, "interceptors").build();
            // public void loadInto(Map<String, Class<? extends IInterceptor>> interceptors){}
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO).addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).addParameter(parameterSpec);
            if (null != interceptors && !interceptors.isEmpty()) {
                for (Map.Entry<Integer, Element> entry : interceptors.entrySet()) {
                    methodBuilder.addStatement("interceptors.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, TypeSpec.classBuilder(Constants.NAME_OF_INTERCEPTOR + moduleName).addModifiers(Modifier.PUBLIC).addMethod(methodBuilder.build()).addSuperinterface(ClassName.get(iInterceptorRoot)).build()).build().writeTo(filer);
        }
    }

    /**
     * Verify the interceptor meta
     */
    private boolean verify(Element element) {
        Interceptor interceptor = element.getAnnotation(Interceptor.class);
        // It must be implement the interface IInterceptor and marked with annotation Interceptor.
        /**
         * 必须有@Interceptor注解
         * */
        return interceptor != null && ((TypeElement) element).getInterfaces().contains(iInterceptor);
    }
}

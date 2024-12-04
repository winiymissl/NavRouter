package com.example.lib_compiler.processor;

import static com.example.lib_compiler.util.Constants.ANNOTATION_TYPE_ROUTE;
import static com.example.lib_compiler.util.Constants.IROUTE_ROOT;
import static com.example.lib_compiler.util.Constants.KEY_MODULE_GRAPH_NAME;
import static com.example.lib_compiler.util.Constants.KEY_MODULE_NAME;
import static com.example.lib_compiler.util.Constants.NO_MODULE_NAME_TIPS;
import static com.example.lib_compiler.util.Utils.isEmpty;
import static com.example.lib_compiler.util.Utils.isNotEmpty;

import com.example.lib_annotation.Route;
import com.example.lib_compiler.model.RouteMeta;
import com.example.lib_compiler.util.Constants;
import com.example.lib_compiler.util.Logger;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
 * @Date 2024-04-07 17:38
 * @Version 1.0
 * @Description 注解处理器
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes(ANNOTATION_TYPE_ROUTE)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RouteProcessor extends AbstractProcessor {
    private Map<String, RouteMeta> routeMetaMap = new TreeMap<>();
    private Elements elementUtils;
    private Types types;
    private Filer filer;

    private File rootFile;
    private String moduleName;
    private String graphName;

    private Logger logger;

//    public File getRootFile() {
//        try {
//            JavaFileObject dummySourceFile = filer.createSourceFile("dummy"+System.currentTimeMillis());
//            String dummySourceFilePath = dummySourceFile.toUri().toString();
//            if(dummySourceFilePath.startsWith("file:")){
//                if(!dummySourceFilePath.contains("file://")){
//                    dummySourceFilePath = dummySourceFilePath.substring("file:".length());
//                }
//            }else{
//                dummySourceFilePath  ="";
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
//        rootFile = processingEnv.
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        logger = new Logger(processingEnv.getMessager());
        /**
         *  javaCompileOptions {
         *             annotationProcessorOptions {
         *                 arguments = [
         *                         ROUTER_MODULE_NAME      : project.getName(),
         *                         //这个navigation表的id, 如<navigation android:id="@+id/nav_graph_module_one">
         *                         ROUTER_MODULE_GRAPH_NAME: "community_graph"
         *                 ]
         *             }
         *         }
         *
         *         就是这个东西里面的两个参数
         * */
        Map<String, String> options = processingEnv.getOptions();
        if (isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
            graphName = options.get(KEY_MODULE_GRAPH_NAME);
        }

        if (isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error(NO_MODULE_NAME_TIPS);
            throw new RuntimeException("Router::Compiler >>> No module name, for more information, look at gradle log.");
        }
        if (isEmpty(graphName)) {
            logger.error(NO_MODULE_NAME_TIPS);
            throw new RuntimeException("Router::Compiler >>> No graph name, for more information, look at gradle log.");
        } else {
            graphName = graphName.replaceAll("[^0-9a-zA-Z_]+", "");
            logger.info("The user has configuration the module graph name, it was [" + graphName + "]");
        }

        logger.info(">>> RouteProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set != null && set.size() > 0) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                this.parseRoutes(elements);
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> elements) {
        if (elements != null && elements.size() > 0) {
            logger.info(">>> Found routes, size is " + elements.size() + " <<<");
            /*
             * 为了清除之前的内容，因为每一次运行代码可能会更改
             * */
            routeMetaMap.clear();
            /*
             * 可以使用 TypeMirror 来判断一个元素是否是某个特定类的子类、实现类，或者是某个接口的实现类等。
             * 拿到两种类型，分别判断
             * */
            TypeMirror typeFragment = elementUtils.getTypeElement(Constants.FRAGMENT).asType();
            TypeMirror typeProvider = elementUtils.getTypeElement(Constants.PROVIDER).asType();
            /*
             * 遍历判断
             * */
            for (Element element : elements) {
                /*
                 * 元素本身的类型
                 * */
                TypeMirror tm = element.asType();
                logger.info("Route class:" + tm.toString());
                Route route = element.getAnnotation(Route.class);
                RouteMeta routeMeta;
                /*
                 * new出来map里面的实例
                 * */
                if (types.isSubtype(tm, typeFragment)) {
                    logger.info(">>> Found fragment route: " + tm.toString() + " <<<");
                    routeMeta = new RouteMeta(RouteMeta.RouteType.FRAGMENT, route, graphName, element);
                } else if (types.isSubtype(tm, typeProvider)) {
                    routeMeta = new RouteMeta(RouteMeta.RouteType.PROVIDER, route, graphName, element);
                } else {
                    throw new RuntimeException("Just support Fragment Route: " + element);
                }
                /*
                 * 将符合条件的元素，添加到集合中
                 * */
                routeMetaMap.put(routeMeta.getDestinationText(), routeMeta);
            }
            logger.info(">>>     routeMetaMap中总数  :  " + routeMetaMap.size());
            /**
             * 拿到需要生成类继承的接口的，类名
             * eg：
             *  addSuperinterface(ClassName.get(iRouteRoot))
             * */
            TypeElement iRouteRoot = elementUtils.getTypeElement(IROUTE_ROOT);
            generatedRoutFile(iRouteRoot);
        }
    }

    private void generatedRoutFile(TypeElement iRouteRoot) {
        /*
         * 这段代码创建了一个参数化类型的 ParameterizedTypeName 对象，表示一个具有两个类型参数的 Map 类型。
         * */
        //create parameter -> Map<String,RouteMeta>
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouteMeta.class));
        /*
         * 构建参数
         * */
        //set parameter name -> Map<String,RouteMeta> routes
        ParameterSpec parameter = ParameterSpec.builder(parameterizedTypeName, "routes").build();
        /*
         * 构建函数。以及函数的注解，修饰符
         * */
        //create function -> public void loadInfo(Map<String,RouteMeta> routes)
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.METHOD_LOAD_INTO).addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).addParameter(parameter);

        //function body
        for (Map.Entry<String, RouteMeta> entry : routeMetaMap.entrySet()) {
            /*
             * 构建函数体
             * 有几个fragment。就构建几个fragment
             * */
            methodBuilder.addStatement("routes.put($S,$T.build($T.$L,$S,$S,$T.class))", entry.getKey(), ClassName.get(RouteMeta.class), ClassName.get(RouteMeta.RouteType.class), entry.getValue().getType(), entry.getValue().getDestinationText(), graphName, ClassName.get(((TypeElement) entry.getValue().getElement())));
        }

        //create class
        String className = Constants.NAME_OF_ROOT + moduleName;
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                /*
                 * 继承IRoute，生成类的文件
                 * */.addSuperinterface(ClassName.get(iRouteRoot)).addModifiers(Modifier.PUBLIC).addMethod(methodBuilder.build()).build();
        try {
            JavaFile.builder(Constants.PACKAGE_OF_GENERATE_FILE, typeSpec).build().writeTo(filer);
            logger.info("Generated Route：" + Constants.PACKAGE_OF_GENERATE_FILE + "." + className);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }
}

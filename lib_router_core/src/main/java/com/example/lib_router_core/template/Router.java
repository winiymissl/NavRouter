package com.example.lib_router_core.template;

import static com.example.lib_compiler.util.Constants.KEY_DESTINATION_ID;
import static com.example.lib_compiler.util.Constants.PACKAGE_OF_GENERATE_FILE;
import static com.example.lib_compiler.util.Constants.PROJECT;
import static com.example.lib_compiler.util.Constants.SEPARATOR;
import static com.example.lib_compiler.util.Constants.SUFFIX_INTERCEPTOR;
import static com.example.lib_compiler.util.Constants.SUFFIX_ROOT;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.example.lib_compiler.model.RouteMeta;
import com.example.lib_compiler.util.Constants;
import com.example.lib_compiler.util.Utils;
import com.example.lib_router_core.Postcard;
import com.example.lib_router_core.Warehouse;
import com.example.lib_router_core.callback.InterceptorCallback;
import com.example.lib_router_core.callback.NavigationCallback;
import com.example.lib_router_core.implments.InterceptorImpl;
import com.example.lib_router_core.utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @Author winiymissl
 * @Date 2024-04-08 11:52
 * @Version 1.0
 */
public class Router {
    static boolean DEBUG = false;

    public static void DEBUG(boolean DEBUG) {
        Router.DEBUG = DEBUG;
    }

    public static boolean debuggable() {
        return Router.DEBUG;
    }

    private static final String TAG = "Router";
    private static volatile Router sInstance;
    private static Application sApplication;
    private static Handler mHandler;

    public static void init(Application application) {
        sApplication = application;
        mHandler = new Handler(Looper.getMainLooper());
        try {

            loadInto();
            Log.d("世界是一个bug", "loadInfo完成");

            /**
             * 初始化线程池，实例化拦截器索引里面的内容
             * */
            InterceptorImpl.init(application);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init failed!", e);
        }
    }

    public static Router getInstance() {
        /**
         * DCL单例模式
         * 一种在多线程环境下使用的单例模式
         * 保证对象之创建一次的同时，尽可能的减少了开销
         * 解释：
         * 即在多个线程同时访问时，先进行一次检查是否已经实例化，如果没有实例化才会进行同步块，并在同步块内再次进行检查，确保只有一个线程创建实例。
         * */
        if (sInstance == null) {
            synchronized (Router.class) {
                if (sInstance == null) {
                    sInstance = new Router();
                }
            }
        }
        return sInstance;
    }

    /**
     * 将指定包下的路由和拦截器加载到内存中。
     * 该方法首先获取指定包下所有文件的名称，然后遍历这些文件名，根据文件名的前缀判断其是路由还是拦截器，
     * 并将它们分别加载到路由表和拦截器索引中。
     *
     * @throws PackageManager.NameNotFoundException 如果包名未找到，抛出此异常
     * @throws InterruptedException                 如果线程被中断，抛出此异常
     * @throws ClassNotFoundException               如果类未找到，抛出此异常
     * @throws NoSuchMethodException                如果找不到构造函数，抛出此异常
     * @throws IllegalAccessException               如果无法访问构造函数，抛出此异常
     * @throws InstantiationException               如果类实例化失败，抛出此异常
     * @throws IOException                          如果发生输入/输出错误，抛出此异常
     * @throws InvocationTargetException            如果调用目标异常，抛出此异常
     */
    private static void loadInto() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException, InvocationTargetException, PackageManager.NameNotFoundException, InterruptedException {
        // 通过包名获取生成文件的文件名集合
        Set<String> routerMap = ClassUtils.getFileNameByPackageName(sApplication, PACKAGE_OF_GENERATE_FILE);
//        List<String> routerMap = ClassUtils.getClassName(sApplication, PACKAGE_OF_GENERATE_FILE);
        Log.d("世界是一个bug", "通过包名获取生成文件的文件名集合 : " + routerMap.toString());

        for (String className : routerMap) {
            // 判断当前类名是否为路由根类
            if (className.startsWith(PACKAGE_OF_GENERATE_FILE + "." + PROJECT + SEPARATOR + SUFFIX_ROOT)) {
                // 加载路由到路由表
                ((IRouteRoot) Class.forName(className).getConstructor().newInstance()).loadInto(Warehouse.routes);
            } else if (className.startsWith(PACKAGE_OF_GENERATE_FILE + "." + PROJECT + SEPARATOR + SUFFIX_INTERCEPTOR)) {
                // 加载拦截器到拦截器索引
                ((IInterceptorRoot) Class.forName(className).getConstructor().newInstance()).loadInto(Warehouse.interceptorsIndex);
            }
        }
        Log.d("世界是一个bug", "loadInto 　:  " + Warehouse.routes.size());
    }


    /**
     * 导航方法，用于处理导航逻辑。
     *
     * @param navController      NavController对象，用于执行导航操作。
     * @param postcard           Postcard对象，包含导航的目的地信息。
     * @param popUpToDestination 需要弹出到的目标导航标识符。
     * @param inclusive          是否包含popUpToDestination指定的碎片。
     * @param callback           NavigationCallback对象，用于处理导航过程中的回调。
     * @return 返回一个对象，根据导航类型可能为IProvider实例或null。
     */
    public Object navigation(NavController navController, Postcard postcard, String popUpToDestination, boolean inclusive, NavigationCallback callback) {
        switch (postcard.getType()) {
            case FRAGMENT:
                // 当导航类型为FRAGMENT时，通过Interceptor拦截处理。
                InterceptorImpl.onInterceptions(postcard, new InterceptorCallback() {
                    /**
                     * 继续处理导航过程。
                     * @param postcard 包含导航信息的Postcard对象。
                     */
                    @Override
                    public void onContinue(Postcard postcard) {
                        // 在主线程中执行实际的导航操作。
                        runInMainThread(() -> {
                            _navigation(navController, postcard, popUpToDestination, inclusive, callback);
                        });
                    }

                    /**
                     * 导航被中断时的处理逻辑。
                     * @param exception 导航中断的原因。
                     */
                    @Override
                    public void onInterrupt(Exception exception) {
                        // 如果设置了回调，则调用导航中断的回调方法。
                        if (null != callback) {
                            callback.onInterrupt(postcard);
                        }
                    }
                });
                break;
            case PROVIDER:
                // 当导航类型为PROVIDER时，从Warehouse的providerMap中获取或创建IProvider实例。
                if (Warehouse.providerMap.get(postcard.getDestination()) == null) {
                    try {
                        // 如果providerMap中不存在，则尝试通过反射创建实例并添加到providerMap中。
                        Warehouse.providerMap.put(postcard.getDestination(), (IProvider) postcard.getDestination().getConstructor().newInstance());
                    } catch (Exception e) {
                        // 打印异常信息。
                        e.printStackTrace();
                    }
                }
                // 返回对应的IProvider实例。
                return Warehouse.providerMap.get(postcard.getDestination());
            default:
                // 对于其他类型，默认不处理，返回null。
                break;
        }
        // 默认返回值为null。
        return null;
    }


    /**
     * 进行导航操作的私有方法。
     *
     * @param navController      NavController对象，用于执行导航操作。
     * @param postcard           表示导航目标和相关信息的Postcard对象。
     * @param popUpToDestination 指定要弹出到的目的地的标识符。
     * @param inclusive          是否在弹出操作时包含指定的目的地。
     * @param callback           导航回调对象，用于处理导航的开始和结束。
     * @return 总是返回null，目前方法设计上没有返回具体结果的需要。
     */
    private Object _navigation(NavController navController, Postcard postcard, String popUpToDestination, boolean inclusive, NavigationCallback callback) {
        try {
            // 先进行前提条件的检查
            if (!isNeedPrerequisite(navController, postcard, popUpToDestination, inclusive)) {
                // 如果检查不通过，则直接进行导航
                Log.d("世界是一个bug", "loadInto 　:  " + " 是否进行直接导航  :  " + !isNeedPrerequisite(navController, postcard, popUpToDestination, inclusive));
                navigate(navController, postcard.getGraphText(), postcard.getDestinationText(), popUpToDestination, inclusive, postcard.getBundle());
            }
            // 到达目的地后的回调
            if (null != callback) {
                callback.onArrival(postcard);
            }

        } catch (Exception e) {
            // 导航失败时的回调
            if (null != callback) {
                callback.onLost(postcard);
            }
            Log.e(TAG, e.getMessage());
            return null;
        }
        return null;
    }


    /**
     * isNeedPrerequisite 方法通常用于判断是否需要执行预先条件（prerequisite）操作。
     * 在很多情况下，特别是在路由或导航过程中，可能需要先执行一些预先条件的检查或操作，然后再执行实际的路由或导航操作。
     */
    private boolean isNeedPrerequisite(NavController navController, Postcard postcard, String popUpToDestination, boolean inclusive) throws Exception {
        String prerequisiteDestination = postcard.getPrerequisiteDestination();
        if (Utils.isNotEmpty(prerequisiteDestination)) {
            Bundle bundle = postcard.getBundle();
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString(Constants.KEY_CONTINUE_DESTINATION, postcard.getDestinationText());
            navigate(navController, postcard.getPrerequisiteDestinationGraph(), prerequisiteDestination, popUpToDestination, inclusive, bundle);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据给定的路径构建一张明信片对象。
     *
     * @param path 表示路由的路径。
     * @return 返回一个Postcard对象。如果给定的路径在Warehouse的路由表中找不到，则返回一个包含空信息的Postcard对象。
     */
    public Postcard build(String path) throws Exception {
        // 通过路径从Warehouse的路由表中获取RouteMeta对象
        RouteMeta routeMeta = Warehouse.routes.get(path);
        if (routeMeta == null) {
            // 如果找不到对应的RouteMeta，返回一个默认构造的Postcard对象
            Log.d("世界是一个bug", String.valueOf(Warehouse.routes.size()));
            throw new Exception("找不到对应的RouteMeta");
//            return new Postcard(RouteMeta.RouteType.FRAGMENT, "", "", null);
        }
        // 根据找到的RouteMeta对象构造并返回一个Postcard对象
        return new Postcard(routeMeta.getType(), routeMeta.getDestinationText(), routeMeta.getGraphText(), routeMeta.getDestination());
    }


    /**
     * 使用给定的 NavController 实例和导航图信息，进行导航操作。
     *
     * @param navController      NavController 实例，用于执行导航操作。
     * @param graphText          表示导航图文本标识，用于确定当前的导航图。
     * @param destinationText    目的地文本标识，用于确定导航的目的地。
     * @param popUpToDestination 如果非空，表示要弹出到指定目的地。
     * @param inclusive          如果为 true，则在弹出操作时包括指定的目的地。
     * @param bundle             用于传递给目的地的额外参数。如果为 null，将会创建一个新的 Bundle 实例。
     */
    private void navigate(NavController navController, String graphText, String destinationText, String popUpToDestination, boolean inclusive, Bundle bundle) throws Exception {
        Log.d("世界是一个bug", "Router：  navigate  ： " + graphText);
        if (navController == null) {
            // NavController 不能为空,否则抛出异常
            throw new Exception("The NavController must not be null!");
        }
        int destinationId = getDestinationId(destinationText);
        NavOptions.Builder navOptions = new NavOptions.Builder();
        // 如果 popUpToDestination 不为空，进行相关设置（代码未给出具体实现）
        if (Utils.isNotEmpty(popUpToDestination)) {
        }
        // 如果 bundle 为 null，则创建一个新的 Bundle 实例
        if (bundle == null) {
            bundle = new Bundle();
        }
        Log.d("世界是一个bug", "Router： navController.getGraph().getId()  ：  " + navController.getGraph().getId());
        Log.d("世界是一个bug", "Router： getDestinationId(graphText)  ：  " + getDestinationId(graphText));
        // 判断当前导航图是否与给定的 graphText 匹配，进行相应导航操作
        if (navController.getGraph().getId() == getDestinationId(graphText)) {
            navController.navigate(destinationId, bundle, navOptions.build());
        } else {
            bundle.putInt(KEY_DESTINATION_ID, destinationId);
            Log.d("世界是一个bug", "Router：   ：  导航到   ： " + graphText);

            try {
                navController.navigate(getDestinationId(graphText), bundle, navOptions.build());
            } catch (Exception e) {
                Log.d("世界是一个bug", "Router：   navController.navigate(getDestinationId(graphText), bundle, navOptions.build());：   " + e.toString());
            }
        }
    }

    /**
     * 获取指定目的地文本对应的身份ID。
     * 该方法首先尝试从Warehouse.destinationMap中获取对应目的地的ID，如果存在且不为0，则直接返回。
     * 如果在Warehouse.destinationMap中未找到或找到的ID为0，则通过资源文件动态获取该目的地文本对应的ID，
     * 并将该ID存储到Warehouse.destinationMap中，以备后续使用。
     *
     * @param destinationText 目的地的文本标识。
     * @return 目的地的ID。如果在资源文件中找到，则返回该ID；否则返回0。
     */
    private int getDestinationId(String destinationText) {

        // 尝试从Warehouse.destinationMap中获取目的地ID，如果存在且不为0，则直接返回
        if (Warehouse.destinationMap.get(destinationText) != null && Warehouse.destinationMap.get(destinationText) != 0) {
            return Warehouse.destinationMap.get(destinationText);
        } else {
            // 通过资源文件动态获取目的地ID，并将该ID存储到Warehouse.destinationMap中
            /*
             * 寻找id叫login_graph的对应的ID
             * */
            int destinationId = sApplication.getResources().getIdentifier(destinationText, "id", sApplication.getPackageName());

            Warehouse.destinationMap.put(destinationText, destinationId);
            return destinationId;
        }
    }


    /**
     * 在主线程中执行指定的Runnable对象。
     * 如果当前线程不是主线程，则通过Handler将Runnable post到主线程的MessageQueue中；
     * 如果当前线程是主线程，则直接执行Runnable的run方法。
     *
     * @param runnable 要在主线程中执行的Runnable对象。
     */
    private void runInMainThread(Runnable runnable) {
        // 检查当前线程是否是主线程
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            // 如果当前线程不是主线程，则通过Handler将任务post到主线程
            mHandler.post(runnable);
        } else {
            // 如果当前线程是主线程，则直接执行任务
            runnable.run();
        }
    }
}
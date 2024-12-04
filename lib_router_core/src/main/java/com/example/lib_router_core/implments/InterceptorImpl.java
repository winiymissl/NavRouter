package com.example.lib_router_core.implments;

import android.content.Context;

import com.example.lib_router_core.Postcard;
import com.example.lib_router_core.Warehouse;
import com.example.lib_router_core.callback.InterceptorCallback;
import com.example.lib_router_core.exception.HandlerException;
import com.example.lib_router_core.template.IInterceptor;
import com.example.lib_router_core.thread.CancelableCountDownLatch;
import com.example.lib_router_core.thread.DefaultPoolExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author winiymissl
 * @Date 2024-04-08 12:26
 * @Version 1.0
 */
public class InterceptorImpl {
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();

    public static void init(final Context context) {

        executor.execute(new Runnable() {
            /**
             * 这个方法是一个线程的入口点，用于在运行时动态地加载和初始化所有的拦截器。
             * 它会检查Warehouse类中的拦截器索引是否已经建立且不为空，然后遍历每个拦截器的配置，
             * 尝试实例化并初始化这些拦截器。
             */
            @Override
            public void run() {
                // 如果拦截器索引存在且不为空，则开始遍历并尝试加载每个拦截器
                if (Warehouse.interceptorsIndex != null && !Warehouse.interceptorsIndex.isEmpty()) {
                    for (Map.Entry<Integer, Class<? extends IInterceptor>> entry : Warehouse.interceptorsIndex.entrySet()) {
                        // 尝试从拦截器索引中获取拦截器类，并实例化它
                        Class<? extends IInterceptor> interceptorClass = entry.getValue();
                        try {
                            // 通过无参构造函数创建拦截器实例，并初始化
                            IInterceptor iInterceptor = interceptorClass.getConstructor().newInstance();
                            iInterceptor.init(context);
                            // 将初始化好的拦截器添加到拦截器列表中
                            Warehouse.interceptors.add(iInterceptor);
                        } catch (Exception e) {
                            // 如果在创建或初始化拦截器时发生异常，则打印异常堆栈信息
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static void onInterceptions(final Postcard postcard, final InterceptorCallback callback) {
        if (Warehouse.interceptors.size() > 0) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    CancelableCountDownLatch countDownLatch = new CancelableCountDownLatch(Warehouse.interceptors.size());
                    try {
                        execute(0, countDownLatch, postcard);
                        countDownLatch.await(postcard.getTimeout(), TimeUnit.SECONDS);
                        if (countDownLatch.getCount() > 0) { // Cancel the navigation this time, if it hasn't return anythings.
                            callback.onInterrupt(new HandlerException("The interceptor processing timed out."));
                        } else if (null != postcard.getTag()) { // Maybe some exception in the tag.
                            callback.onInterrupt((HandlerException) postcard.getTag());
                        } else {
                            callback.onContinue(postcard);
                        }
                    } catch (InterruptedException e) {
                        callback.onInterrupt(e);
                    }
                }
            });
        } else {
            callback.onContinue(postcard);
        }
    }

    /**
     * @param index          current interceptor index
     * @param countDownLatch interceptor counter
     * @param postcard       routeMeta
     */
    private static void execute(final int index, final CancelableCountDownLatch countDownLatch, final Postcard postcard) {
        if (index < Warehouse.interceptors.size()) {
            IInterceptor iInterceptor = Warehouse.interceptors.get(index);
            iInterceptor.process(postcard, new InterceptorCallback() {
                @Override
                public void onContinue(Postcard postcard) {
                    // Last interceptor execute over with no exception.

                    countDownLatch.countDown();
                    // When counter is down, it will be execute continue ,but index bigger than interceptors size, then U know.
                    execute(index + 1, countDownLatch, postcard);
                }

                @Override
                public void onInterrupt(Exception exception) {
                    postcard.setTag(null == exception ? new HandlerException("No message.") : exception);    // save the exception message for backup.
                    countDownLatch.cancel();
                }
            });
        }
    }
}

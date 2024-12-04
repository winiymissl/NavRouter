package com.example.lib_router_core.template;

import android.content.Context;

import com.example.lib_router_core.Postcard;
import com.example.lib_router_core.callback.InterceptorCallback;

/**
 * @Author winiymissl
 * @Date 2024-04-08 13:24
 * @Version 1.0
 */
public interface IInterceptor {
    /**
     * The operation of this interceptor.
     */
    void process(Postcard postcard, InterceptorCallback callback);

    /**
     * Do your init work in this method, it well be call when processor has been load.
     */
    void init(Context context);
}

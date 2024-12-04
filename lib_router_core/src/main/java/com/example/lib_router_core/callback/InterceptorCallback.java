package com.example.lib_router_core.callback;

import com.example.lib_router_core.Postcard;

/**
 * @Author winiymissl
 * @Date 2024-04-08 12:23
 * @Version 1.0
 */
public interface InterceptorCallback {
    /*
     * Continue process
     * */
    void onContinue(Postcard postcard);

    /*
     * 验证不通过的情况
     * */
    void onInterrupt(Exception exception);
}

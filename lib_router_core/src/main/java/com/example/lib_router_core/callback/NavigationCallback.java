package com.example.lib_router_core.callback;

import com.example.lib_router_core.Postcard;

/**
 * @Author winiymissl
 * @Date 2024-04-08 12:21
 * @Version 1.0
 */
public interface NavigationCallback {

    void onArrival(Postcard postcard);

    void onLost(Postcard postcard);

    void onInterrupt(Postcard postcard);
}

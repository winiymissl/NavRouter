package com.example.lib_router_core.callback;

import com.example.lib_router_core.Postcard;

/**
 * @Author winiymissl
 * @Date 2024-04-08 12:22
 * @Version 1.0
 * @Description 这个类存在的意义：提供一个空实现的默认实现类可以简化代码编写，
 * 使得使用者在需要的时候可以选择性地重写某些回调方法，而不是必须实现所有的回调方法。
 */
public class SimpleNavigationCallback implements NavigationCallback {


    @Override
    public void onArrival(Postcard postcard) {

    }

    @Override
    public void onLost(Postcard postcard) {

    }

    @Override
    public void onInterrupt(Postcard postcard) {

    }
}
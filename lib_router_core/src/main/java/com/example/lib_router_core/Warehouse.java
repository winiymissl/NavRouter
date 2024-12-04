package com.example.lib_router_core;

import com.example.lib_compiler.model.RouteMeta;
import com.example.lib_router_core.template.IInterceptor;
import com.example.lib_router_core.template.IProvider;
import com.example.lib_router_core.utils.UniqueKeyTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author winiymissl
 * @Date 2024-04-08 13:20
 * @Version 1.0
 */
public class Warehouse {
    // Cache route and metas
    public static Map<String, RouteMeta> routes = new HashMap<>();

    public static Map<String, RouteMeta> get() {
        return routes;
    }

    public static Map<String, Integer> destinationMap = new HashMap<>();
    public static Map<Class<?>, IProvider> providerMap = new HashMap<>();

    // Cache interceptor
    public static Map<Integer, Class<? extends IInterceptor>> interceptorsIndex = new UniqueKeyTreeMap<>("More than one interceptors use same priority [%s]");
    public static List<IInterceptor> interceptors = new ArrayList<>();

    public static void clear() {
        routes.clear();
        interceptors.clear();
        interceptorsIndex.clear();
    }
}

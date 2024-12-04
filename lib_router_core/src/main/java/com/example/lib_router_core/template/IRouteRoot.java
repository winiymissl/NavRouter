package com.example.lib_router_core.template;

import com.example.lib_compiler.model.RouteMeta;

import java.util.Map;

/**
 * @Author winiymissl
 * @Date 2024-04-08 11:51
 * @Version 1.0
 */
public interface IRouteRoot {
    void loadInto(Map<String, RouteMeta> args);
}

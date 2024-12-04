package com.example.lib_router_core.template;

import java.util.Map;

/**
 * @Author winiymissl
 * @Date 2024-04-08 13:24
 * @Version 1.0
 */
public interface IInterceptorRoot {
    void loadInto(Map<Integer, Class<? extends IInterceptor>> map);

}

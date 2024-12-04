package com.example.lib_router_core.utils;


import androidx.annotation.Nullable;

import java.util.TreeMap;

/**
 * @Author winiymissl
 * @Date 2024-04-08 13:33
 * @Version 1.0
 */
public class UniqueKeyTreeMap<K,V> extends TreeMap<K, V> {
    private String tipText;

    public UniqueKeyTreeMap(String exceptionText) {
        super();
        tipText = exceptionText;
    }
    @Nullable
    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }
}

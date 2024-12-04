package com.example.lib_compiler.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author winiymissl
 * @Date 2024-04-07 20:18
 * @Version 1.0
 */
public class Utils {
    public static boolean isNotEmpty(CharSequence cs) {
        return cs != null && cs.length() > 0;
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return coll != null && coll.size() > 0;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && map.size() > 0;
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isEmpty(final TreeMap<?, ?> treeMap) {
        return treeMap == null || treeMap.isEmpty();
    }
}

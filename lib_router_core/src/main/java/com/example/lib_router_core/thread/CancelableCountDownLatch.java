package com.example.lib_router_core.thread;

import java.util.concurrent.CountDownLatch;

/**
 * @Author winiymissl
 * @Date 2024-04-08 12:27
 * @Version 1.0
 */
public class CancelableCountDownLatch extends CountDownLatch {
    public CancelableCountDownLatch(int count) {
        super(count);
    }

    public void cancel() {
        while (getCount() > 0) {
            countDown();
        }
    }
}

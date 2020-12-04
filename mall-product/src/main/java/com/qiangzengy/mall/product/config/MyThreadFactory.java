package com.qiangzengy.mall.product.config;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/12/4
 */
public class MyThreadFactory implements ThreadFactory {

    private final String namePrefix;

    private final AtomicInteger nextId = new AtomicInteger(1);


    MyThreadFactory(String whatFeatureOfGroup) {
        namePrefix = "From MyThreadFactory" + whatFeatureOfGroup + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,namePrefix+nextId);
    }
}

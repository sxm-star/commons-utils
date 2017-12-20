package com.songxm.commons;

import org.springframework.aop.framework.AdvisedSupport;

import java.lang.reflect.Field;

public class BaseAopUtils {
    public BaseAopUtils() {
    }

    public static Object getObjectFromCglibProxy(Object proxy) {
        try {
            Field e = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            e.setAccessible(true);
            Object dynamicAdvisedInterceptor = e.get(proxy);
            Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            return ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        } catch (Throwable var4) {
            return null;
        }
    }
}
package com.songxm.commons;

import com.google.common.base.Preconditions;
import com.rits.cloning.Cloner;
import jodd.bean.BeanCopy;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
@SuppressWarnings("unchecked")
public class BaseBeanUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseBeanUtils.class);
    private static final BeanUtilsBean beanUtil = BeanUtilsBean.getInstance();
    private static final Cloner cloner = new Cloner();

    public BaseBeanUtils() {
    }

    public static void copyProperties(Object src, Object des) {
        Preconditions.checkArgument(src != null, "源对象不能为空");
        Preconditions.checkArgument(des != null, "目标对象不能为空");
        BeanCopy.beans(src, des).copy();
    }

    public static void copyNoneNullProperties(Object src, Object des) {
        Preconditions.checkArgument(src != null, "源对象不能为空");
        Preconditions.checkArgument(des != null, "目标对象不能为空");
        BeanCopy.beans(src, des).ignoreNulls(true).copy();
    }

    public static <T> T shallowClone(T src) {
        Preconditions.checkArgument(src != null, "对象不能为空");

        try {
            return (T)beanUtil.cloneBean(src);
        } catch (Exception var2) {
            throw new RuntimeException("对类型为\'{" + src.getClass().getName() + "}\'进行浅拷贝失败.");
        }
    }

    public static <T> T deepClone(T src) {
        Preconditions.checkArgument(src != null, "对象不能为空");
        return cloner.deepClone(src);
    }

    public static Map<String, Object> beanToMap(Object obj) {
        Preconditions.checkArgument(obj != null, "对象不能为空");
        if(obj instanceof Map) {
            return (Map)obj;
        } else {
            try {
                Map e = PropertyUtils.describe(obj);
                e.remove("class");
                return e;
            } catch (Throwable var2) {
                log.error("将对象[{}]转换为map异常: {}", BaseJsonUtils.writeValue(obj), ExceptionUtils.getStackTrace(var2));
                throw new RuntimeException(String.format("将对象[%s]转换为map异常", new Object[]{BaseJsonUtils.writeValue(obj)}));
            }
        }
    }

    public static Map<String, Object> beanToMapNonNull(Object obj) {
        Map map = beanToMap(obj);
        Iterator iterator = map.entrySet().iterator();

        while(iterator.hasNext()) {
            if(((Entry)iterator.next()).getValue() == null) {
                iterator.remove();
            }
        }

        return map;
    }

    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (Throwable var2) {
            log.error("无法实例化类[{}]对象:{}", cls.getSimpleName(), ExceptionUtils.getStackTrace(var2));
            return null;
        }
    }

    public static <T> T convert(Object obj, Class<T> cls) {
        Object t = newInstance(cls);
        if(t != null) {
            copyNoneNullProperties(obj, t);
        }

        return (T)t;
    }
}

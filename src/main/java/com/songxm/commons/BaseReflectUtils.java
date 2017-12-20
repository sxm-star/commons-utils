package com.songxm.commons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@SuppressWarnings("unchecked")
public class BaseReflectUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseReflectUtils.class);

    public BaseReflectUtils() {
    }

    public static Object parse(String src, Type type) {
        Type rawType = type;
        boolean isArray = false;
        if(type instanceof ParameterizedType) {
            isArray = true;
            rawType = ((ParameterizedType)type).getRawType();
            if(!(rawType instanceof Class) || rawType != List.class && rawType != Set.class) {
                log.info("无法将{}转换为不支持的类型{}", src, type);
                return null;
            }
        }

        Class cls = getClass(type);
        if(isArray) {
            Stream editor1 = BaseJsonUtils.readValues(src, String.class).stream().map((innerSrc) -> {
                return parse(innerSrc, cls);
            });
            return rawType == List.class?editor1.collect(Collectors.toList()):editor1.collect(Collectors.toSet());
        } else {
            PropertyEditor editor = PropertyEditorManager.findEditor(cls);
            if(editor != null) {
                editor.setAsText(src);
                return editor.getValue();
            } else if(cls != Date.class && cls != java.sql.Date.class) {
                try {
                    return BaseJsonUtils.readValue(src, cls);
                } catch (Throwable var7) {
                    log.error("无法将{}解析为{}类型的对象", src, type);
                    return null;
                }
            } else {
                return BaseDateUtils.parseDate(src);
            }
        }
    }

    private static Class getClass(Type type) {
        if(type instanceof ParameterizedType) {
            type = ((ParameterizedType)type).getActualTypeArguments()[0];
        }

        return (Class)type;
    }
}

package com.songxm.commons;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.songxm.commons.annotation.JsonMosaic;
import com.songxm.commons.annotation.LogIgnore;
import com.songxm.commons.annotation.LogInfo;
import com.songxm.commons.exception.AspectException;
import com.songxm.commons.serializer.CustomSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.Stream;
@SuppressWarnings("unchecked")
public class BaseAspectUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseAspectUtils.class);
    private static final Set<Class<?>> jsonIgnoredClasses;
    private static ObjectMapper mapper;

    public BaseAspectUtils() {
    }

    public static Object logAround(ProceedingJoinPoint joinPoint, Long maxTimeInMillis) throws Throwable {
        long start = System.currentTimeMillis();
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Map paramValues = paramsMap(method, joinPoint.getArgs());
        String methodInfo = methodInfo(method, paramValues);
        log.info(methodInfo + "-开始");
        boolean var16 = false;

        Object e;
        try {
            var16 = true;
            e = joinPoint.proceed();
            var16 = false;
        } catch (Throwable var17) {
            String message = var17.getMessage();
            if(StringUtils.isNotBlank(message)) {
                message = message.replaceFirst("false:", "");
            }

            LogInfo logInfo = (LogInfo)method.getAnnotation(LogInfo.class);
            if(!(var17 instanceof BaseAspectUtils.IgnorableException)) {
                StringBuilder params = new StringBuilder(method.getDeclaringClass().getSimpleName() + "." + method.getName());
                if(joinPoint.getArgs() != null) {
                    params.append("(").append(paramInfo(method, joinPoint.getArgs())).append(")");
                }

                if(!StringUtils.startsWithIgnoreCase(var17.getMessage(), "false:") && (logInfo == null || logInfo.logError())) {
                    log.error("方法{}调用异常", params.toString());
                } else {
                    log.warn("方法{}调用异常", params.toString());
                }
            }

            if(logInfo == null || !StringUtils.isNotBlank(logInfo.errorKey()) && logInfo.logError()) {
                throw var17;
            }

            if(logInfo.errorParams().length > 0) {
                String[] params1 = new String[logInfo.errorParams().length + 1];
                IntStream.range(0, logInfo.errorParams().length).forEach((index) -> {
                    params1[index] = (String)paramValues.get(logInfo.errorParams()[index]);
                });
                params1[logInfo.errorParams().length] = message;
                throw AspectException.fromKey(logInfo.errorKey(), logInfo.logError(), params1);
            }

            throw AspectException.fromKey(logInfo.errorKey(), logInfo.logError(), new String[]{message});
        } finally {
            if(var16) {
                long timeUsed = System.currentTimeMillis() - start;
                if(maxTimeInMillis.longValue() > 0L && timeUsed > maxTimeInMillis.longValue()) {
                    log.warn("{}-结束, 所花时间: {}ms", methodInfo, Long.valueOf(timeUsed));
                } else {
                    log.debug("{}-结束, 所花时间: {}ms", methodInfo, Long.valueOf(timeUsed));
                }

            }
        }

        long message1 = System.currentTimeMillis() - start;
        if(maxTimeInMillis.longValue() > 0L && message1 > maxTimeInMillis.longValue()) {
            log.warn("{}-结束, 所花时间: {}ms", methodInfo, Long.valueOf(message1));
        } else {
            log.debug("{}-结束, 所花时间: {}ms", methodInfo, Long.valueOf(message1));
        }

        return e;
    }

    private static String methodInfo(Method method, Map<String, String> paramValues) {
        StringBuilder buf = (new StringBuilder("方法")).append(method.getDeclaringClass().getSimpleName()).append(".").append(method.getName());

        try {
            String e = "";
            LogInfo logInfo = (LogInfo)method.getAnnotation(LogInfo.class);
            if(logInfo != null) {
                e = logInfo.value();
                if(logInfo.params().length > 0) {
                    String[] var5 = logInfo.params();
                    int var6 = var5.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        String param = var5[var7];
                        String paramValue = (String)paramValues.get(param);
                        e = e.replaceFirst("\\{\\}", paramValue == null?"":Matcher.quoteReplacement(paramValue));
                    }
                }
            }

            if(StringUtils.isNotBlank(e)) {
                if(logInfo.replace()) {
                    buf = new StringBuilder(e);
                } else {
                    buf.append(":").append(e);
                }
            }
        } catch (Throwable var10) {
            log.error("获取[{}]额外日志信息异常:{}", method, ExceptionUtils.getStackTrace(var10));
        }

        return buf.toString();
    }

    private static String paramInfo(Method method, Object[] args) {
        StringBuilder buf = new StringBuilder();
        if(args != null) {
            Parameter[] params = method.getParameters();
            IntStream.range(0, args.length).forEach((index) -> {
                if(!(args[index] instanceof InputStream)) {
                    String sParam = "";
                    if(args[index] != null && params[index].getAnnotation(LogIgnore.class) == null) {
                        if(BaseObjectUtils.isPrimitive(args[index].getClass()) && params[index].getAnnotation(JsonMosaic.class) != null) {
                            JsonMosaic jsonMosaic = (JsonMosaic)params[index].getAnnotation(JsonMosaic.class);
                            sParam = BaseJsonUtils.writeValue(args[index]);
                            sParam = BaseStringUtils.mosaic(sParam, jsonMosaic, '*');
                        } else {
                            sParam = toJson(args[index]);
                        }
                    }

                    if(index != 0) {
                        buf.append(", ");
                    }

                    buf.append(sParam);
                }

            });
        }

        return buf.toString();
    }

    private static Map<String, String> paramsMap(Method method, Object[] args) {
        HashMap map = new HashMap();
        if(args != null) {
            Parameter[] params = method.getParameters();
            IntStream.range(0, args.length).forEach((index) -> {
                try {
                    String e = index + "";
                    if(params[index].isNamePresent()) {
                        e = params[index].getName();
                    }

                    map.put(e, toJson(args[index]));
                    if(args[index] != null && params[index].getAnnotation(LogIgnore.class) == null) {
                        if(BaseObjectUtils.isPrimitive(args[index].getClass()) && params[index].getAnnotation(JsonMosaic.class) != null) {
                            JsonMosaic jsonMosaic = (JsonMosaic)params[index].getAnnotation(JsonMosaic.class);
                            String text = BaseJsonUtils.writeValue(args[index]);
                            text = BaseStringUtils.mosaic(text, jsonMosaic, '*');
                            map.put(e, text);
                        } else {
                            map.putAll(toMap(e, args[index]));
                        }
                    }
                } catch (Throwable var7) {
                    log.error("将对象[{}]转为map异常:{}", args[index].getClass().getSimpleName(), ExceptionUtils.getStackTrace(var7));
                }

            });
        }

        return map;
    }

    private static String toJson(Object obj) {
        if(obj == null) {
            return "null";
        } else {
            try {
                return mapper.writeValueAsString(obj);
            } catch (Throwable var2) {
                throw new RuntimeException(var2);
            }
        }
    }

    private static Map<String, String> toMap(String name, Object value) {
        return toMap(name, value, 0);
    }

    private static Map<String, String> toMap(String name, Object value, int crtDepth) {
        HashMap map = new HashMap();
        if(value != null && crtDepth < 5) {
            if(BaseObjectUtils.isPrimitive(value.getClass())) {
                map.put(name, value.toString());
            } else if(value instanceof Enum) {
                map.put(name, ((Enum)value).name());
            } else if(!(value instanceof Collection) && !(value instanceof Map)) {
                Class temp1 = (Class)jsonIgnoredClasses.stream().filter((cls) -> {
                    return cls.isAssignableFrom(value.getClass());
                }).findAny().orElse(null);
                if(temp1 != null) {
                    map.put(name, value.getClass().getSimpleName());
                } else {
                    Field[] list1 = value.getClass().getDeclaredFields();
                    if(list1 != null) {
                        Stream.of(list1).forEach((field) -> {
                            Object fieldValue = null;

                            try {
                                field.setAccessible(true);
                                fieldValue = field.get(value);
                            } catch (Throwable var8) {
                                log.warn("无法访问类[{}]成员变量[{}]", field.getClass().getSimpleName(), field.getName());
                            }

                            if(fieldValue != null && field.getAnnotation(LogIgnore.class) == null) {
                                if(BaseObjectUtils.isPrimitive(fieldValue.getClass()) && field.getAnnotation(JsonMosaic.class) != null) {
                                    JsonMosaic jsonMosaic = (JsonMosaic)field.getAnnotation(JsonMosaic.class);
                                    String text = BaseJsonUtils.writeValue(fieldValue);
                                    fieldValue = BaseStringUtils.mosaic(text, jsonMosaic, '*');
                                }

                                map.put(StringUtils.isBlank(name)?field.getName():name + "." + field.getName(), toJson(fieldValue));
                                map.putAll(toMap(StringUtils.isBlank(name)?field.getName():name + "." + field.getName(), fieldValue, crtDepth + 1));
                            }

                        });
                    }
                }
            } else if(value instanceof Collection && ((Collection)value).size() > 0) {
                HashMap temp = new HashMap();
                ArrayList list = new ArrayList();
                ((Collection)value).forEach((item) -> {
                    list.add(toMap("", item));
                });
                list.forEach((innerMap) -> {
                    ((Map)innerMap).entrySet().forEach((entry) -> {
                        String key = (String)((Map.Entry)entry).getKey();
                        if(temp.containsKey(key)) {
                            temp.put(key, (String)temp.get(key) + "," + (String)((Map.Entry)entry).getValue());
                        } else {
                            temp.put(key, ((Map.Entry)entry).getValue());
                        }

                    });
                });
                temp.forEach((tempKey, tempValue) -> {
                    String var10000 = (String)map.put(StringUtils.isBlank(name)?tempKey:name + "." + tempKey, "[" + tempValue + "]");
                });
            }
        }

        return map;
    }

    static {
        jsonIgnoredClasses = BaseJsonUtils.jsonIgnoredClasses;
        mapper = new ObjectMapper();
        JacksonAnnotationIntrospector introspector = new JacksonAnnotationIntrospector() {
            @Override
            protected boolean _isIgnorable(Annotated a) {
                return a.getAnnotation(LogIgnore.class) != null?true:super._isIgnorable(a);
            }
            @Override
            public Object findSerializer(Annotated a) {
                JsonMosaic jsonMosaic = (JsonMosaic)a.getAnnotation(JsonMosaic.class);
                return jsonMosaic != null?new CustomSerializer(jsonMosaic):super.findSerializer(a);
            }
        };
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setAnnotationIntrospector(introspector);
        mapper.registerModule(BaseJsonUtils.getSimpleModule());
    }

    public static class IgnorableException extends RuntimeException {
        public IgnorableException() {
        }
    }
}

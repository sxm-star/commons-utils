package com.songxm.commons;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.songxm.commons.annotation.JsonMosaic;
import com.songxm.commons.serializer.CustomSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Slf4j
@SuppressWarnings("unchecked")
/**
 * @author songxm
 */
public class BaseJsonUtils {
    public static final Set<Class<?>> jsonIgnoredClasses = ImmutableSet.of(ServletRequest.class, ServletResponse.class, InputStream.class, OutputStream.class, RequestBody.class, ResponseBody.class, new Class[]{okhttp3.RequestBody.class, okhttp3.ResponseBody.class, Environment.class, MultipartFile.class, File.class, Logger.class, java.util.logging.Logger.class, Class.class});
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper mapperIgnoreUnknown = new ObjectMapper();
    private static final ObjectMapper mapperScanAnno = new ObjectMapper();
    private static SimpleModule module;
    private static String basePath;

    public BaseJsonUtils() {
    }

    public static ObjectMapper defaultMaspper() {
        return mapperIgnoreUnknown;
    }

    public static <T> T readValue(String s, Class<T> cls) {
        return readValue(s, cls, true);
    }

    public static <T> T readValueChecked(String s, Class<T> cls) throws Exception {
        return readValueChecked(s, cls, true);
    }

    public static <T> T readValue(String s, Class<T> cls, boolean ignoreUnknown) {
        Preconditions.checkArgument(cls != null, "Class类型不能为空");
        if(cls == String.class) {
            return (T)s;
        } else if(StringUtils.isBlank(s)) {
            return null;
        } else {
            try {
                return ignoreUnknown?mapperIgnoreUnknown.readValue(s, cls):mapper.readValue(s, cls);
            } catch (Throwable var4) {
                log.error("无法将{}转换为类型为[{}]的对象: {}", new Object[]{s, cls.getSimpleName(), ExceptionUtils.getStackTrace(var4)});
                return null;
            }
        }
    }

    public static <T> T readValueChecked(String s, Class<T> cls, boolean ignoreUnknown) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(s), "字串不能为空");
        Preconditions.checkArgument(cls != null, "Class类型不能为空");
        return cls == String.class?(T)s:(ignoreUnknown?mapperIgnoreUnknown.readValue(s, cls):mapper.readValue(s, cls));
    }

    public static <T> T readValue(String s, TypeReference typeReference) {
        return (T)readValue(s, typeReference, true);
    }

    public static <T> T readValue(String s, TypeReference typeReference, boolean ignoreUnknown) {
        Preconditions.checkArgument(StringUtils.isNotBlank(s), "字串不能为空");
        Preconditions.checkArgument(typeReference != null, "TypeWrapper类型不能为空");

        try {
            return (T)(ignoreUnknown?mapperIgnoreUnknown.readValue(s, typeReference):mapper.readValue(s, typeReference));
        } catch (Throwable var4) {
            log.error("无法将{}转换为类型为[{}]的对象: {}", new Object[]{s, typeReference.getType(), ExceptionUtils.getStackTrace(var4)});
            return null;
        }
    }

    public static <T> T readValue(Map<String, Object> map, Class<T> cls) {
        return readValue(map, cls, true);
    }

    public static <T> T readValue(Map<String, Object> map, Class<T> cls, boolean ignoreUnknown) {
        Preconditions.checkArgument(map != null, "map不能为null");
        Preconditions.checkArgument(cls != null, "Class类型不能为空");
        if(cls == String.class) {
            return (T) writeValue(map);
        } else {
            try {
                return ignoreUnknown?mapperIgnoreUnknown.convertValue(map, cls):mapper.convertValue(map, cls);
            } catch (Throwable e) {
                log.error("无法将Map{}转换为类型为[{}]的对象: {}", new Object[]{map, cls.getSimpleName(), ExceptionUtils.getStackTrace(e)});
                return null;
            }
        }
    }

    public static <T> List<T> readValues(String s, Class<T> cls) {
        return readValues(s, cls, true);
    }

    public static <T> List<T> readValues(String s, Class<T> cls, boolean ignoreUnknown) {
        Preconditions.checkArgument(StringUtils.isNotBlank(s), "字串不能为空");
        Preconditions.checkArgument(cls != null, "Class类型不能为空");

        List list;
        try {
            if(ignoreUnknown) {
                list = (List)mapperIgnoreUnknown.readValue(s, List.class);
            } else {
                list = (List)mapper.readValue(s, List.class);
            }
        } catch (Throwable var5) {
            log.error("无法将{}转换为数组对象: {}", s, ExceptionUtils.getStackTrace(var5));
            return Collections.emptyList();
        }

        return cls == List.class?list:(List)list.stream().map((ele) -> {
            return ele instanceof Map?readValue((Map)ele, cls):readValue(ele.toString(), cls);
        }).collect(Collectors.toList());
    }

    public static String writeValue(Object obj) {
        return writeValue(obj, false);
    }

    public static String writeValue(Object obj, boolean scanAnno) {
        if(obj == null) {
            return null;
        } else if(BaseObjectUtils.isPrimitive(obj.getClass())) {
            return obj.toString();
        } else {
            try {
                return scanAnno?mapperScanAnno.writeValueAsString(obj):mapper.writeValueAsString(obj);
            } catch (Throwable var3) {
                log.error("json转换异常", var3);
                return null;
            }
        }
    }

    public static Object valueFromJsonKey(String json, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(json), "json内容不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key不能为空");
        if(!json.startsWith("{") || !json.endsWith("}")) {
            json = StringUtils.substringBeforeLast(json, "}");
            json = StringUtils.substringAfter(json, "{");
            json = "{" + json + "}";
        }

        Map map = (Map)readValue(json, Map.class);
        return map.get(key);
    }

    public static SimpleModule getSimpleModule() {
        return module;
    }

    public static void generateCode(String rootClassName, String content, String packageName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "内容不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(packageName), "包名不能为空");
        Map jsonMap;
        if(StringUtils.startsWith(content, "[")) {
            jsonMap = (Map)readValues(content, Map.class).get(0);
        } else {
            jsonMap = (Map)readValue(content, Map.class);
        }

        generateCode("", rootClassName, jsonMap, packageName);
    }

    private static String generateCode(String parent, String name, Map<String, Object> jsonMap, String packageName) {
        File dir;
        try {
            dir = new File(basePath + "/src/main/java/" + packageName.replace('.', '/'));
            FileUtils.forceMkdir(dir);
        } catch (Throwable ex) {
            log.error("生成文件夹异常");
            throw new RuntimeException(ex);
        }

        FileInputStream in = null;
        name = BaseStringUtils.underScoreToCamel(name, true);
        File file = new File(dir, name + ".java");
        if(file.exists()) {
            try {
                in = new FileInputStream(file);
                String fileName = IOUtils.toString(in, Charsets.UTF_8);
                if(jsonMap.entrySet().stream().filter((entry) -> {
                    return !fileName.contains((CharSequence)entry.getKey());
                }).findAny().orElse((Entry<String, Object>)null) != null) {
                    name = BaseStringUtils.underScoreToCamel(parent, true) + name;
                    file = new File(dir, name + ".java");
                }
            } catch (Throwable var30) {
                log.error("读取文件内容异常", var30);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        try {
            FileUtils.touch(file);
        } catch (Throwable var29) {
            log.error("json内容转文件异常", var29);
        }

        StringBuilder bufFirst = (new StringBuilder("package ")).append(packageName).append(";\n\n");
        StringBuilder bufHeader = new StringBuilder("import lombok.Data;\n\n");
        AtomicBoolean hasExtraHeader = new AtomicBoolean(false);
        StringBuilder bufFooter = new StringBuilder("@Data\n");
        bufFooter.append("public class ").append(name).append(" {\n");
        final String codeName = name;
        jsonMap.entrySet().forEach((entry) -> {
            BaseJsonUtils.JsonStructure jsonStructure = flatJsonValue(entry.getValue());
            String key = (String)entry.getKey();
            String standardKey = BaseStringUtils.underScoreToCamel(key, false);
            Object value = jsonStructure.getValue();
            String type;
            if(value instanceof Map) {
                type = BaseStringUtils.underScoreToCamel(key, true);
                if(jsonStructure.getDepth() > 0) {
                    type = BaseStringUtils.singularize(type);
                    if(!StringUtils.endsWithIgnoreCase(standardKey, "list") && !StringUtils.endsWithIgnoreCase(standardKey, "set")) {
                        standardKey = BaseStringUtils.pluralize(standardKey);
                    }
                }
               
                type = generateCode(codeName, type, (Map)value, packageName);
            } else if(value instanceof String) {
                Class cls = BaseObjectUtils.forName((String)value, true);
                type = cls == null?"String":cls.getSimpleName();
            } else {
                type = value.getClass().getSimpleName();
            }

            if(!key.equals(standardKey)) {
                if(bufFirst.indexOf("import com.fasterxml.jackson.annotation.JsonProperty") == -1) {
                    bufFirst.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
                }

                bufFooter.append("    @JsonProperty(\"").append(key).append("\")\n");
            }

            if(type.equals("BigDecimal") && bufHeader.indexOf("import java.math.BigDecimal") == -1) {
                hasExtraHeader.set(true);
                bufHeader.append("import java.math.BigDecimal;\n");
            } else if(type.equals("Date") && bufHeader.indexOf("import java.util.Date") == -1) {
                hasExtraHeader.set(true);
                bufHeader.append("import java.util.Date;\n");
            }

            if(jsonStructure.getDepth() > 0 && bufHeader.indexOf("import java.util.List") == -1) {
                hasExtraHeader.set(true);
                bufHeader.append("import java.util.List;\n");
            }

            bufFooter.append("    private ").append(wrapWithDepth(jsonStructure.getDepth(), type)).append(" ").append(standardKey).append(";\n");
        });
        bufFooter.append("}");
        if(hasExtraHeader.get()) {
            bufHeader.append("\n");
        }

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            IOUtils.write(bufFirst.append(bufHeader).append(bufFooter).toString(), out, Charsets.UTF_8);
        } catch (Throwable var27) {
            log.error("json内容转文件异常", var27);
        } finally {
            IOUtils.closeQuietly(out);
        }

        return name;
    }

    private static BaseJsonUtils.JsonStructure flatJsonValue(Object value) {
        if(value instanceof List) {
            Object item = ((List)value).get(0);
            BaseJsonUtils.JsonStructure jsonStructure = flatJsonValue(item);
            jsonStructure.setDepth(jsonStructure.getDepth() + 1);
            return jsonStructure;
        } else {
            return new BaseJsonUtils.JsonStructure(value);
        }
    }

    private static String wrapWithDepth(int depth, String type) {
        if(depth == 0) {
            return type;
        } else {
            StringBuilder buf = new StringBuilder("List");
            IntStream.range(0, depth).forEach((i) -> {
                buf.append("<");
            });
            buf.append(type);
            IntStream.range(0, depth).forEach((i) -> {
                buf.append(">");
            });
            return buf.toString();
        }
    }

    static {
        SimpleSerializers simpleSerializers = new SimpleSerializers();
        jsonIgnoredClasses.forEach((cls) -> {
            simpleSerializers.addSerializer(new BaseJsonUtils.ClassNameSerializer(cls));
        });
        module = new SimpleModule();
        module.setSerializers(simpleSerializers);
        JacksonAnnotationIntrospector introspector = new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated a) {
                JsonMosaic jsonMosaic = (JsonMosaic)a.getAnnotation(JsonMosaic.class);
                return jsonMosaic != null?new CustomSerializer(jsonMosaic):super.findSerializer(a);
            }
        };
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"));
        mapper.registerModule(module);
        mapperIgnoreUnknown.setSerializationInclusion(Include.NON_NULL);
        mapperIgnoreUnknown.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapperIgnoreUnknown.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapperIgnoreUnknown.setDateFormat(new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"));
        mapperIgnoreUnknown.registerModule(module);
        mapperScanAnno.setSerializationInclusion(Include.NON_NULL);
        mapperScanAnno.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapperScanAnno.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapperScanAnno.setDateFormat(new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"));
        mapperScanAnno.setAnnotationIntrospector(introspector);
        mapperScanAnno.registerModule(module);
        URL baseUrl = BaseJsonUtils.class.getClassLoader().getResource(".");

        try {
            if(baseUrl != null && StringUtils.isNotBlank(baseUrl.getPath())) {
                basePath = URLDecoder.decode(baseUrl.getPath(), "UTF-8");
                basePath = StringUtils.substringBefore(basePath, "/target");
            }
        } catch (Exception var4) {
            log.info("获取项目路径异常");
        }

    }

    public static class ClassNameSerializer extends StdSerializer {
        private Class cls;

        public ClassNameSerializer(Class cls) {
            super(cls);
            this.cls = cls;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if(value == null) {
                gen.writeString("null");
            } else {
                gen.writeString(this.cls.getSimpleName());
            }

        }
    }

    static class JsonStructure {
        private Object value;
        private int depth = 0;

        public JsonStructure(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }

        public int getDepth() {
            return this.depth;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) {
                return true;
            } else if(!(o instanceof BaseJsonUtils.JsonStructure)) {
                return false;
            } else {
                BaseJsonUtils.JsonStructure other = (BaseJsonUtils.JsonStructure)o;
                if(!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$value = this.getValue();
                    Object other$value = other.getValue();
                    if(this$value == null) {
                        if(other$value == null) {
                            return this.getDepth() == other.getDepth();
                        }
                    } else if(this$value.equals(other$value)) {
                        return this.getDepth() == other.getDepth();
                    }

                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof BaseJsonUtils.JsonStructure;
        }

        @Override
        public int hashCode() {
            boolean PRIME = true;
            byte result = 1;
            Object $value = this.getValue();
            int result1 = result * 59 + ($value == null?0:$value.hashCode());
            result1 = result1 * 59 + this.getDepth();
            return result1;
        }

        @Override
        public String toString() {
            return "BaseJsonUtils.JsonStructure(value=" + this.getValue() + ", depth=" + this.getDepth() + ")";
        }
    }
}

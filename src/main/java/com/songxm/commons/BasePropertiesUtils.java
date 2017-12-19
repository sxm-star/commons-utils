package com.songxm.commons;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.net.URL;
import java.util.Properties;

/**
 * @author songxm
 */
@Slf4j
public class BasePropertiesUtils {
    public BasePropertiesUtils() {
    }

    public static Properties load(String fileName) {
        Properties props = null;

        try {
            URL e = BasePropertiesUtils.class.getClassLoader().getResource(fileName);
            if(e != null) {
                props = PropertiesLoaderUtils.loadProperties(new EncodedResource(new UrlResource(e), "utf-8"));
            }
        } catch (Throwable e) {
            log.error("加载配置文件[{}]异常: {}", fileName, ExceptionUtils.getStackTrace(e));
        }

        if(props == null) {
            log.error("找不到配置文件:" + fileName);
        }

        return props;
    }
}

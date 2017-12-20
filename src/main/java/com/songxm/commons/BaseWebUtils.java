package com.songxm.commons;
import com.auth0.jwt.internal.org.apache.commons.io.IOUtils;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.IntStream;

public class BaseWebUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseWebUtils.class);
    private static Set<String> IGNORED_HEADERS = ImmutableSet.of("accept-charset", "accept-encoding", "accept-language", "accept-ranges", "age", "allow", new String[]{"cache-control", "connection", "content-encoding", "content-language", "content-length", "content-location", "content-md5", "content-range", "date", "dav", "depth", "destination", "etag", "expect", "expires", "from", "host", "if", "if-match", "if-modified-since", "if-none-match", "if-range", "if-unmodified-since", "last-modified", "location", "lock-token", "max-forwards", "overwrite", "pragma", "proxy-authenticate", "proxy-authorization", "range", "referer", "retry-after", "server", "status-uri", "te", "timeout", "trailer", "transfer-encoding", "upgrade", "user-agent", "vary", "via", "warning", "www-authenticate"});

    public BaseWebUtils() {
    }

    public static Method getMethod(RequestMappingHandlerMapping mapping, ServletRequest servletRequest) {
        if(mapping != null && servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest)servletRequest;

            try {
                HandlerExecutionChain e = mapping.getHandler(request);
                if(e == null) {
                    return null;
                } else {
                    Object handlerMethod = e.getHandler();
                    return handlerMethod != null && handlerMethod instanceof HandlerMethod?((HandlerMethod)handlerMethod).getMethod():null;
                }
            } catch (Throwable var5) {
                log.error("获取handler method异常", var5);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String requestKey(HttpServletRequest request) throws IOException {
        String sep = ":";
        if(request == null) {
            return null;
        } else {
            String method = request.getMethod();
            StringBuilder buf = new StringBuilder(method);
            buf.append(sep).append(request.getRequestURL());
            if(StringUtils.isNotBlank(request.getQueryString())) {
                buf.append(sep).append(request.getQueryString());
            }

            ArrayList headerNames = new ArrayList();
            Enumeration enumeration = request.getHeaderNames();

            String body;
            while(enumeration != null && enumeration.hasMoreElements()) {
                body = (String)enumeration.nextElement();
                if(!IGNORED_HEADERS.contains(body.toLowerCase())) {
                    headerNames.add(body);
                }
            }

            if(!CollectionUtils.isEmpty(headerNames)) {
                buf.append(sep);
            }

            IntStream.range(0, headerNames.size()).forEach((index) -> {
                String header = (String)headerNames.get(index);
                if(index != 0) {
                    buf.append(";");
                }

                buf.append(header).append("=").append(request.getHeader(header));
            });
            if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post") || method.equalsIgnoreCase("patch")) {
                body = IOUtils.toString(request.getReader());
                if(StringUtils.isNotBlank(body)) {
                    buf.append(sep).append(body);
                }
            }

            return buf.toString().replaceAll("\\r?\\n", "");
        }
    }
}

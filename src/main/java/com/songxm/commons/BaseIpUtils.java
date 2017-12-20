package com.songxm.commons;

import com.google.common.base.Preconditions;
import com.songxm.commons.exception.IpInvalidException;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BaseIpUtils {
    private static final String[] HEADERS_TO_TRY = new String[]{"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};

    public BaseIpUtils() {
    }

    public static String getIp(HttpServletRequest request) {
        Preconditions.checkArgument(request != null, "request不能为空");
        String[] var1 = HEADERS_TO_TRY;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String header = var1[var3];
            String ip = request.getHeader(header);
            if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    public static boolean isPublic(String ip) {
        Preconditions.checkArgument(StringUtils.isNotBlank(ip), "ip地址不能为空");

        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException var3) {
            throw new IpInvalidException(ip);
        }

        return !address.isLoopbackAddress() && !address.isSiteLocalAddress();
    }

    public static boolean isLocal(String ip) {
        Preconditions.checkArgument(StringUtils.isNotBlank(ip), "ip地址不能为空");

        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException var3) {
            throw new IpInvalidException(ip);
        }

        return address.isLoopbackAddress();
    }
}

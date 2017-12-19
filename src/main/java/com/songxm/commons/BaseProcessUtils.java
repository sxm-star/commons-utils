package com.songxm.commons;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class BaseProcessUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseProcessUtils.class);

    public BaseProcessUtils() {
    }

    public static String run(String... commands) {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        Process process;
        try {
            process = processBuilder.redirectErrorStream(true).start();
        } catch (Exception var12) {
            throw new RuntimeException("启动进程" + Arrays.asList(commands) + "异常", var12);
        }

        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), Charsets.toCharset("UTF-8")));

        try {
            String errorCode;
            try {
                while((errorCode = in.readLine()) != null) {
                    buf.append(errorCode);
                }
            } catch (Exception var13) {
                throw new RuntimeException("从process中读取数据异常", var13);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        int errorCode1;
        try {
            errorCode1 = process.waitFor();
        } catch (Throwable var11) {
            log.error("获取进程结果异常");
            throw new RuntimeException(var11);
        }

        if(errorCode1 == 0) {
            return buf.toString();
        } else {
            throw new RuntimeException(String.format("process执行异常.错误码[%s],异常信息:%s", new Object[]{Integer.valueOf(errorCode1), buf.toString()}));
        }
    }
}

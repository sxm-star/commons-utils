package com.songxm.commons;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BaseZipUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseZipUtils.class);

    public BaseZipUtils() {
    }

    public static String zip(String content) {
        return StringUtils.isBlank(content)?content:new String(zip(content.getBytes(Charsets.UTF_8)), Charsets.ISO_8859_1);
    }

    public static String unzip(String content) {
        return StringUtils.isBlank(content)?content:new String(unzip(content.getBytes(Charsets.ISO_8859_1)), Charsets.UTF_8);
    }

    public static byte[] zip(byte[] content) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(1024);
        GZIPOutputStream output = null;

        try {
            output = new GZIPOutputStream(byteOutput);
            output.write(content);
        } catch (Throwable var7) {
            throw new RuntimeException("gzip压缩异常", var7);
        } finally {
            IOUtils.closeQuietly(output);
        }

        return byteOutput.toByteArray();
    }

    public static byte[] unzip(byte[] content) {
        GZIPInputStream in = null;

        byte[] e;
        try {
            in = new GZIPInputStream(new ByteArrayInputStream(content));
            e = IOUtils.toByteArray(in);
        } catch (Throwable var6) {
            throw new RuntimeException("unzip解压缩异常", var6);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return e;
    }
}

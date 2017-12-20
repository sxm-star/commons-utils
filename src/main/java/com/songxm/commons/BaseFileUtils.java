package com.songxm.commons;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.songxm.commons.model.DirInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
@SuppressWarnings("unchecked")
public class BaseFileUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseFileUtils.class);

    public BaseFileUtils() {
    }

    public static DirInfo getDirInfo(String dir, String fileType) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(dir), "搜索路径不能为空");
        DirInfo dirInfo = new DirInfo("");
        String rootPath = URLDecoder.decode(BaseFileUtils.class.getClassLoader().getResource(dir).getPath(), "UTF-8");
        if(rootPath.contains("!")) {
            JarFile jar = new JarFile(rootPath.substring(rootPath.indexOf(":" + File.separator) + 1, rootPath.indexOf("!")));
            Enumeration entries = jar.entries();

            while(entries.hasMoreElements()) {
                String path = ((JarEntry)entries.nextElement()).getName();
                if(path.contains(dir)) {
                    String relativePath = path.substring(path.indexOf(dir) + dir.length());
                    if(relativePath.endsWith(fileType)) {
                        int index = relativePath.lastIndexOf(File.separator);
                        String relativeDir = relativePath.substring(0, index);
                        dirInfo.createFile(relativeDir, nameToFile(dir + relativePath));
                    } else {
                        dirInfo.createPath(relativePath);
                    }
                }
            }

            jar.close();
        } else {
            handleDir(dirInfo, new File(rootPath), fileType);
        }

        return dirInfo;
    }

    public static String fileToString(String file) {
        return fileToString(file, Charsets.UTF_8);
    }

    public static String fileToString(String file, Charset charset) {
        try {
            return fileToString(nameToFile(file), charset);
        } catch (Throwable var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String fileToString(File file) {
        return fileToString(file, Charsets.UTF_8);
    }

    public static String fileToString(File file, Charset charset) {
        try {
            if(!file.isFile()) {
                String e = "!" + File.separator;
                int index = file.getPath().lastIndexOf(e);
                index = index == -1?0:index + e.length();
                String path = file.getPath().substring(index);
                List lines = IOUtils.readLines(BaseFileUtils.class.getClassLoader().getResourceAsStream(path), charset);
                return StringUtils.join(lines, System.lineSeparator());
            } else {
                return Files.toString(file, charset);
            }
        } catch (Exception var6) {
            throw new RuntimeException(var6.getMessage());
        }
    }

    private static File nameToFile(String name) throws Exception {
        URL url = BaseFileUtils.class.getClassLoader().getResource(name);
        if(url == null) {
            throw new RuntimeException("无法找到" + name + "对应的文件");
        } else {
            String rootPath = url.getPath();
            return new File(URLDecoder.decode(rootPath, "UTF-8"));
        }
    }

    private static void handleDir(DirInfo dirInfo, File file, String fileType) {
        if(file != null && file.isDirectory() && dirInfo != null && dirInfo.getFiles() != null) {
            dirInfo.getFiles().addAll(Arrays.asList(file.listFiles(fileFilter(fileType))));
            Stream.of(file.listFiles(dirFilter())).forEach((innerDir) -> {
                handleDir(dirInfo.createPath(innerDir.getName()), innerDir, fileType);
            });
        }

    }

    private static FilenameFilter fileFilter(String fileType) {
        return (dir, name) -> {
            return name.contains(fileType);
        };
    }

    private static FilenameFilter dirFilter() {
        return (dir, name) -> {
            File file = new File(dir, name);
            return file.isDirectory();
        };
    }
}

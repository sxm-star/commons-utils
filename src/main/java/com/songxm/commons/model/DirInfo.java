package com.songxm.commons.model;

import com.songxm.commons.BaseJsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
@SuppressWarnings("unchecked")
public class DirInfo {
    private String relativePath;
    private Map<String, DirInfo> childDirs = new HashMap(0);
    private List<File> files = new ArrayList(0);

    public DirInfo() {
    }

    public DirInfo(String relativePath) {
        this.relativePath = relativePath;
    }

    public DirInfo createPath(String path) {
        path = StringUtils.isNoneBlank(new CharSequence[]{path})?path.replaceFirst("^" + File.separator, ""):path;
        path = StringUtils.isNoneBlank(new CharSequence[]{path})?path.replaceFirst(File.separator + "$", ""):path;
        if(StringUtils.isBlank(path)) {
            return this;
        } else {
            int index = path.indexOf(File.separator);
            DirInfo dirInfo;
            if(index == -1) {
                if(!this.childDirs.containsKey(path)) {
                    this.getChildDirs().put(path, new DirInfo(this.relativePath + File.separator + path));
                }

                dirInfo = (DirInfo)this.getChildDirs().get(path);
            } else {
                String childPath = path.substring(0, index);
                if(!this.childDirs.containsKey(childPath)) {
                    this.getChildDirs().put(childPath, new DirInfo(this.relativePath + File.separator + childPath));
                }

                dirInfo = ((DirInfo)this.getChildDirs().get(childPath)).createPath(path.substring(index + File.separator.length()));
            }

            return dirInfo;
        }
    }

    public void createFile(String path, File file) {
        if(!StringUtils.isBlank(path) && !path.equals(File.separator)) {
            DirInfo dirInfo = this.createPath(path);
            dirInfo.getFiles().add(file);
        } else {
            this.getFiles().add(file);
        }

    }

    public Set<String> getDirs(String path) {
        DirInfo dirInfo = this.getDirInfo(path);
        return dirInfo != null?dirInfo.getChildDirs().keySet():Collections.EMPTY_SET;
    }

    public List<File> getFiles(String path) {
        DirInfo dirInfo = this.getDirInfo(path);
        return dirInfo != null?dirInfo.getFiles():Collections.emptyList();
    }

    public String toString() {
        return BaseJsonUtils.writeValue(this);
    }

    private DirInfo getDirInfo(String path) {
        path = StringUtils.isNoneBlank(new CharSequence[]{path})?path.replaceFirst("^" + File.separator, ""):path;
        path = StringUtils.isNoneBlank(new CharSequence[]{path})?path.replaceFirst(File.separator + "$", ""):path;
        DirInfo dirInfo = this;
        if(StringUtils.isNotBlank(path)) {
            String fileSep = File.separator.equals("\\")?"\\\\":File.separator;
            String[] var4 = path.split(fileSep);
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String crtPath = var4[var6];
                dirInfo = (DirInfo)dirInfo.getChildDirs().get(crtPath);
            }
        }

        return dirInfo;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public Map<String, DirInfo> getChildDirs() {
        return this.childDirs;
    }

    public List<File> getFiles() {
        return this.files;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setChildDirs(Map<String, DirInfo> childDirs) {
        this.childDirs = childDirs;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DirInfo)) {
            return false;
        } else {
            DirInfo other = (DirInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label47: {
                    String this$relativePath = this.getRelativePath();
                    String other$relativePath = other.getRelativePath();
                    if(this$relativePath == null) {
                        if(other$relativePath == null) {
                            break label47;
                        }
                    } else if(this$relativePath.equals(other$relativePath)) {
                        break label47;
                    }

                    return false;
                }

                Map this$childDirs = this.getChildDirs();
                Map other$childDirs = other.getChildDirs();
                if(this$childDirs == null) {
                    if(other$childDirs != null) {
                        return false;
                    }
                } else if(!this$childDirs.equals(other$childDirs)) {
                    return false;
                }

                List this$files = this.getFiles();
                List other$files = other.getFiles();
                if(this$files == null) {
                    if(other$files != null) {
                        return false;
                    }
                } else if(!this$files.equals(other$files)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DirInfo;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $relativePath = this.getRelativePath();
        int result1 = result * 59 + ($relativePath == null?0:$relativePath.hashCode());
        Map $childDirs = this.getChildDirs();
        result1 = result1 * 59 + ($childDirs == null?0:$childDirs.hashCode());
        List $files = this.getFiles();
        result1 = result1 * 59 + ($files == null?0:$files.hashCode());
        return result1;
    }
}

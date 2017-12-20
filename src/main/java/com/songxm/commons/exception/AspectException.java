package com.songxm.commons.exception;

import java.util.Arrays;

public class AspectException extends RuntimeException {
    private String errorKey;
    private String[] args;
    private boolean logError = true;

    public AspectException(String errorKey, boolean logAsError, String... args) {
        this.errorKey = errorKey;
        this.args = args;
        this.logError = logAsError;
    }

    public static AspectException fromKey(String key, String... args) {
        return fromKey(key, true, args);
    }

    public static AspectException fromKey(String key, boolean logAsError, String... args) {
        return new AspectException(key, logAsError, args);
    }

    public String getErrorKey() {
        return this.errorKey;
    }

    public String[] getArgs() {
        return this.args;
    }

    public boolean isLogError() {
        return this.logError;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public void setLogError(boolean logError) {
        this.logError = logError;
    }
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof AspectException)) {
            return false;
        } else {
            AspectException other = (AspectException)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                String this$errorKey = this.getErrorKey();
                String other$errorKey = other.getErrorKey();
                if(this$errorKey == null) {
                    if(other$errorKey == null) {
                        return !Arrays.deepEquals(this.getArgs(), other.getArgs())?false:this.isLogError() == other.isLogError();
                    }
                } else if(this$errorKey.equals(other$errorKey)) {
                    return !Arrays.deepEquals(this.getArgs(), other.getArgs())?false:this.isLogError() == other.isLogError();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof AspectException;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $errorKey = this.getErrorKey();
        int result1 = result * 59 + ($errorKey == null?0:$errorKey.hashCode());
        result1 = result1 * 59 + Arrays.deepHashCode(this.getArgs());
        result1 = result1 * 59 + (this.isLogError()?79:97);
        return result1;
    }
    @Override
    public String toString() {
        return "AspectException(errorKey=" + this.getErrorKey() + ", args=" + Arrays.deepToString(this.getArgs()) + ", logError=" + this.isLogError() + ")";
    }
}

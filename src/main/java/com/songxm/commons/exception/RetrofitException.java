package com.songxm.commons.exception;

public class RetrofitException extends RuntimeException {
    private Integer code;
    private String message;

    public RetrofitException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }
    @Override
    public String getMessage() {
        return this.message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof RetrofitException)) {
            return false;
        } else {
            RetrofitException other = (RetrofitException)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                Integer this$code = this.getCode();
                Integer other$code = other.getCode();
                if(this$code == null) {
                    if(other$code != null) {
                        return false;
                    }
                } else if(!this$code.equals(other$code)) {
                    return false;
                }

                String this$message = this.getMessage();
                String other$message = other.getMessage();
                if(this$message == null) {
                    if(other$message != null) {
                        return false;
                    }
                } else if(!this$message.equals(other$message)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof RetrofitException;
    }
    @Override
    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        Integer $code = this.getCode();
        int result1 = result * 59 + ($code == null?0:$code.hashCode());
        String $message = this.getMessage();
        result1 = result1 * 59 + ($message == null?0:$message.hashCode());
        return result1;
    }
    @Override
    public String toString() {
        return "RetrofitException(code=" + this.getCode() + ", message=" + this.getMessage() + ")";
    }
}

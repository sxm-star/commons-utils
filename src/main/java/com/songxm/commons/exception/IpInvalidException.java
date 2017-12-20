package com.songxm.commons.exception;

public class IpInvalidException extends RuntimeException {
    private String ip;

    public IpInvalidException(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof IpInvalidException)) {
            return false;
        } else {
            IpInvalidException other = (IpInvalidException)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                String this$ip = this.getIp();
                String other$ip = other.getIp();
                if(this$ip == null) {
                    if(other$ip != null) {
                        return false;
                    }
                } else if(!this$ip.equals(other$ip)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof IpInvalidException;
    }
    @Override
    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $ip = this.getIp();
        int result1 = result * 59 + ($ip == null?0:$ip.hashCode());
        return result1;
    }
    @Override
    public String toString() {
        return "IpInvalidException(ip=" + this.getIp() + ")";
    }
}


package com.songxm.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JwtToken {
    private String token;
    @JsonProperty("exp")
    private Long expireAt;

    public JwtToken() {
    }

    public String getToken() {
        return this.token;
    }

    public Long getExpireAt() {
        return this.expireAt;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof JwtToken)) {
            return false;
        } else {
            JwtToken other = (JwtToken)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                String this$token = this.getToken();
                String other$token = other.getToken();
                if(this$token == null) {
                    if(other$token != null) {
                        return false;
                    }
                } else if(!this$token.equals(other$token)) {
                    return false;
                }

                Long this$expireAt = this.getExpireAt();
                Long other$expireAt = other.getExpireAt();
                if(this$expireAt == null) {
                    if(other$expireAt != null) {
                        return false;
                    }
                } else if(!this$expireAt.equals(other$expireAt)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof JwtToken;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $token = this.getToken();
        int result1 = result * 59 + ($token == null?0:$token.hashCode());
        Long $expireAt = this.getExpireAt();
        result1 = result1 * 59 + ($expireAt == null?0:$expireAt.hashCode());
        return result1;
    }

    public String toString() {
        return "JwtToken(token=" + this.getToken() + ", expireAt=" + this.getExpireAt() + ")";
    }
}

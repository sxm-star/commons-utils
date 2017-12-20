
package com.songxm.commons.model;

public class DbInfo {
    private String url;
    private String user;
    private String pass;

    public DbInfo(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUser() {
        return this.user;
    }

    public String getPass() {
        return this.pass;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof DbInfo)) {
            return false;
        } else {
            DbInfo other = (DbInfo)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label47: {
                    String this$url = this.getUrl();
                    String other$url = other.getUrl();
                    if(this$url == null) {
                        if(other$url == null) {
                            break label47;
                        }
                    } else if(this$url.equals(other$url)) {
                        break label47;
                    }

                    return false;
                }

                String this$user = this.getUser();
                String other$user = other.getUser();
                if(this$user == null) {
                    if(other$user != null) {
                        return false;
                    }
                } else if(!this$user.equals(other$user)) {
                    return false;
                }

                String this$pass = this.getPass();
                String other$pass = other.getPass();
                if(this$pass == null) {
                    if(other$pass != null) {
                        return false;
                    }
                } else if(!this$pass.equals(other$pass)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof DbInfo;
    }
    @Override
    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $url = this.getUrl();
        int result1 = result * 59 + ($url == null?0:$url.hashCode());
        String $user = this.getUser();
        result1 = result1 * 59 + ($user == null?0:$user.hashCode());
        String $pass = this.getPass();
        result1 = result1 * 59 + ($pass == null?0:$pass.hashCode());
        return result1;
    }
    @Override
    public String toString() {
        return "DbInfo(url=" + this.getUrl() + ", user=" + this.getUser() + ", pass=" + this.getPass() + ")";
    }
}

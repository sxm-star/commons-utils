
package com.songxm.commons.model;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("unchecked")
public class JdbcResult {
    private String sql;
    private List<Object[]> paramsList;

    public JdbcResult(String sql, Object[] params) {
        this.sql = sql;
        this.paramsList = new ArrayList();
        this.paramsList.add(params);
    }

    public JdbcResult(String sql, List<Object[]> paramsList) {
        this.sql = sql;
        this.paramsList = paramsList;
    }

    public String getSql() {
        return this.sql;
    }

    public Object[] getParams() {
        return (Object[])this.paramsList.get(0);
    }

    public List<Object[]> getBatchParams() {
        return this.paramsList;
    }

    public List<Object[]> getParamsList() {
        return this.paramsList;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setParamsList(List<Object[]> paramsList) {
        this.paramsList = paramsList;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof JdbcResult)) {
            return false;
        } else {
            JdbcResult other = (JdbcResult)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                String this$sql = this.getSql();
                String other$sql = other.getSql();
                if(this$sql == null) {
                    if(other$sql != null) {
                        return false;
                    }
                } else if(!this$sql.equals(other$sql)) {
                    return false;
                }

                List this$paramsList = this.getParamsList();
                List other$paramsList = other.getParamsList();
                if(this$paramsList == null) {
                    if(other$paramsList != null) {
                        return false;
                    }
                } else if(!this$paramsList.equals(other$paramsList)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof JdbcResult;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        String $sql = this.getSql();
        int result1 = result * 59 + ($sql == null?0:$sql.hashCode());
        List $paramsList = this.getParamsList();
        result1 = result1 * 59 + ($paramsList == null?0:$paramsList.hashCode());
        return result1;
    }

    public String toString() {
        return "JdbcResult(sql=" + this.getSql() + ", paramsList=" + this.getParamsList() + ")";
    }
}

package com.songxm.commons.model;
import java.util.Collections;
import java.util.List;

public class PageResponse<T> {
    private Integer total;
    private List<T> datas = Collections.emptyList();

    public PageResponse(List<T> datas) {
        if(datas == null) {
            datas = Collections.emptyList();
        }

        this.datas = datas;
    }

    public PageResponse(Integer total, List<T> datas) {
        if(datas == null) {
            datas = Collections.emptyList();
        }

        this.datas = datas;
        this.total = total;
    }

    public Integer getTotal() {
        return this.total;
    }

    public List<T> getDatas() {
        return this.datas;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof PageResponse)) {
            return false;
        } else {
            PageResponse other = (PageResponse)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                Integer this$total = this.getTotal();
                Integer other$total = other.getTotal();
                if(this$total == null) {
                    if(other$total != null) {
                        return false;
                    }
                } else if(!this$total.equals(other$total)) {
                    return false;
                }

                List this$datas = this.getDatas();
                List other$datas = other.getDatas();
                if(this$datas == null) {
                    if(other$datas != null) {
                        return false;
                    }
                } else if(!this$datas.equals(other$datas)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageResponse;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        Integer $total = this.getTotal();
        int result1 = result * 59 + ($total == null?0:$total.hashCode());
        List $datas = this.getDatas();
        result1 = result1 * 59 + ($datas == null?0:$datas.hashCode());
        return result1;
    }

    public String toString() {
        return "PageResponse(total=" + this.getTotal() + ", datas=" + this.getDatas() + ")";
    }

    public PageResponse() {
    }
}

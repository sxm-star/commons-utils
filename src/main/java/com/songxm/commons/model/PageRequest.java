
package com.songxm.commons.model;

public class PageRequest {
    private Integer page;
    private Integer pageSize;
    private String orderedCol;
    private PageRequest.Order order;

    public PageRequest() {
        this.order = PageRequest.Order.DESC;
    }

    protected PageRequest(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        this.order = PageRequest.Order.DESC;
    }

    public static PageRequest of(Integer page, Integer pageSize) {
        return new PageRequest(page, pageSize);
    }

    public PageRequest orderBy(String orderedCol) {
        this.orderedCol = orderedCol;
        return this;
    }

    public PageRequest orderBy(String orderedCol, PageRequest.Order order) {
        this.orderedCol = orderedCol;
        this.order = order;
        return this;
    }

    public Integer getPage() {
        return this.page;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public String getOrderedCol() {
        return this.orderedCol;
    }

    public PageRequest.Order getOrder() {
        return this.order;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setOrderedCol(String orderedCol) {
        this.orderedCol = orderedCol;
    }

    public void setOrder(PageRequest.Order order) {
        this.order = order;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(!(o instanceof PageRequest)) {
            return false;
        } else {
            PageRequest other = (PageRequest)o;
            if(!other.canEqual(this)) {
                return false;
            } else {
                label59: {
                    Integer this$page = this.getPage();
                    Integer other$page = other.getPage();
                    if(this$page == null) {
                        if(other$page == null) {
                            break label59;
                        }
                    } else if(this$page.equals(other$page)) {
                        break label59;
                    }

                    return false;
                }

                Integer this$pageSize = this.getPageSize();
                Integer other$pageSize = other.getPageSize();
                if(this$pageSize == null) {
                    if(other$pageSize != null) {
                        return false;
                    }
                } else if(!this$pageSize.equals(other$pageSize)) {
                    return false;
                }

                String this$orderedCol = this.getOrderedCol();
                String other$orderedCol = other.getOrderedCol();
                if(this$orderedCol == null) {
                    if(other$orderedCol != null) {
                        return false;
                    }
                } else if(!this$orderedCol.equals(other$orderedCol)) {
                    return false;
                }

                PageRequest.Order this$order = this.getOrder();
                PageRequest.Order other$order = other.getOrder();
                if(this$order == null) {
                    if(other$order != null) {
                        return false;
                    }
                } else if(!this$order.equals(other$order)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageRequest;
    }

    public int hashCode() {
        boolean PRIME = true;
        byte result = 1;
        Integer $page = this.getPage();
        int result1 = result * 59 + ($page == null?0:$page.hashCode());
        Integer $pageSize = this.getPageSize();
        result1 = result1 * 59 + ($pageSize == null?0:$pageSize.hashCode());
        String $orderedCol = this.getOrderedCol();
        result1 = result1 * 59 + ($orderedCol == null?0:$orderedCol.hashCode());
        PageRequest.Order $order = this.getOrder();
        result1 = result1 * 59 + ($order == null?0:$order.hashCode());
        return result1;
    }

    public String toString() {
        return "PageRequest(page=" + this.getPage() + ", pageSize=" + this.getPageSize() + ", orderedCol=" + this.getOrderedCol() + ", order=" + this.getOrder() + ")";
    }

    public static enum Order {
        ASC,
        DESC;

        private Order() {
        }
    }
}

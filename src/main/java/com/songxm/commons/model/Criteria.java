
package com.songxm.commons.model;

import java.util.List;

public class Criteria {
    private String colName;
    private Criteria.Type type;
    private Object value;

    protected Criteria(String colName) {
        this.colName = colName;
    }

    public static Criteria column(String colName) {
        return new Criteria(colName);
    }

    public Criteria eq(Object value) {
        this.type = Criteria.Type.EQ;
        this.value = value;
        return this;
    }

    public Criteria ne(Object value) {
        this.type = Criteria.Type.NE;
        this.value = value;
        return this;
    }

    public Criteria gt(Object value) {
        this.type = Criteria.Type.GT;
        this.value = value;
        return this;
    }

    public Criteria ge(Object value) {
        this.type = Criteria.Type.GE;
        this.value = value;
        return this;
    }

    public Criteria lt(Object value) {
        this.type = Criteria.Type.LT;
        this.value = value;
        return this;
    }

    public Criteria le(Object value) {
        this.type = Criteria.Type.LE;
        this.value = value;
        return this;
    }

    public Criteria in(List value) {
        this.type = Criteria.Type.IN;
        this.value = value;
        return this;
    }

    public Criteria nin(List value) {
        this.type = Criteria.Type.NIN;
        this.value = value;
        return this;
    }

    public Criteria like(Object value) {
        this.type = Criteria.Type.LIKE;
        this.value = value;
        return this;
    }

    public Criteria isNull() {
        this.type = Criteria.Type.IS_NULL;
        return this;
    }

    public Criteria isNotNull() {
        this.type = Criteria.Type.IS_NOT_NULL;
        return this;
    }

    public String getColName() {
        return this.colName;
    }

    public Criteria.Type getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public static enum Type {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        IN,
        NIN,
        LIKE,
        IS_NULL,
        IS_NOT_NULL;

        private Type() {
        }
    }
}

package com.test.model;

import java.math.BigDecimal;

public class Distance {
    private Integer id;
    private Integer from;
    private Integer to;
    private BigDecimal distance;
    private String unite;

    public Distance() {
    }

    public Distance(Integer id, Integer from, Integer to, BigDecimal distance, String unite) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.unite = unite;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }
}

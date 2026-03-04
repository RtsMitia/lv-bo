package com.test.model;

import java.math.BigDecimal;
import java.util.List;

public class Trajet {
    public BigDecimal distance;
    public List<Integer> lieuxIds;

    public Trajet() {
    }

    public Trajet(BigDecimal distance, List<Integer> lieuxIds) {
        this.distance = distance;
        this.lieuxIds = lieuxIds;
    }

    public BigDecimal getDistance() {
        return this.distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public List<Integer> getLieuxIds() {
        return this.lieuxIds;
    }

    public void setLieuxIds(List<Integer> lieuxIds) {
        this.lieuxIds = lieuxIds;
    }
}

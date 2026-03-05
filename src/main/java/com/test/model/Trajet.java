package com.test.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Trajet {
    public BigDecimal distance;
    public List<Integer> lieuxIds;
    public List<BigDecimal> segmentDistances;

    public Trajet() {
    }

    public Trajet(BigDecimal distance, List<Integer> lieuxIds) {
        this.distance = distance;
        this.lieuxIds = lieuxIds;
        this.segmentDistances = new ArrayList<>();
    }

    public Trajet(BigDecimal distance, List<Integer> lieuxIds, List<BigDecimal> segmentDistances) {
        this.distance = distance;
        this.lieuxIds = lieuxIds;
        this.segmentDistances = segmentDistances;
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

    public List<BigDecimal> getSegmentDistances() {
        return this.segmentDistances;
    }

    public void setSegmentDistances(List<BigDecimal> segmentDistances) {
        this.segmentDistances = segmentDistances;
    }
}

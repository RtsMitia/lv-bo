package com.test.model;

public class Vehicule {
    
    private Integer id;
    private String reference;
    private Integer place;
    private String typeCarburant;

    public Vehicule() {
    }

    public Vehicule(Integer id, String reference, Integer place, String typeCarburant) {
        this.id = id;
        this.reference = reference;
        this.place = place;
        this.typeCarburant = typeCarburant;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getPlace() {
        return this.place;
    }

    public void setPlace(Integer place) {
        this.place = place;
    }

    public String getTypeCarburant() {
        return this.typeCarburant;
    }

    public void setTypeCarburant(String typeCarburant) {
        this.typeCarburant = typeCarburant;
    }
    
}

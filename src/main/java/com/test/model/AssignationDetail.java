package com.test.model;

public class AssignationDetail {
    private Integer id;
    private Integer idAssociation;
    private Integer idReservation;
    private Integer nbPersPrises;

    public AssignationDetail() {
    }

    public AssignationDetail(Integer id, Integer idAssociation, Integer idReservation, Integer nbPersPrises) {
        this.id = id;
        this.idAssociation = idAssociation;
        this.idReservation = idReservation;
        this.nbPersPrises = nbPersPrises;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdAssociation() {
        return idAssociation;
    }

    public void setIdAssociation(Integer idAssociation) {
        this.idAssociation = idAssociation;
    }

    public Integer getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(Integer idReservation) {
        this.idReservation = idReservation;
    }

    public Integer getNbPersPrises() {
        return nbPersPrises;
    }

    public void setNbPersPrises(Integer nbPersPrises) {
        this.nbPersPrises = nbPersPrises;
    }

    @Override
    public String toString() {
        return "AssignationDetail{" +
                "id=" + id +
                ", idAssociation=" + idAssociation +
                ", idReservation=" + idReservation +
                ", nbPersPrises=" + nbPersPrises +
                '}';
    }
}

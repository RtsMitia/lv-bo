package com.test.dto;

public class ReservationDTO {
    private Integer id;
    private String idClient;
    private Integer nbPassager;
    private String dateHeureArrivee;
    private Integer idHotel;
    private String nomHotel;

    public ReservationDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public Integer getNbPassager() {
        return nbPassager;
    }

    public void setNbPassager(Integer nbPassager) {
        this.nbPassager = nbPassager;
    }

    public String getDateHeureArrivee() {
        return dateHeureArrivee;
    }

    public void setDateHeureArrivee(String dateHeureArrivee) {
        this.dateHeureArrivee = dateHeureArrivee;
    }

    public Integer getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(Integer idHotel) {
        this.idHotel = idHotel;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    @Override
    public String toString() {
        return "ReservationDTO{" +
                "id=" + id +
                ", idClient='" + idClient + '\'' +
                ", nbPassager=" + nbPassager +
                ", dateHeureArrivee='" + dateHeureArrivee + '\'' +
                ", idHotel=" + idHotel +
                ", nomHotel='" + nomHotel + '\'' +
                '}';
    }
}

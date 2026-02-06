package com.test.model;

import java.time.LocalDateTime;

public class Reservation {
    private Integer id;
    private String idClient;
    private Integer nbPassager;
    private LocalDateTime dateHeureArrivee;
    private Integer idHotel;

    public Reservation() {
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

    public LocalDateTime getDateHeureArrivee() {
        return dateHeureArrivee;
    }

    public void setDateHeureArrivee(LocalDateTime dateHeureArrivee) {
        this.dateHeureArrivee = dateHeureArrivee;
    }

    public Integer getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(Integer idHotel) {
        this.idHotel = idHotel;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", idClient='" + idClient + '\'' +
                ", nbPassager=" + nbPassager +
                ", dateHeureArrivee=" + dateHeureArrivee +
                ", idHotel=" + idHotel +
                '}';
    }
}

package com.test.dto;

import com.test.model.Reservation;

public class ResaNADTO {
    private Reservation reservation;
    private int restePersonne;

    public ResaNADTO() {
    }

    public ResaNADTO(Reservation reservation, int restePersonne) {
        this.reservation = reservation;
        this.restePersonne = restePersonne;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getRestePersonne() {
        return restePersonne;
    }

    public void setRestePersonne(int restePersonne) {
        this.restePersonne = restePersonne;
    }
}

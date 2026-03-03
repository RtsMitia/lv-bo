package com.test.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignationWithDetails {
    private Integer assignationId;
    private Integer vehiculeId;
    private String vehiculeReference;
    private Integer vehiculePlace;
    private LocalDateTime departAeroport;
    private LocalDateTime retourAeroport;
    private Integer totalPassagers;
    private Integer restePlace;
    private List<ReservationWithHotel> reservations;

    public AssignationWithDetails() {
        this.reservations = new ArrayList<>();
    }

    public Integer getAssignationId() {
        return assignationId;
    }

    public void setAssignationId(Integer assignationId) {
        this.assignationId = assignationId;
    }

    public Integer getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Integer vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public String getVehiculeReference() {
        return vehiculeReference;
    }

    public void setVehiculeReference(String vehiculeReference) {
        this.vehiculeReference = vehiculeReference;
    }

    public Integer getVehiculePlace() {
        return vehiculePlace;
    }

    public void setVehiculePlace(Integer vehiculePlace) {
        this.vehiculePlace = vehiculePlace;
    }

    public LocalDateTime getDepartAeroport() {
        return departAeroport;
    }

    public void setDepartAeroport(LocalDateTime departAeroport) {
        this.departAeroport = departAeroport;
    }

    public LocalDateTime getRetourAeroport() {
        return retourAeroport;
    }

    public void setRetourAeroport(LocalDateTime retourAeroport) {
        this.retourAeroport = retourAeroport;
    }

    public Integer getTotalPassagers() {
        return totalPassagers;
    }

    public void setTotalPassagers(Integer totalPassagers) {
        this.totalPassagers = totalPassagers;
    }

    public Integer getRestePlace() {
        return restePlace;
    }

    public void setRestePlace(Integer restePlace) {
        this.restePlace = restePlace;
    }

    public List<ReservationWithHotel> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationWithHotel> reservations) {
        this.reservations = reservations;
    }

    public void addReservation(ReservationWithHotel reservation) {
        this.reservations.add(reservation);
    }

    // Inner class pour les réservations avec hotel
    public static class ReservationWithHotel {
        private Integer reservationId;
        private String idClient;
        private Integer nbPassager;
        private LocalDateTime dateHeureArrivee;
        private Integer idHotel;
        private String hotelNom;
        private Integer nbPersPrises;

        public ReservationWithHotel() {
        }

        public Integer getReservationId() {
            return reservationId;
        }

        public void setReservationId(Integer reservationId) {
            this.reservationId = reservationId;
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

        public String getHotelNom() {
            return hotelNom;
        }

        public void setHotelNom(String hotelNom) {
            this.hotelNom = hotelNom;
        }

        public Integer getNbPersPrises() {
            return nbPersPrises;
        }

        public void setNbPersPrises(Integer nbPersPrises) {
            this.nbPersPrises = nbPersPrises;
        }
    }
}

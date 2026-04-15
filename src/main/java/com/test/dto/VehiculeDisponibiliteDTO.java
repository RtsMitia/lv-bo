package com.test.dto;

import com.test.model.Vehicule;

import java.time.LocalDateTime;

public class VehiculeDisponibiliteDTO {
    private Integer idVehicule;
    private Vehicule vehicule;
    private LocalDateTime heureDisponibilite;

    public VehiculeDisponibiliteDTO() {
    }

    public VehiculeDisponibiliteDTO(Integer idVehicule, Vehicule vehicule, LocalDateTime heureDisponibilite) {
        this.idVehicule = idVehicule;
        this.vehicule = vehicule;
        this.heureDisponibilite = heureDisponibilite;
    }

    public Integer getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Integer idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public LocalDateTime getHeureDisponibilite() {
        return heureDisponibilite;
    }

    public void setHeureDisponibilite(LocalDateTime heureDisponibilite) {
        this.heureDisponibilite = heureDisponibilite;
    }
}

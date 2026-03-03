package com.test.model;

import java.time.LocalDateTime;

public class Assignation {
    private Integer id;
    private Integer vehicule;
    private LocalDateTime departAeroport;
    private LocalDateTime retourAeroport;

    public Assignation() {
    }

    public Assignation(Integer id, Integer vehicule, LocalDateTime departAeroport, LocalDateTime retourAeroport) {
        this.id = id;
        this.vehicule = vehicule;
        this.departAeroport = departAeroport;
        this.retourAeroport = retourAeroport;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVehicule() {
        return vehicule;
    }

    public void setVehicule(Integer vehicule) {
        this.vehicule = vehicule;
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

    @Override
    public String toString() {
        return "Assignation{" +
                "id=" + id +
                ", vehicule=" + vehicule +
                ", departAeroport=" + departAeroport +
                ", retourAeroport=" + retourAeroport +
                '}';
    }
}

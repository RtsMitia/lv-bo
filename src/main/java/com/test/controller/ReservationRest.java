package com.test.controller;

import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.JSON;
import com.test.dto.ReservationDTO;
import com.test.model.Reservation;
import com.test.repository.ReservationRepository;

import java.util.ArrayList;
import java.util.List;

@AnnotationController("/api/reservations")
public class ReservationRest {

    private final ReservationRepository repo = new ReservationRepository();

    @ManageUrl("/list")
    @JSON
    public List<ReservationDTO> list() {
        List<Reservation> reservations = repo.findAll();
        List<ReservationDTO> out = new ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Reservation r : reservations) {
            ReservationDTO dto = new ReservationDTO();
            dto.setId(r.getId());
            dto.setIdClient(r.getIdClient());
            dto.setNbPassager(r.getNbPassager());
            dto.setDateHeureArrivee(r.getDateHeureArrivee() == null ? null : r.getDateHeureArrivee().format(fmt));
            dto.setIdHotel(r.getIdHotel());
            out.add(dto);
        }
        return out;
    }
}

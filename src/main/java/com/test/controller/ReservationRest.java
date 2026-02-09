package com.test.controller;

import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.JSON;
import com.test.dto.ReservationDTO;
import com.test.model.Reservation;
import com.test.repository.ReservationRepository;
import com.test.repository.HotelRepository;
import com.test.model.Hotel;

import java.util.ArrayList;
import java.util.List;

@AnnotationController("/api/reservations")
public class ReservationRest {

    private final ReservationRepository repo = new ReservationRepository();
    private final HotelRepository hotelRepo = new HotelRepository();

    @ManageUrl("/list")
    @JSON
    public List<ReservationDTO> list(String date) {
        List<Reservation> reservations;
        if (date == null || date.trim().isEmpty()) {
            reservations = repo.findAll();
        } else {
            java.time.LocalDate ld = java.time.LocalDate.parse(date.trim());
            reservations = repo.findByDate(ld);
        }
        List<ReservationDTO> out = new ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Reservation r : reservations) {
            ReservationDTO dto = new ReservationDTO();
            dto.setId(r.getId());
            dto.setIdClient(r.getIdClient());
            dto.setNbPassager(r.getNbPassager());
            dto.setDateHeureArrivee(r.getDateHeureArrivee() == null ? null : r.getDateHeureArrivee().format(fmt));
            dto.setIdHotel(r.getIdHotel());
            if (r.getIdHotel() != null) {
                Hotel h = hotelRepo.findById(r.getIdHotel());
                dto.setNomHotel(h == null ? null : h.getNom());
            }
            out.add(dto);
        }
        return out;
    }
}

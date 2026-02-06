package com.test.controller;

import com.fw.ModelView;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.MyPOST;
import com.fw.annotations.JSON;
import com.test.model.Reservation;
import com.test.repository.ReservationRepository;
import com.test.repository.HotelRepository;

import java.util.List;

@AnnotationController("/reservation")
public class ReservationController {

    private final ReservationRepository repo = new ReservationRepository();
    private final HotelRepository hotelRepo = new HotelRepository();

    @ManageUrl("/list")
    @JSON
    public List<Reservation> list() {
        return repo.findAll();
    }

    @ManageUrl("/new")
    public ModelView newForm() {
        ModelView mv = new ModelView("/WEB-INF/views/reservation/reservation_form.jsp");
        mv.addItem("hotels", hotelRepo.findAll());
        return mv;
    }

    @ManageUrl("/save")
    @MyPOST
    public String save(String idClient, Integer nbPassager, String dateHeureArrivee, Integer idHotel) {
        Reservation reservation = new Reservation();
        reservation.setIdClient(idClient);
        reservation.setNbPassager(nbPassager);

        if (dateHeureArrivee != null && !dateHeureArrivee.isEmpty()) {
            reservation.setDateHeureArrivee(java.time.LocalDateTime.parse(dateHeureArrivee));
        }

        reservation.setIdHotel(idHotel);

        Reservation saved = repo.save(reservation);
        return "Saved reservation with id=" + saved.getId();
    }
}

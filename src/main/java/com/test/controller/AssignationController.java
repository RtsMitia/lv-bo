package com.test.controller;

import com.test.dto.AssignationWithDetails;
import com.test.model.Hotel;
import com.test.model.Reservation;
import com.test.model.Trajet;
import com.test.repository.AssignationRepository;
import com.test.repository.HotelRepository;
import com.test.repository.LieuRepository;
import com.test.repository.ReservationRepository;
import com.test.service.AssignationService;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.MyGET;
import com.fw.ModelView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AnnotationController("/assignation")
public class AssignationController {

    private final AssignationRepository assignationRepository = new AssignationRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final HotelRepository hotelRepository = new HotelRepository();
    private final AssignationService assignationService = new AssignationService();
    private final LieuRepository lieuRepository = new LieuRepository();

    @ManageUrl("")
    @MyGET
    public ModelView index() {
        return showDateForm();
    }

    @ManageUrl("/date")
    @MyGET
    public ModelView showDateForm() {
        ModelView mv = new ModelView("layout.jsp");
        mv.addItem("content", "assignation/assignation_date_form.jsp");
        return mv;
    }

    @ManageUrl("/list")
    @MyGET
    public ModelView showAssignationList(String date) {
        if (date == null || date.isEmpty()) {
            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("content", "assignation/assignation_date_form.jsp");
            mv.addItem("error", "Veuillez sélectionner une date");
            return mv;
        }

        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

            // Automatically assign unassigned reservations before displaying
            int assignedCount = assignationService.assignReservationsForDate(localDate);

            List<AssignationWithDetails> assignations = assignationRepository.findWithDetailsByDate(localDate);

            List<Reservation> unassignedReservations = reservationRepository.findUnassignedByDate(localDate);

            Map<Integer, String> hotelMap = new HashMap<>();
            List<Hotel> hotels = hotelRepository.findAll();
            for (Hotel h : hotels) {
                hotelMap.put(h.getId(), h.getNom());
            }

            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("date", localDate);
            mv.addItem("dateFormatted", localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            mv.addItem("assignations", assignations);
            mv.addItem("unassignedReservations", unassignedReservations);
            mv.addItem("hotelMap", hotelMap);
            mv.addItem("assignedCount", assignedCount);
            mv.addItem("content", "assignation/assignation_list.jsp");
            return mv;

        } catch (Exception e) {
            e.printStackTrace();
            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("error", "Erreur lors de la récupération des données: " + e.getMessage());
            mv.addItem("content", "assignation/assignation_date_form.jsp");
            return mv;
        }
    }

    @ManageUrl("/detail/{id}")
    @MyGET
    public ModelView showDetail(int id, String date) {
        try {
            Trajet trajet = assignationService.findTrajet(id);
            BigDecimal totalDistance = trajet.getDistance();
            List<String> lieux = lieuRepository.getLibelle(trajet.getLieuxIds());
            AssignationWithDetails assignationDetails = assignationRepository.getDetailAssignation(id);

            java.time.LocalDate localDate = null;
            if (date != null && !date.isEmpty()) {
                try {
                    localDate = java.time.LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
                } catch (Exception ex) {
                    localDate = null;
                }
            }

            if (localDate == null && assignationDetails != null && assignationDetails.getReservations() != null
                    && !assignationDetails.getReservations().isEmpty()) {
                java.time.LocalDateTime dt = assignationDetails.getReservations().get(0).getDateHeureArrivee();
                if (dt != null) {
                    localDate = dt.toLocalDate();
                }
            }

            ModelView mv = new ModelView("layout.jsp");
            mv.addItem("trajet", trajet);
            mv.addItem("totalDistance", totalDistance);
            mv.addItem("lieux", lieux);
            mv.addItem("assignationDetails", assignationDetails);

            if (localDate != null) {
                mv.addItem("date", localDate);
                mv.addItem("dateFormatted", localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                mv.addItem("dateParam", localDate.format(DateTimeFormatter.ISO_DATE));
            }

            mv.addItem("content", "assignation/trajet_detail.jsp");
            return mv;
        } catch (Exception e) {
            e.printStackTrace();
            return showAssignationList(date);
        }
    }
}

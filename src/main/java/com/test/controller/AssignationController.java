package com.test.controller;

import com.test.dto.AssignationWithDetails;
import com.test.model.Hotel;
import com.test.model.Reservation;
import com.test.repository.AssignationRepository;
import com.test.repository.HotelRepository;
import com.test.repository.ReservationRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AssignationController", urlPatterns = { "/assignation/*" })
public class AssignationController extends HttpServlet {

    private final AssignationRepository assignationRepository = new AssignationRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final HotelRepository hotelRepository = new HotelRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if (path == null || path.equals("/") || path.equals("/date")) {
            showDateForm(req, resp);
        } else if (path.equals("/list")) {
            showAssignationList(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }


    private void showDateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("content", "/WEB-INF/views/assignation/assignation_date_form.jsp");
        req.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(req, resp);
    }


    private void showAssignationList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dateParam = req.getParameter("date");

        if (dateParam == null || dateParam.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/assignation/date");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateParam, DateTimeFormatter.ISO_DATE);

            List<AssignationWithDetails> assignations = assignationRepository.findWithDetailsByDate(date);

            List<Reservation> unassignedReservations = reservationRepository.findUnassignedByDate(date);

            Map<Integer, String> hotelMap = new HashMap<>();
            List<Hotel> hotels = hotelRepository.findAll();
            for (Hotel h : hotels) {
                hotelMap.put(h.getId(), h.getNom());
            }

            req.setAttribute("date", date);
            req.setAttribute("dateFormatted", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            req.setAttribute("assignations", assignations);
            req.setAttribute("unassignedReservations", unassignedReservations);
            req.setAttribute("hotelMap", hotelMap);
            req.setAttribute("content", "/WEB-INF/views/assignation/assignation_list.jsp");
            req.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Erreur lors de la récupération des données: " + e.getMessage());
            req.setAttribute("content", "/WEB-INF/views/assignation/assignation_date_form.jsp");
            req.getRequestDispatcher("/WEB-INF/views/layout.jsp").forward(req, resp);
        }
    }
}

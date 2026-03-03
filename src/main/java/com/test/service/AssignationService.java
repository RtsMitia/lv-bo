package com.test.service;

import com.test.model.*;
import com.test.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for automatic assignment of vehicles to reservations
 */
public class AssignationService {

    private final ReservationRepository reservationRepo;
    private final VehiculeRepository vehiculeRepo;
    private final HotelRepository hotelRepo;
    private final LieuRepository lieuRepo;
    private final DistanceRepository distanceRepo;
    private final ParamRepository paramRepo;
    private final AssignationRepository assignationRepo;
    private final AssignationDetailRepository assignationDetailRepo;

    public AssignationService() {
        this.reservationRepo = new ReservationRepository();
        this.vehiculeRepo = new VehiculeRepository();
        this.hotelRepo = new HotelRepository();
        this.lieuRepo = new LieuRepository();
        this.distanceRepo = new DistanceRepository();
        this.paramRepo = new ParamRepository();
        this.assignationRepo = new AssignationRepository();
        this.assignationDetailRepo = new AssignationDetailRepository();
    }

    /**
     * Automatically assign vehicles to all unassigned reservations for a specific date
     */
    public int assignReservationsForDate(LocalDate date) throws Exception {
        // Get average velocity from param
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null) {
            throw new RuntimeException("Parameter 'vm' not found in database");
        }
        double vm = Double.parseDouble(vmString);

        // Get all reservations for this date that don't have an assignation yet
        List<Reservation> unassignedReservations = getUnassignedReservationsForDate(date);

        // Sort by date_heure_arrivee (earliest first)
        unassignedReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        int assignedCount = 0;

        // Process each reservation
        for (Reservation reservation : unassignedReservations) {
            try {
                boolean assigned = assignReservation(reservation, vm, date);
                if (assigned) {
                    assignedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error assigning reservation " + reservation.getId() + ": " + e.getMessage());
                // Continue with next reservation
            }
        }

        return assignedCount;
    }

    /**
     * Get reservations for a date that don't have an assignation detail yet
     */
    private List<Reservation> getUnassignedReservationsForDate(LocalDate date) {
        List<Reservation> allReservations = reservationRepo.findByDate(date);
        
        // Filter out reservations that already have an assignation_detail
        List<Reservation> unassigned = new ArrayList<>();
        for (Reservation r : allReservations) {
            if (!hasAssignation(r.getId())) {
                unassigned.add(r);
            }
        }
        
        return unassigned;
    }

    /**
     * Check if a reservation has an assignation detail
     */
    private boolean hasAssignation(Integer reservationId) {
        // Query to check if reservation is in assignation_detail
        try {
            List<AssignationDetail> allDetails = assignationDetailRepo.findAll();
            for (AssignationDetail detail : allDetails) {
                if (detail.getIdReservation().equals(reservationId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Assign a single reservation to an available vehicle
     */
    private boolean assignReservation(Reservation reservation, double vm, LocalDate date) throws Exception {
        // Get hotel and its lieu
        Hotel hotel = hotelRepo.findById(reservation.getIdHotel());
        if (hotel == null || hotel.getIdLieu() == null) {
            System.err.println("Hotel not found or has no lieu for reservation " + reservation.getId());
            return false;
        }

        // Get lieu code for distance lookup
        Lieu hotelLieu = lieuRepo.getById(hotel.getIdLieu());
        if (hotelLieu == null) {
            System.err.println("Lieu not found for hotel " + hotel.getId());
            return false;
        }

        // Get distance from airport to hotel
        BigDecimal distanceKm = distanceRepo.findDistanceFromAirportToLieu(hotelLieu.getCode());
        if (distanceKm == null) {
            System.err.println("Distance not found from airport to lieu " + hotelLieu.getCode());
            return false;
        }

        // Calculate retour_aeroport = depart_aeroport + (distance * 2 / vm) hours
        LocalDateTime departAeroport = reservation.getDateHeureArrivee();
        double roundTripHours = (distanceKm.doubleValue() * 2) / vm;
        LocalDateTime retourAeroport = departAeroport.plusMinutes((long) (roundTripHours * 60));

        // Find available vehicles that can fit the passengers
        List<Vehicule> availableVehicles = findAvailableVehicles(
            reservation.getNbPassager(),
            departAeroport,
            retourAeroport,
            date
        );

        if (availableVehicles.isEmpty()) {
            System.err.println("No available vehicle for reservation " + reservation.getId());
            return false;
        }

        // Select best-fit vehicle (smallest capacity that fits, prefer Diesel)
        Vehicule bestVehicle = selectBestVehicle(availableVehicles, reservation.getNbPassager());

        // Create assignation
        Integer assignationId = assignationRepo.createAssignation(
            bestVehicle.getId(),
            departAeroport,
            retourAeroport
        );

        if (assignationId == null) {
            System.err.println("Failed to create assignation for reservation " + reservation.getId());
            return false;
        }

        // Create assignation detail
        assignationDetailRepo.createDetail(
            assignationId,
            reservation.getId(),
            reservation.getNbPassager()
        );

        return true;
    }

    /**
     * Find vehicles that are available and can fit the required passengers
     */
    private List<Vehicule> findAvailableVehicles(
        Integer requiredCapacity,
        LocalDateTime departAeroport,
        LocalDateTime retourAeroport,
        LocalDate date
    ) {
        // Get all vehicles
        List<Vehicule> allVehicles = vehiculeRepo.findAll();

        // Get all assignations for this date to check availability
        List<Assignation> assignations = assignationRepo.findAll();

        // Filter vehicles that can fit passengers and are available
        List<Vehicule> availableVehicles = new ArrayList<>();
        
        for (Vehicule vehicule : allVehicles) {
            // Check capacity
            if (vehicule.getPlace() < requiredCapacity) {
                continue;
            }

            // Check if vehicle is available (no time overlap with existing assignations)
            boolean isAvailable = isVehiculeAvailable(vehicule.getId(), departAeroport, retourAeroport, assignations);
            
            if (isAvailable) {
                availableVehicles.add(vehicule);
            }
        }

        return availableVehicles;
    }

    /**
     * Check if a vehicle is available during a time window
     */
    private boolean isVehiculeAvailable(
        Integer vehiculeId,
        LocalDateTime requestedDepart,
        LocalDateTime requestedRetour,
        List<Assignation> assignations
    ) {
        for (Assignation assignation : assignations) {
            if (!assignation.getVehicule().equals(vehiculeId)) {
                continue;
            }

            LocalDateTime existingDepart = assignation.getDepartAeroport();
            LocalDateTime existingRetour = assignation.getRetourAeroport();

            // Check for time overlap
            // Overlap occurs if:
            // - requestedDepart is between existingDepart and existingRetour
            // - requestedRetour is between existingDepart and existingRetour
            // - requested window completely contains existing window
            boolean overlap = 
                (requestedDepart.isBefore(existingRetour) && requestedRetour.isAfter(existingDepart));

            if (overlap) {
                return false; // Vehicle is busy
            }
        }

        return true; // Vehicle is available
    }

    /**
     * Select the best vehicle: smallest capacity that fits, prefer Diesel for ties
     */
    private Vehicule selectBestVehicle(List<Vehicule> vehicles, Integer requiredCapacity) {
        // Sort by place (ascending), then by type_carburant (D comes before others)
        vehicles.sort((v1, v2) -> {
            int placeCompare = Integer.compare(v1.getPlace(), v2.getPlace());
            if (placeCompare != 0) {
                return placeCompare;
            }
            
            // If same capacity, prefer Diesel
            String type1 = v1.getTypeCarburant();
            String type2 = v2.getTypeCarburant();
            
            if ("D".equalsIgnoreCase(type1) && !"D".equalsIgnoreCase(type2)) {
                return -1; // v1 comes first
            } else if (!"D".equalsIgnoreCase(type1) && "D".equalsIgnoreCase(type2)) {
                return 1; // v2 comes first
            }
            
            return 0; // Same priority
        });

        return vehicles.get(0); // Return the best one
    }
}

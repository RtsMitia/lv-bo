package com.test.service;

import com.test.dto.AssignationWithDetails;
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

    public int assignReservationsForDate(LocalDate date) throws Exception {
        // Get average velocity from param
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null) {
            throw new RuntimeException("Parameter 'vm' not found in database");
        }
        double vm = Double.parseDouble(vmString);

        List<Reservation> unassignedReservations = getUnassignedReservationsForDate(date);
        unassignedReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        Map<LocalDateTime, List<Reservation>> groups = new TreeMap<>();
        for(Reservation r : unassignedReservations) {
            LocalDateTime key = r.getDateHeureArrivee();
            List<Reservation> list = groups.get(key);
            if(list == null) {
                list = new ArrayList<>();
                groups.put(key, list);
            }
            list.add(r);
        }

        for (List<Reservation> reservations : groups.values()) {
            reservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed()); // pr que le nb de passager soit decroissant
        }

        List<Assignation> assignations = new ArrayList<>();
        for(LocalDateTime dateEntry : groups.keySet()) {
            List<Reservation> reservations = groups.get(dateEntry);
            for (Reservation r : reservations) {
                Assignation a = assignReservation(r, date);
                if (assignations.stream().noneMatch(x -> x.getId().equals(a.getId()))) {
                    assignations.add(a);
                }
            }

        }

        return assignations.size();
    }

    private List<Reservation> getUnassignedReservationsForDate(LocalDate date) {
        List<Reservation> allReservations = reservationRepo.findByDate(date);
        
        List<Reservation> unassigned = new ArrayList<>();
        for (Reservation r : allReservations) {
            if (!hasAssignation(r.getId())) {
                unassigned.add(r);
            }
        }
        
        return unassigned;
    }

    private boolean hasAssignation(Integer reservationId) {
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

    private Vehicule trouverNouveauVehicule(Reservation r, LocalDate date) {
        List<Vehicule> all = vehiculeRepo.findAll();
        List<Vehicule> pasDansMap = new ArrayList<>();
        Set<Integer> usedVehiculeIds = new HashSet<>();

        for (AssignationWithDetails awd : assignationRepo.findWithDetailsByDate(date)) {
            if (awd.getVehiculeId() != null) {
                usedVehiculeIds.add(awd.getVehiculeId());
            }
        }

        for (Vehicule v : all) {
            if (!usedVehiculeIds.contains(v.getId()) && v.getPlace() >= r.getNbPassager()) {
                pasDansMap.add(v);
            }
        }

        if (pasDansMap.isEmpty()) {
            return null;
        }

        return selectBestVehicle(pasDansMap, r.getNbPassager());
    }

    private Assignation assignationExistanteDisponible(Reservation r, LocalDate date) {
        List<AssignationWithDetails> vehiculesDispos = assignationRepo.findWithDetailsByDate(date);
        Map<Integer, List<AssignationWithDetails>> assignationMap = new HashMap<>();
        for (AssignationWithDetails a : vehiculesDispos) {
            if (!assignationMap.containsKey(a.getAssignationId())) {
                assignationMap.put(a.getAssignationId(), new ArrayList<>());
            }
            assignationMap.get(a.getAssignationId()).add(a);
        }

        int plusPetit = Integer.MAX_VALUE;
        int idAssignationBest = 0;
        boolean trouve = false;

        for (int idEntry : assignationMap.keySet()) {
            int restePlace = assignationMap.get(idEntry).get(0).getRestePlace();
            if (restePlace >= r.getNbPassager() && restePlace < plusPetit) {
                plusPetit = restePlace;
                idAssignationBest = idEntry;
                trouve = true;
            }
        }

        if (trouve) {
            return assignationRepo.findById(idAssignationBest);
        }

        return null;
    }

    public Assignation assignReservation(Reservation reservation, LocalDate date) throws Exception {
        Assignation existante = assignationExistanteDisponible(reservation, date);
        if (existante != null) {
            assignationDetailRepo.createDetail(existante.getId(), reservation.getId(), reservation.getNbPassager());
            return existante;
        }

        Vehicule vehicule = trouverNouveauVehicule(reservation, date);
        if (vehicule == null) {
            System.err.println("Aucun véhicule disponible pour la réservation " + reservation.getId());
            return null;
        }
        Integer newId = assignationRepo.createAssignation(vehicule.getId(), reservation.getDateHeureArrivee(), null);
        assignationDetailRepo.createDetail(newId, reservation.getId(), reservation.getNbPassager());

        return assignationRepo.findById(newId);
    }

    private Vehicule selectBestVehicle(List<Vehicule> vehicles, Integer requiredCapacity) {
        vehicles.sort((v1, v2) -> {
            int placeCompare = Integer.compare(v1.getPlace(), v2.getPlace());
            if (placeCompare != 0) {
                return placeCompare;
            }
            
            String type1 = v1.getTypeCarburant();
            String type2 = v2.getTypeCarburant();

            if ("D".equalsIgnoreCase(type1) && !"D".equalsIgnoreCase(type2)) {
                return -1; 
            } else if (!"D".equalsIgnoreCase(type1) && "D".equalsIgnoreCase(type2)) {
                return 1; 
            }
            
            return 0; 
        });

        return vehicles.get(0); 
    }

    public boolean calculateAndUpdateRetourAeroport(List<Assignation> assignations) {
        boolean allSuccess = true;

        try {
            for (Assignation a : assignations) {
                boolean success = calculateAndUpdateRetourAeroport(a);
                if (!success) {
                    allSuccess = false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing assignations: " + e.getMessage());
            throw e;
        }

        return allSuccess;
    }

    public boolean calculateAndUpdateRetourAeroport(Assignation assignation) {
        try {
            LocalDateTime newRetour = calculateRetourAeroport(assignation);
            return assignationRepo.updateHeureRetourAeroport(assignation.getId(), newRetour);
        } catch (Exception e) {
            System.err.println("Error calculating/updating retour_aeroport for assignation " + assignation.getId()
                    + ": " + e.getMessage());
            return false;
        }
    }

    public LocalDateTime calculateRetourAeroport(Assignation a) throws Exception {
        Trajet trajet = findTrajet(a.getId());
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null) {
            throw new RuntimeException("Parameter 'vm' not found in database");
        }
        double vm = Double.parseDouble(vmString);
        double roundTripHours = (trajet.getDistance().doubleValue() * 2) / vm;
        return a.getDepartAeroport().plusMinutes((long) (roundTripHours * 60));
    }

    
    public List<Integer> findLieuxIds(Integer assignationId) throws Exception {
        return assignationRepo.findLieuxIds(assignationId);
    }

    public Map.Entry<Integer, BigDecimal> findNearestLieu(Integer lieuDepart, List<Integer> lieuxIds) throws Exception {
        return distanceRepo.findNearest(lieuDepart, lieuxIds);
    }

    public Trajet findTrajet(Integer assignationId) throws Exception {
        try {
            BigDecimal totalDistance = BigDecimal.ZERO;
            List<Integer> lieuxIds = findLieuxIds(assignationId);
            List<Integer> sortedLieux = new ArrayList<>();
            List<Integer> tempList = new ArrayList<>(lieuxIds);
            Integer currentPoint = distanceRepo.getLieuIdByCode("AIR");

            while (!tempList.isEmpty()) {
                Map.Entry<Integer, BigDecimal> nearest = findNearestLieu(currentPoint, tempList);
                if (nearest == null) {
                    break;
                }
                sortedLieux.add(nearest.getKey());
                tempList.remove(nearest.getKey());
                currentPoint = nearest.getKey();
                totalDistance = totalDistance.add(nearest.getValue());
            }

            return new Trajet(totalDistance, sortedLieux);

        } catch (Exception e) {
            System.err.println("Error finding trajet for assignation " + assignationId + ": " + e.getMessage());
            throw e;
        }
    }
}

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

    /**
     * Simulate assignment for a given date without persisting anything to the database.
     * Returns a list of AssignationWithDetails representing the projected assignations.
     */
    public List<AssignationWithDetails> simulateAssignationsForDate(LocalDate date) throws Exception {
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null) throw new RuntimeException("Parameter 'vm' not found in database");
        double vm = Double.parseDouble(vmString);

        List<Reservation> unassigned = getUnassignedReservationsForDate(date);
        unassigned.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        Map<LocalDateTime, List<Reservation>> groups = new TreeMap<>();
        for (Reservation r : unassigned) {
            groups.computeIfAbsent(r.getDateHeureArrivee(), k -> new ArrayList<>()).add(r);
        }
        for (List<Reservation> grp : groups.values()) {
            grp.sort(Comparator.comparing(Reservation::getNbPassager).reversed());
        }

        // Prepare hotel lookup (id -> Hotel) for display and trajet calculation
        List<Hotel> hotels = hotelRepo.findAll();
        Map<Integer, Hotel> hotelById = new HashMap<>();
        for (Hotel h : hotels) hotelById.put(h.getId(), h);

        List<Vehicule> allVehicules = vehiculeRepo.findAll();

        // In-memory virtual assignations being built
        List<AssignationWithDetails> virtualAssignations = new ArrayList<>();
        // Track which vehicule IDs are virtually busy per time slot
        Map<LocalDateTime, Set<Integer>> virtualBusyBySlot = new HashMap<>();

        for (LocalDateTime timeSlot : groups.keySet()) {
            for (Reservation r : groups.get(timeSlot)) {
                // Find best existing virtual assignation with enough free space
                AssignationWithDetails bestVirtual = null;
                int minReste = Integer.MAX_VALUE;
                for (AssignationWithDetails va : virtualAssignations) {
                    if (va.getDepartAeroport().equals(timeSlot) && va.getRestePlace() >= r.getNbPassager()) {
                        if (va.getRestePlace() < minReste) {
                            minReste = va.getRestePlace();
                            bestVirtual = va;
                        }
                    }
                }

                if (bestVirtual != null) {
                    AssignationWithDetails.ReservationWithHotel rwh = buildReservationWithHotel(r, hotelById);
                    bestVirtual.addReservation(rwh);
                    bestVirtual.setTotalPassagers(bestVirtual.getTotalPassagers() + r.getNbPassager());
                    bestVirtual.setRestePlace(bestVirtual.getRestePlace() - r.getNbPassager());
                } else {
                    // Combine real DB busy vehicles with virtual busy vehicles for this slot
                    Set<Integer> realBusy = assignationRepo.findBusyVehiculeIds(timeSlot);
                    Set<Integer> virtualBusy = virtualBusyBySlot.getOrDefault(timeSlot, new HashSet<>());
                    Set<Integer> allBusy = new HashSet<>(realBusy);
                    allBusy.addAll(virtualBusy);

                    List<Vehicule> candidates = new ArrayList<>();
                    for (Vehicule v : allVehicules) {
                        if (!allBusy.contains(v.getId()) && v.getPlace() >= r.getNbPassager()) {
                            candidates.add(v);
                        }
                    }
                    if (candidates.isEmpty()) continue;

                    Vehicule chosen = selectBestVehicle(candidates, r.getNbPassager());

                    AssignationWithDetails va = new AssignationWithDetails();
                    va.setVehiculeId(chosen.getId());
                    va.setVehiculeReference(chosen.getReference());
                    va.setVehiculePlace(chosen.getPlace());
                    va.setDepartAeroport(timeSlot);
                    va.setTotalPassagers(r.getNbPassager());
                    va.setRestePlace(chosen.getPlace() - r.getNbPassager());

                    AssignationWithDetails.ReservationWithHotel rwh = buildReservationWithHotel(r, hotelById);
                    va.addReservation(rwh);

                    virtualAssignations.add(va);
                    virtualBusyBySlot.computeIfAbsent(timeSlot, k -> new HashSet<>()).add(chosen.getId());
                }
            }
        }

        // Calculate retour aeroport for each virtual assignation using in-memory trajet
        for (AssignationWithDetails va : virtualAssignations) {
            try {
                List<Integer> lieuxIds = new ArrayList<>();
                for (AssignationWithDetails.ReservationWithHotel rwh : va.getReservations()) {
                    if (rwh.getIdHotel() != null) {
                        Hotel h = hotelById.get(rwh.getIdHotel());
                        if (h != null && h.getIdLieu() != null) {
                            lieuxIds.add(h.getIdLieu());
                        }
                    }
                }
                if (!lieuxIds.isEmpty()) {
                    BigDecimal totalDist = BigDecimal.ZERO;
                    List<Integer> temp = new ArrayList<>(lieuxIds);
                    Integer current = distanceRepo.getLieuIdByCode("AIR");
                    while (!temp.isEmpty()) {
                        Map.Entry<Integer, BigDecimal> nearest = distanceRepo.findNearest(current, temp);
                        if (nearest == null) break;
                        totalDist = totalDist.add(nearest.getValue());
                        temp.remove(nearest.getKey());
                        current = nearest.getKey();
                    }
                    double roundTripHours = (totalDist.doubleValue() * 2) / vm;
                    va.setRetourAeroport(va.getDepartAeroport().plusMinutes((long)(roundTripHours * 60)));
                }
            } catch (Exception e) {
                // retour stays null for this virtual assignation
            }
        }

        return virtualAssignations;
    }

    private AssignationWithDetails.ReservationWithHotel buildReservationWithHotel(Reservation r, Map<Integer, Hotel> hotelById) {
        AssignationWithDetails.ReservationWithHotel rwh = new AssignationWithDetails.ReservationWithHotel();
        rwh.setReservationId(r.getId());
        rwh.setIdClient(r.getIdClient());
        rwh.setNbPassager(r.getNbPassager());
        rwh.setNbPersPrises(r.getNbPassager());
        rwh.setDateHeureArrivee(r.getDateHeureArrivee());
        rwh.setIdHotel(r.getIdHotel());
        if (r.getIdHotel() != null) {
            Hotel h = hotelById.get(r.getIdHotel());
            if (h != null) rwh.setHotelNom(h.getNom());
        }
        return rwh;
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
        for (Reservation r : unassignedReservations) {
            LocalDateTime key = r.getDateHeureArrivee();
            List<Reservation> list = groups.get(key);
            if (list == null) {
                list = new ArrayList<>();
                groups.put(key, list);
            }
            list.add(r);
        }

        for (List<Reservation> reservations : groups.values()) {
            reservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed()); // pr que le nb de passager
                                                                                            // soit decroissant
        }

        List<Assignation> assignations = new ArrayList<>();
        for (LocalDateTime dateEntry : groups.keySet()) {
            List<Reservation> reservations = groups.get(dateEntry);
            for (Reservation r : reservations) {
                Assignation a = assignReservation(r, date);
                if (a != null && assignations.stream().noneMatch(x -> x != null && x.getId().equals(a.getId()))) {
                    assignations.add(a);
                }
            }

            boolean updateRetour = calculateAndUpdateRetourAeroport(assignations);
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
        Set<Integer> busyVehiculeIds = assignationRepo.findBusyVehiculeIds(r.getDateHeureArrivee());

        for (Vehicule v : all) {
            if (!busyVehiculeIds.contains(v.getId()) && v.getPlace() >= r.getNbPassager()) {
                pasDansMap.add(v);
            }
        }

        if (pasDansMap.isEmpty()) {
            return null;
        }

        return selectBestVehicle(pasDansMap, r.getNbPassager());
    }

    private Assignation assignationExistanteDisponible(Reservation r, LocalDate date) throws Exception {
        try {
            List<AssignationWithDetails> vehiculesDispos = assignationRepo.findWithDetailsByDateAndDepartAeroport(date, r.getDateHeureArrivee());

            int plusPetit = Integer.MAX_VALUE;
            int idAssignationBest = 0;
            boolean trouve = false;

            for (AssignationWithDetails awd : vehiculesDispos) {
                int restePlace = awd.getRestePlace();
                if (restePlace >= r.getNbPassager() && restePlace < plusPetit) {
                    plusPetit = restePlace;
                    idAssignationBest = awd.getAssignationId();
                    trouve = true;
                }
            }

            if (trouve) {
                return assignationRepo.findById(idAssignationBest);
            }

            return null;
        } catch(Exception e) {
            throw new Exception(e.getMessage());
        }
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
            List<BigDecimal> segmentDistances = new ArrayList<>();
            List<Integer> tempList = new ArrayList<>(lieuxIds);
            Integer currentPoint = distanceRepo.getLieuIdByCode("AIR");

            while (!tempList.isEmpty()) {
                Map.Entry<Integer, BigDecimal> nearest = findNearestLieu(currentPoint, tempList);
                if (nearest == null) {
                    break;
                }
                sortedLieux.add(nearest.getKey());
                segmentDistances.add(nearest.getValue());
                tempList.remove(nearest.getKey());
                currentPoint = nearest.getKey();
                totalDistance = totalDistance.add(nearest.getValue());
            }

            return new Trajet(totalDistance, sortedLieux, segmentDistances);

        } catch (Exception e) {
            System.err.println("Error finding trajet for assignation " + assignationId + ": " + e.getMessage());
            throw e;
        }
    }
}

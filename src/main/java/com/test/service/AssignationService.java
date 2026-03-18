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

    public List<Reservation> getReservationsBetweenDates(List<Reservation> reservations, LocalDateTime start, LocalDateTime  end) {
        List<Reservation> filteredList = reservations.stream()
                                        .filter(r -> !r.getDateHeureArrivee().isBefore(start) && 
                                                    !r.getDateHeureArrivee().isAfter(end))
                                        .collect(Collectors.toList());

        return filteredList;
    }
    public Map<LocalDateTime, List<Reservation>> groupReservations(List<Reservation> reservations) throws Exception {
        String taString = paramRepo.getValueByKey("ta");
        if (taString == null) throw new RuntimeException("Parameter 'ta' not found");
        int ta = Integer.parseInt(taString);

        Map<LocalDateTime, List<Reservation>> groups = new TreeMap<>();
        
        reservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        int i = 0;
        while (i < reservations.size()) {
            Reservation current = reservations.get(i);
            LocalDateTime windowStart = current.getDateHeureArrivee();
            LocalDateTime windowEnd = windowStart.plusMinutes(ta);

            List<Reservation> currentGroup = new ArrayList<>();
            int j = i;
            while (j < reservations.size() && !reservations.get(j).getDateHeureArrivee().isAfter(windowEnd)) {
                currentGroup.add(reservations.get(j));
                j++;
            }
            
            LocalDateTime maxDate = currentGroup.get(currentGroup.size() - 1).getDateHeureArrivee();
            
            groups.put(maxDate, currentGroup);

            i = j; 
        }
        return groups;
    }


    public int assignReservationsForDate(LocalDate date) throws Exception {
        int ta = getTa();

        // Single bulk delete by date for existing assignations and details.
        int deleted = assignationRepo.deleteByDepartDate(date);

        List<Reservation> pendingReservations = getUnassignedReservationsForDate(date);
        pendingReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        Set<Integer> createdAssignationIds = new HashSet<>();
        List<Reservation> deferredToNextGroup = new ArrayList<>();

        while (!pendingReservations.isEmpty() || !deferredToNextGroup.isEmpty()) {
            LocalDateTime groupStart;
            if (!pendingReservations.isEmpty()) {
                groupStart = pendingReservations.get(0).getDateHeureArrivee();
            } else {
                groupStart = deferredToNextGroup.stream()
                        .map(Reservation::getDateHeureArrivee)
                        .min(LocalDateTime::compareTo)
                        .orElseThrow(() -> new RuntimeException("No group start found for deferred reservations"));
            }
            LocalDateTime windowEnd = groupStart.plusMinutes(ta);

            List<Reservation> windowReservations = extractWindowReservations(pendingReservations, windowEnd);
            if (!deferredToNextGroup.isEmpty()) {
                windowReservations.addAll(deferredToNextGroup);
                deferredToNextGroup.clear();
                windowReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));
            }

            GroupAssignmentResult result = assignWindowWithFallback(windowReservations, windowEnd);

            createdAssignationIds.addAll(result.getCreatedAssignationIds());

            if (result.hasAssigned() && !result.getCreatedAssignationIds().isEmpty()) {
                // Critical for chained groups in the same day: make retour_aeroport visible
                // immediately so vehicles can become available later in the planning run.
                updateRetoursForAssignationIds(result.getCreatedAssignationIds());
            }

            if (result.hasAssigned() && !result.getDeferredReservations().isEmpty()) {
                deferredToNextGroup.addAll(result.getDeferredReservations());
                deferredToNextGroup.sort(Comparator.comparing(Reservation::getDateHeureArrivee));
            } else if (!result.hasAssigned() && !result.getDeferredReservations().isEmpty()) {
                // No progress for this window: keep later timestamps for next windows,
                // drop only the earliest timestamp batch to avoid infinite loops.
                LocalDateTime earliestDeferred = result.getDeferredReservations().stream()
                        .map(Reservation::getDateHeureArrivee)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);

                if (earliestDeferred != null) {
                    List<Reservation> carryForward = result.getDeferredReservations().stream()
                            .filter(r -> r.getDateHeureArrivee().isAfter(earliestDeferred))
                            .collect(Collectors.toList());

                    if (!carryForward.isEmpty()) {
                        pendingReservations.addAll(carryForward);
                        pendingReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));
                    }
                }
            }
        }

        return createdAssignationIds.size();
    }

    private void updateRetoursForAssignationIds(Set<Integer> assignationIds) {
        List<Assignation> assignations = new ArrayList<>();
        for (Integer id : assignationIds) {
            Assignation a = assignationRepo.findById(id);
            if (a != null) {
                assignations.add(a);
            }
        }
        calculateAndUpdateRetourAeroport(assignations);
    }

    private int getTa() throws Exception {
        String taString = paramRepo.getValueByKey("ta");
        if (taString == null) {
            throw new RuntimeException("Parameter 'ta' not found");
        }
        return Integer.parseInt(taString);
    }

    private List<Reservation> extractWindowReservations(List<Reservation> pendingReservations, LocalDateTime windowEnd) {
        List<Reservation> windowReservations = new ArrayList<>();

        int idx = 0;
        while (idx < pendingReservations.size()
                && !pendingReservations.get(idx).getDateHeureArrivee().isAfter(windowEnd)) {
            windowReservations.add(pendingReservations.get(idx));
            idx++;
        }

        pendingReservations.subList(0, idx).clear();
        return windowReservations;
    }

    private GroupAssignmentResult assignWindowWithFallback(List<Reservation> windowReservations, LocalDateTime windowEnd)
            throws Exception {
        List<Reservation> working = new ArrayList<>(windowReservations);
        List<Reservation> deferred = new ArrayList<>();
        Set<Integer> createdAssignationIds = new HashSet<>();

        while (!working.isEmpty()) {
            LocalDateTime currentDeparture = working.stream()
                    .map(Reservation::getDateHeureArrivee)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            if (currentDeparture == null) {
                break;
            }

            LocalDateTime feasibleDeparture = findEarliestFeasibleDeparture(working, currentDeparture, windowEnd);
            if (feasibleDeparture != null) {
                createdAssignationIds.addAll(assignReservationsAtDeparture(working, feasibleDeparture));
                return new GroupAssignmentResult(deferred, createdAssignationIds, true);
            }

            // Cannot assign this full set in the waiting window: push latest reservations to next group.
            List<Reservation> latestReservations = working.stream()
                    .filter(r -> r.getDateHeureArrivee().equals(currentDeparture))
                    .collect(Collectors.toList());

            deferred.addAll(latestReservations);
            working.removeAll(latestReservations);
        }

        // Nothing was assignable from this window.
        return new GroupAssignmentResult(deferred, createdAssignationIds, false);
    }

    private LocalDateTime findEarliestFeasibleDeparture(List<Reservation> reservations,
            LocalDateTime departureMin,
            LocalDateTime departureMax) throws Exception {
        LocalDateTime current = departureMin;
        while (!current.isAfter(departureMax)) {
            if (canAssignAllAtDeparture(reservations, current)) {
                return current;
            }
            current = current.plusMinutes(1);
        }
        return null;
    }

    private boolean canAssignAllAtDeparture(List<Reservation> reservations, LocalDateTime departure) throws Exception {
        List<Integer> remainingCapacities = new ArrayList<>();

        List<AssignationWithDetails> existingAssignations = assignationRepo.findWithDetailsByDateAndDepartAeroport(departure);
        for (AssignationWithDetails awd : existingAssignations) {
            Integer restePlace = awd.getRestePlace();
            if (restePlace != null && restePlace > 0) {
                remainingCapacities.add(restePlace);
            }
        }

        Set<Integer> busyVehiculeIds = assignationRepo.findBusyVehiculeIds(departure);
        List<Integer> freeVehicleCapacities = vehiculeRepo.findAll().stream()
                .filter(v -> !busyVehiculeIds.contains(v.getId()))
                .map(Vehicule::getPlace)
                .sorted()
                .collect(Collectors.toList());

        List<Reservation> sortedReservations = new ArrayList<>(reservations);
        sortedReservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

        for (Reservation r : sortedReservations) {
            int pax = r.getNbPassager();
            int idxBin = findBestFitCapacityIndex(remainingCapacities, pax);
            if (idxBin >= 0) {
                remainingCapacities.set(idxBin, remainingCapacities.get(idxBin) - pax);
                continue;
            }

            int idxVeh = findBestFitCapacityIndex(freeVehicleCapacities, pax);
            if (idxVeh < 0) {
                return false;
            }

            int vehCapacity = freeVehicleCapacities.remove(idxVeh);
            remainingCapacities.add(vehCapacity - pax);
        }
        return true;
    }

    private int findBestFitCapacityIndex(List<Integer> capacities, int required) {
        int bestIndex = -1;
        int bestCapacity = Integer.MAX_VALUE;

        for (int i = 0; i < capacities.size(); i++) {
            int cap = capacities.get(i);
            if (cap >= required && cap < bestCapacity) {
                bestCapacity = cap;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private Set<Integer> assignReservationsAtDeparture(List<Reservation> reservations, LocalDateTime departure)
            throws Exception {
        List<Reservation> sortedReservations = new ArrayList<>(reservations);
        sortedReservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

        Set<Integer> createdAssignationIds = new HashSet<>();
        for (Reservation r : sortedReservations) {
            Assignation a = assignReservation(r, departure);
            if (a != null && a.getId() != null) {
                createdAssignationIds.add(a.getId());
            }
        }

        return createdAssignationIds;
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

    private Vehicule trouverNouveauVehicule(Reservation r, LocalDateTime dateMax) {
        List<Vehicule> all = vehiculeRepo.findAll();
        List<Vehicule> pasDansMap = new ArrayList<>();
        Set<Integer> busyVehiculeIds = assignationRepo.findBusyVehiculeIds(dateMax);

        for (Vehicule v : all) {
            if (!busyVehiculeIds.contains(v.getId()) && v.getPlace() >= r.getNbPassager()) {
                pasDansMap.add(v);
            }
        }

        if (pasDansMap.isEmpty()) {
            return null;
        }

        return selectBestVehicle(pasDansMap, r.getNbPassager(), dateMax.toLocalDate());
    }

    private Assignation assignationExistanteDisponible(Reservation r, LocalDateTime dateDepartMax) throws Exception {
        try {
            List<AssignationWithDetails> vehiculesDispos = assignationRepo.findWithDetailsByDateAndDepartAeroport(dateDepartMax);

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
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public Assignation assignReservation(Reservation reservation, LocalDateTime dateDepartMax) throws Exception {
        Assignation existante = assignationExistanteDisponible(reservation, dateDepartMax);
        if (existante != null) {
            assignationDetailRepo.createDetail(existante.getId(), reservation.getId(), reservation.getNbPassager());
            return existante;
        }

        Vehicule vehicule = trouverNouveauVehicule(reservation, dateDepartMax);
        if (vehicule == null) {
            System.err.println("Aucun véhicule disponible pour la réservation " + reservation.getId());
            return null;
        }
        Integer newId = assignationRepo.createAssignation(vehicule.getId(), dateDepartMax, null);
        assignationDetailRepo.createDetail(newId, reservation.getId(), reservation.getNbPassager());

        return assignationRepo.findById(newId);
    }

    private Vehicule selectBestVehicle(List<Vehicule> vehicles, Integer requiredCapacity, LocalDate tripDate) {
        Map<Integer, Integer> tripsPerVehicle = new HashMap<>();
        for (Vehicule v : vehicles) {
            int trips = assignationRepo.countTripsForVehiculeOnDate(v.getId(), tripDate);
            tripsPerVehicle.put(v.getId(), trips);
        }

        vehicles.sort((v1, v2) -> {
            int placeCompare = Integer.compare(v1.getPlace(), v2.getPlace());
            if (placeCompare != 0) {
                return placeCompare;
            }

            int tripsV1 = tripsPerVehicle.getOrDefault(v1.getId(), 0);
            int tripsV2 = tripsPerVehicle.getOrDefault(v2.getId(), 0);
            int tripsCompare = Integer.compare(tripsV1, tripsV2);
            if (tripsCompare != 0) {
                return tripsCompare;
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
        double roundTripHours = (trajet.getDistance().doubleValue()) / vm;
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
            Integer aeroport = distanceRepo.getLieuIdByCode("AIR");
            Integer currentPoint = aeroport;

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

            BigDecimal retour = distanceRepo.getDistanceBetween(currentPoint, aeroport);
            segmentDistances.add(retour);
            totalDistance = totalDistance.add(retour);
            return new Trajet(totalDistance, sortedLieux, segmentDistances);

        } catch (Exception e) {
            System.err.println("Error finding trajet for assignation " + assignationId + ": " + e.getMessage());
            throw e;
        }
    }
}

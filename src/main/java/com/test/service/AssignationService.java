package com.test.service;

import com.test.dto.AssignationWithDetails;
import com.test.dto.DepartureAssignmentResult;
import com.test.dto.SplitReservationResult;
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

    public List<Reservation> getReservationsBetweenDates(List<Reservation> reservations, LocalDateTime start,
            LocalDateTime end) {
        List<Reservation> filteredList = reservations.stream()
                .filter(r -> !r.getDateHeureArrivee().isBefore(start) &&
                        !r.getDateHeureArrivee().isAfter(end))
                .collect(Collectors.toList());

        return filteredList;
    }

    public Map<LocalDateTime, List<Reservation>> groupReservations(List<Reservation> reservations) throws Exception {
        String taString = paramRepo.getValueByKey("ta");
        if (taString == null)
            throw new RuntimeException("Parameter 'ta' not found");
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

        while (!pendingReservations.isEmpty()) {
            LocalDateTime groupStart = pendingReservations.get(0).getDateHeureArrivee();
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

            if (result.hasAssigned() && !result.getDeferredReservations().isEmpty() && !pendingReservations.isEmpty()) {
                deferredToNextGroup.addAll(result.getDeferredReservations());
                deferredToNextGroup.sort(Comparator.comparing(Reservation::getDateHeureArrivee));
            } else if (!result.hasAssigned() && !result.getDeferredReservations().isEmpty()
                    && !pendingReservations.isEmpty()) {
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

    private List<Reservation> extractWindowReservations(List<Reservation> pendingReservations,
            LocalDateTime windowEnd) {
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

    private GroupAssignmentResult assignWindowWithFallback(List<Reservation> windowReservations,
            LocalDateTime windowEnd)
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

            LocalDateTime departureWithCapacity = findEarliestDepartureWithCapacity(currentDeparture, windowEnd);
            if (departureWithCapacity != null) {
                DepartureAssignmentResult departureResult = assignReservationsAtDepartureWithSplit(working,
                        departureWithCapacity);

                createdAssignationIds.addAll(departureResult.getCreatedAssignationIds());
                deferred.addAll(departureResult.getDeferredReservations());

                if (departureResult.hasAssigned()) {
                    return new GroupAssignmentResult(deferred, createdAssignationIds, true);
                }
            }

            // No progress for this timestamp batch in this waiting window: push it to next
            // group.
            List<Reservation> latestReservations = working.stream()
                    .filter(r -> r.getDateHeureArrivee().equals(currentDeparture))
                    .collect(Collectors.toList());

            deferred.addAll(latestReservations);
            working.removeAll(latestReservations);
        }

        // Nothing was assignable from this window.
        return new GroupAssignmentResult(deferred, createdAssignationIds, false);
    }

    private LocalDateTime findEarliestDepartureWithCapacity(LocalDateTime departureMin,
            LocalDateTime departureMax) {
        LocalDateTime current = departureMin;
        while (!current.isAfter(departureMax)) {
            if (hasAssignableCapacity(current)) {
                return current;
            }
            current = current.plusMinutes(1);
        }
        return null;
    }

    private boolean hasAssignableCapacity(LocalDateTime departure) {
        List<AssignationWithDetails> existingAssignations = assignationRepo
                .findWithDetailsByDateAndDepartAeroport(departure);
        for (AssignationWithDetails awd : existingAssignations) {
            Integer restePlace = awd.getRestePlace();
            if (restePlace != null && restePlace > 0) {
                return true;
            }
        }

        Set<Integer> busyVehiculeIds = assignationRepo.findBusyVehiculeIds(departure);
        return vehiculeRepo.findAll().stream().anyMatch(v -> !busyVehiculeIds.contains(v.getId()));
    }

    private DepartureAssignmentResult assignReservationsAtDepartureWithSplit(List<Reservation> reservations,
            LocalDateTime departure) throws Exception {
        List<Reservation> sortedReservations = new ArrayList<>(reservations);
        sortedReservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

        Set<Integer> createdAssignationIds = new HashSet<>();
        List<Reservation> deferredReservations = new ArrayList<>();
        boolean assignedAnyPassenger = false;

        for (Reservation reservation : sortedReservations) {
            SplitReservationResult splitResult = assignReservationWithSplit(reservation, departure);

            createdAssignationIds.addAll(splitResult.getCreatedAssignationIds());
            if (splitResult.getAssignedPassengers() > 0) {
                assignedAnyPassenger = true;
            }

            if (splitResult.getRemainingPassengers() > 0) {
                deferredReservations
                        .add(copyReservationWithPassengers(reservation, splitResult.getRemainingPassengers()));
            }
        }

        return new DepartureAssignmentResult(deferredReservations, createdAssignationIds, assignedAnyPassenger);
    }

    private SplitReservationResult assignReservationWithSplit(Reservation reservation, LocalDateTime departure)
            throws Exception {
        int remainingPassengers = reservation.getNbPassager() != null ? reservation.getNbPassager() : 0;
        int assignedPassengers = 0;
        Set<Integer> createdAssignationIds = new HashSet<>();

        if (remainingPassengers <= 0) {
            return new SplitReservationResult(assignedPassengers, remainingPassengers, createdAssignationIds);
        }

        // Business rule: first try to assign the full reservation in one go.
        Assignation fullAssignation = assignReservationAtomic(
                copyReservationWithPassengers(reservation, remainingPassengers),
                departure,
                false);
        if (fullAssignation != null) {
            if (fullAssignation.getId() != null) {
                createdAssignationIds.add(fullAssignation.getId());
            }
            return new SplitReservationResult(remainingPassengers, 0, createdAssignationIds);
        }

        // No single capacity can carry all passengers: split across available
        // capacities.
        while (remainingPassengers > 0) {
            int maxAssignableCapacity = findMaxAssignableCapacityAtDeparture(departure);
            if (maxAssignableCapacity <= 0) {
                break;
            }

            int chunkSize = Math.min(remainingPassengers, maxAssignableCapacity);
            Assignation partialAssignation = null;

            for (int candidateChunk = chunkSize; candidateChunk >= 1; candidateChunk--) {
                partialAssignation = assignReservationAtomic(
                        copyReservationWithPassengers(reservation, candidateChunk),
                        departure,
                        false);
                if (partialAssignation != null) {
                    chunkSize = candidateChunk;
                    break;
                }
            }

            if (partialAssignation == null) {
                break;
            }

            if (partialAssignation.getId() != null) {
                createdAssignationIds.add(partialAssignation.getId());
            }

            assignedPassengers += chunkSize;
            remainingPassengers -= chunkSize;
        }

        return new SplitReservationResult(assignedPassengers, remainingPassengers, createdAssignationIds);
    }

    private int findMaxAssignableCapacityAtDeparture(LocalDateTime departure) {
        int maxExistingCapacity = assignationRepo.findWithDetailsByDateAndDepartAeroport(departure).stream()
                .map(AssignationWithDetails::getRestePlace)
                .filter(Objects::nonNull)
                .filter(reste -> reste > 0)
                .max(Integer::compareTo)
                .orElse(0);

        Set<Integer> busyVehiculeIds = assignationRepo.findBusyVehiculeIds(departure);
        int maxFreeVehicleCapacity = vehiculeRepo.findAll().stream()
                .filter(v -> !busyVehiculeIds.contains(v.getId()))
                .map(Vehicule::getPlace)
                .max(Integer::compareTo)
                .orElse(0);

        return Math.max(maxExistingCapacity, maxFreeVehicleCapacity);
    }

    private Reservation copyReservationWithPassengers(Reservation source, int nbPassagers) {
        Reservation copy = new Reservation();
        copy.setId(source.getId());
        copy.setIdClient(source.getIdClient());
        copy.setDateHeureArrivee(source.getDateHeureArrivee());
        copy.setIdHotel(source.getIdHotel());
        copy.setNbPassager(nbPassagers);
        return copy;
    }

    public List<Reservation> getUnassignedReservationsForDate(LocalDate date) {
        List<Reservation> allReservations = reservationRepo.findByDate(date);

        Map<Integer, Integer> assignedPassengersByReservation = new HashMap<>();
        List<AssignationWithDetails> assignationsForDate = assignationRepo.findWithDetailsByDate(date);
        for (AssignationWithDetails assignation : assignationsForDate) {
            for (AssignationWithDetails.ReservationWithHotel assignedReservation : assignation.getReservations()) {
                Integer reservationId = assignedReservation.getReservationId();
                if (reservationId == null) {
                    continue;
                }
                int assignedPassengers = assignedReservation.getNbPersPrises() != null
                        ? assignedReservation.getNbPersPrises()
                        : 0;
                if (assignedPassengers <= 0) {
                    continue;
                }
                assignedPassengersByReservation.merge(reservationId, assignedPassengers, Integer::sum);
            }
        }

        List<Reservation> unassigned = new ArrayList<>();
        for (Reservation r : allReservations) {
            int totalPassengers = r.getNbPassager() != null ? r.getNbPassager() : 0;
            int assignedPassengers = assignedPassengersByReservation.getOrDefault(r.getId(), 0);
            int remainingPassengers = totalPassengers - assignedPassengers;

            if (remainingPassengers > 0) {
                if (remainingPassengers == totalPassengers) {
                    unassigned.add(r);
                } else {
                    unassigned.add(copyReservationWithPassengers(r, remainingPassengers));
                }
            }
        }

        return unassigned;
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
            List<AssignationWithDetails> vehiculesDispos = assignationRepo
                    .findWithDetailsByDateAndDepartAeroport(dateDepartMax);

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
        return assignReservationAtomic(reservation, dateDepartMax, true);
    }

    private Assignation assignReservationAtomic(Reservation reservation,
            LocalDateTime dateDepartMax,
            boolean logWhenUnavailable) throws Exception {
        Assignation existante = assignationExistanteDisponible(reservation, dateDepartMax);
        if (existante != null) {
            assignationDetailRepo.createDetail(existante.getId(), reservation.getId(), reservation.getNbPassager());
            return existante;
        }

        Vehicule vehicule = trouverNouveauVehicule(reservation, dateDepartMax);
        if (vehicule == null) {
            if (logWhenUnavailable) {
                System.err.println("Aucun véhicule disponible pour la réservation " + reservation.getId());
            }
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

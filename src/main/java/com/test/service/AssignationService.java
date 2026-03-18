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

    /**
     * Simulate assignment for a given date without persisting anything to the
     * database.
     * Returns a list of AssignationWithDetails representing the projected
     * assignations.
     */
    public List<AssignationWithDetails> simulateAssignationsForDate(LocalDate date) throws Exception {
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null)
            throw new RuntimeException("Parameter 'vm' not found in database");
        double vm = Double.parseDouble(vmString);

        List<Reservation> unassigned = getUnassignedReservationsForDate(date);
        unassigned.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        Map<LocalDateTime, List<Reservation>> groups = groupReservations(unassigned);
        /*for (Reservation r : unassigned) {
            groups.computeIfAbsent(r.getDateHeureArrivee(), k -> new ArrayList<>()).add(r);
        }*/
        for (List<Reservation> grp : groups.values()) {
            grp.sort(Comparator.comparing(Reservation::getNbPassager).reversed());
        }

        // Prepare hotel lookup (id -> Hotel) for display and trajet calculation
        List<Hotel> hotels = hotelRepo.findAll();
        Map<Integer, Hotel> hotelById = new HashMap<>();
        for (Hotel h : hotels)
            hotelById.put(h.getId(), h);

        List<Vehicule> allVehicules = vehiculeRepo.findAll();

        // In-memory virtual assignations being built
        List<AssignationWithDetails> virtualAssignations = new ArrayList<>();

        for (LocalDateTime timeSlot : groups.keySet()) {
            List<AssignationWithDetails> virtualAssignationsForSlot = new ArrayList<>();
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
                    Set<Integer> virtualBusy = findVirtualBusyVehiculeIds(timeSlot, virtualAssignations);
                    Set<Integer> allBusy = new HashSet<>(realBusy);
                    allBusy.addAll(virtualBusy);

                    List<Vehicule> candidates = new ArrayList<>();
                    for (Vehicule v : allVehicules) {
                        if (!allBusy.contains(v.getId()) && v.getPlace() >= r.getNbPassager()) {
                            candidates.add(v);
                        }
                    }
                    if (candidates.isEmpty())
                        continue;

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
                    virtualAssignationsForSlot.add(va);
                }
            }
            calculateAndUpdateDateRetourForVirtualAssignations(virtualAssignationsForSlot, hotelById);
        }

        return virtualAssignations;
    }
    
    private Set<Integer> findVirtualBusyVehiculeIds(LocalDateTime timeSlot, List<AssignationWithDetails> virtualAssignations) {
        Set<Integer> busy = new HashSet<>();
        for (AssignationWithDetails va : virtualAssignations) {
            if ((va.getDepartAeroport().isBefore(timeSlot) || va.getDepartAeroport().equals(timeSlot)) && (va.getRetourAeroport() == null || va.getRetourAeroport().isAfter(timeSlot))) {
                busy.add(va.getVehiculeId());
            }
        }
        return busy;
    }
    
    private void calculateAndUpdateDateRetourForVirtualAssignations(List<AssignationWithDetails> virtualAssignations, Map<Integer, Hotel> hotelById) throws Exception {
        for (AssignationWithDetails va : virtualAssignations) {
            calculateAndUpdateDateRetourForAVirtualAssignation(va, hotelById);
        }
    }

    private void calculateAndUpdateDateRetourForAVirtualAssignation(AssignationWithDetails va, Map<Integer, Hotel> hotelById) throws Exception {
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
            List<Integer> sortedLieuxIds = new ArrayList<>();
            List<BigDecimal> segmentDistances = new ArrayList<>();
            Integer aeroportId = distanceRepo.getLieuIdByCode("AIR");
            Integer current = aeroportId;
            while (!temp.isEmpty()) {
                Map.Entry<Integer, BigDecimal> nearest = distanceRepo.findNearest(current, temp);
                if (nearest == null)
                    break;
                sortedLieuxIds.add(nearest.getKey());
                segmentDistances.add(nearest.getValue());
                totalDist = totalDist.add(nearest.getValue());
                temp.remove(nearest.getKey());
                current = nearest.getKey();
            }
            BigDecimal retourDist = distanceRepo.getDistanceBetween(current, aeroportId);
            segmentDistances.add(retourDist);
            totalDist = totalDist.add(retourDist);
            String vmString = paramRepo.getValueByKey("vm");
            if (vmString == null) {
                throw new RuntimeException("Parameter 'vm' not found in database");
            }
            double vm = Double.parseDouble(vmString);
            double roundTripHours = (totalDist.doubleValue()) / vm;
            va.setRetourAeroport(va.getDepartAeroport().plusMinutes((long) (roundTripHours * 60)));

            List<String> lieuxNoms = lieuRepo.getLibelle(sortedLieuxIds);
            va.setTrajetLieuxNoms(lieuxNoms);
            va.setTrajetSegmentDistances(segmentDistances);
            va.setTrajetTotalDistance(totalDist);
        }
    }

    private AssignationWithDetails.ReservationWithHotel buildReservationWithHotel(Reservation r,
            Map<Integer, Hotel> hotelById) {
        AssignationWithDetails.ReservationWithHotel rwh = new AssignationWithDetails.ReservationWithHotel();
        rwh.setReservationId(r.getId());
        rwh.setIdClient(r.getIdClient());
        rwh.setNbPassager(r.getNbPassager());
        rwh.setNbPersPrises(r.getNbPassager());
        rwh.setDateHeureArrivee(r.getDateHeureArrivee());
        rwh.setIdHotel(r.getIdHotel());
        if (r.getIdHotel() != null) {
            Hotel h = hotelById.get(r.getIdHotel());
            if (h != null)
                rwh.setHotelNom(h.getNom());
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

        // Supp les assignations dans la base a cette date
        List<Assignation> listAssignations = assignationRepo.getByDepartAeroport(date.atStartOfDay());
        for (Assignation a : listAssignations) {
            List<AssignationDetail> details = assignationDetailRepo.findByAssignationId(a.getId());
            for (AssignationDetail d : details) {
                assignationDetailRepo.deleteById(d.getId());
                System.out.println("Details supprimes");
            }

            assignationRepo.deleteById(a.getId());
            System.out.println("Assignation supprime");
        }

        List<Reservation> unassignedReservations = getUnassignedReservationsForDate(date);
        unassignedReservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        Map<LocalDateTime, List<Reservation>> groups = groupReservations(unassignedReservations);
        // for (Reservation r : unassignedReservations) {
        //     LocalDateTime key = r.getDateHeureArrivee();
        //     List<Reservation> list = groups.get(key);
        //     if (list == null) {
        //         list = new ArrayList<>();
        //         groups.put(key, list);
        //     }
        //     list.add(r);
        // }

        for (List<Reservation> reservations : groups.values()) {
            reservations.sort(Comparator.comparing(Reservation::getNbPassager).reversed()); // pr que le nb de passager
                                                                                            // soit decroissant
        }

        List<Assignation> assignations = new ArrayList<>();
        for (LocalDateTime dateEntry : groups.keySet()) {
            List<Reservation> reservations = groups.get(dateEntry);
            List<Assignation> assignationsForGroup = new ArrayList<>();
            for (Reservation r : reservations) {
                Assignation a = assignReservation(r, date, dateEntry);
                if (a != null && assignations.stream().noneMatch(x -> x != null && x.getId().equals(a.getId()))) {
                    assignations.add(a);
                    assignationsForGroup.add(a);
                }
            }

            boolean updateRetour = calculateAndUpdateRetourAeroport(assignationsForGroup);
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

        return selectBestVehicle(pasDansMap, r.getNbPassager());
    }

    private Assignation assignationExistanteDisponible(Reservation r, LocalDate date, LocalDateTime dateDepartMax) throws Exception {
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

    public Assignation assignReservation(Reservation reservation, LocalDate date, LocalDateTime dateDepartMax) throws Exception {
        Assignation existante = assignationExistanteDisponible(reservation, date, dateDepartMax);
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

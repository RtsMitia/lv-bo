package com.test.service;

import com.test.dto.AssignationWithDetails;
import com.test.dto.ResaNADTO;
import com.test.dto.VehiculeDisponibiliteDTO;
import com.test.model.Assignation;
import com.test.model.Reservation;
import com.test.model.Trajet;
import com.test.model.Vehicule;
import com.test.repository.AssignationDetailRepository;
import com.test.repository.AssignationRepository;
import com.test.repository.DistanceRepository;
import com.test.repository.ParamRepository;
import com.test.repository.ReservationRepository;
import com.test.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignationService {

    private final ReservationRepository reservationRepo;
    private final VehiculeRepository vehiculeRepo;
    private final DistanceRepository distanceRepo;
    private final ParamRepository paramRepo;
    private final AssignationRepository assignationRepo;
    private final AssignationDetailRepository assignationDetailRepo;

    public AssignationService() {
        this.reservationRepo = new ReservationRepository();
        this.vehiculeRepo = new VehiculeRepository();
        this.distanceRepo = new DistanceRepository();
        this.paramRepo = new ParamRepository();
        this.assignationRepo = new AssignationRepository();
        this.assignationDetailRepo = new AssignationDetailRepository();
    }

    public List<Assignation> assignReservationsForDate(LocalDate date) throws Exception {
        int TA = getTa();
        List<Reservation> reservations = getReservations(date);
        reservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        List<ResaNADTO> resaNA = new ArrayList<>();
        List<Reservation> resaCp = new ArrayList<>(reservations);
        List<Assignation> result = new ArrayList<>();

        while (!resaCp.isEmpty() || !resaNA.isEmpty()) {

            List<VehiculeDisponibiliteDTO> vehicules = getVehiculesDisponibles();
            if (vehicules.isEmpty()) break;

            vehicules.sort(Comparator.comparing(VehiculeDisponibiliteDTO::getHeureDisponibilite));

            LocalDateTime plageDebut = vehicules.get(0).getHeureDisponibilite();

            if (resaNA.isEmpty() && !resaCp.isEmpty()) {
                plageDebut = resaCp.get(0).getDateHeureArrivee();
            }
            LocalDateTime plageFin = plageDebut.plusMinutes(TA);

            List<Reservation> listGroup = getReservationBetweenPlage(resaCp, plageDebut, plageFin);
            listGroup.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

            traiterResaNAPrioritaire(resaNA, vehicules, listGroup, resaCp, result, plageDebut, plageFin, TA);

            traiterGroupeNormal(resaNA, vehicules, listGroup, resaCp, result);

            calculateAndUpdateDateRetourFor(result);
        }

        return result;
    }

    private void traiterResaNAPrioritaire(
            List<ResaNADTO> resaNA,
            List<VehiculeDisponibiliteDTO> vehicules,
            List<Reservation> listGroup,
            List<Reservation> resaCp,
            List<Assignation> result,
            LocalDateTime plageDebut,
            LocalDateTime plageFin,
            int TA) {

        resaNA.sort(Comparator.comparing(ResaNADTO::getRestePersonne).reversed());
        Iterator<VehiculeDisponibiliteDTO> itVeh = vehicules.iterator();

        while (itVeh.hasNext() && !resaNA.isEmpty()) {

            VehiculeDisponibiliteDTO v = itVeh.next();
            int capaciteRestante = v.getVehicule().getPlace();

            Assignation A = createAssignation(
                    v.getIdVehicule(),
                    v.getHeureDisponibilite()
            );

            capaciteRestante = remplirDepuisResaNA(resaNA, capaciteRestante, A);

            if (capaciteRestante > 0) {
                completerVehicule(resaNA, listGroup, capaciteRestante, A, resaCp);
            } else {
                A.setDepartAeroport(v.getHeureDisponibilite());
                
                if(itVeh.hasNext()) {
                    VehiculeDisponibiliteDTO nextVeh = vehicules.get(vehicules.indexOf(v) + 1);
                    plageDebut = nextVeh.getHeureDisponibilite();
                    plageFin = plageDebut.plusMinutes(TA);
                    listGroup.clear();
                    listGroup.addAll(getReservationBetweenPlage(resaCp, plageDebut, plageFin));
                    listGroup.sort(Comparator.comparing(Reservation::getNbPassager).reversed());
                } else if (!resaCp.isEmpty()) {
                    plageDebut = resaCp.get(0).getDateHeureArrivee();
                    plageFin = plageDebut.plusMinutes(TA);
                    listGroup.clear();
                    listGroup.addAll(getReservationBetweenPlage(resaCp, plageDebut, plageFin));
                    listGroup.sort(Comparator.comparing(Reservation::getNbPassager).reversed());
                }
            }

            result.add(A);
            itVeh.remove();
        }
    }

    private void traiterGroupeNormal(
            List<ResaNADTO> resaNA,
            List<VehiculeDisponibiliteDTO> vehicules,
            List<Reservation> listGroup,
            List<Reservation> resaCp,
            List<Assignation> result) {
        
        List<Assignation> assignationsGroup = new ArrayList<>();

        LocalDateTime heureDepartGroupe = getHeureMax(listGroup);

        for (Reservation r : new ArrayList<>(listGroup)) {

            if (!resaCp.contains(r)) continue;

            VehiculeDisponibiliteDTO v = selectBestVehicule(r, vehicules);
            if (v == null) continue;

            heureDepartGroupe = max(heureDepartGroupe, v.getHeureDisponibilite());

            Assignation A = createAssignation(
                    v.getIdVehicule(),
                    heureDepartGroupe 
            );

            int capacite = v.getVehicule().getPlace();
            int nbPassager = r.getNbPassager() == null ? 0 : r.getNbPassager();
            int nbPris = Math.min(capacite, nbPassager);

            createAssignationDetail(A.getId(), r.getId(), nbPris);
            
            assignationsGroup.add(A);
            
            int reste = capacite - nbPris;

            if (nbPris < nbPassager) {
                resaNA.add(new ResaNADTO(r, nbPassager - nbPris));
            }

            resaCp.remove(r);
            listGroup.remove(r);
            vehicules.remove(v);

            if (reste > 0) {
                completerVehicule(resaNA, listGroup, reste, A, resaCp);
            }

            result.add(A);
        }
        
        // Update all assignations created for this group with the final max heureDepartGroupe
        for (Assignation assignation : assignationsGroup) {
            assignation.setDepartAeroport(heureDepartGroupe);
            assignationRepo.updateDepartAeroport(assignation.getId(), heureDepartGroupe);
        }
    }

    private int remplirDepuisResaNA(
            List<ResaNADTO> resaNA,
            int capaciteRestante,
            Assignation A) {

        Iterator<ResaNADTO> it = resaNA.iterator();

        while (it.hasNext() && capaciteRestante > 0) {

            ResaNADTO rna = it.next();

            int nbPris = Math.min(capaciteRestante, rna.getRestePersonne());

            createAssignationDetail(
                    A.getId(),
                    rna.getReservation().getId(),
                    nbPris
            );

            capaciteRestante -= nbPris;

            if (nbPris == rna.getRestePersonne()) {
                it.remove();
            } else {
                rna.setRestePersonne(rna.getRestePersonne() - nbPris);
            }
        }

        return capaciteRestante;
    }

    public void completerVehicule(
            List<ResaNADTO> resaNA,
            List<Reservation> listGroup,
            int capaciteRestante,
            Assignation A,
            List<Reservation> resaCp) {

        while (capaciteRestante > 0 && !resaNA.isEmpty()) {

            resaNA.sort(Comparator.comparing(ResaNADTO::getRestePersonne).reversed());

            ResaNADTO best = findBestResaNA(resaNA, capaciteRestante);
            if (best == null) break;

            int nbPris = Math.min(capaciteRestante, best.getRestePersonne());

            createAssignationDetail(
                    A.getId(),
                    best.getReservation().getId(),
                    nbPris
            );

            capaciteRestante -= nbPris;

            if (nbPris == best.getRestePersonne()) {
                resaNA.remove(best);
            } else {
                best.setRestePersonne(best.getRestePersonne() - nbPris);
            }
        }

        while (capaciteRestante > 0 && !listGroup.isEmpty()) {

            listGroup.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

            Reservation best = findBestReservation(listGroup, capaciteRestante);
            if (best == null) break;

            int nbPassager = best.getNbPassager() == null ? 0 : best.getNbPassager();
            int nbPris = Math.min(capaciteRestante, nbPassager);

            createAssignationDetail(
                    A.getId(),
                    best.getId(),
                    nbPris
            );

            capaciteRestante -= nbPris;

            if (nbPris < nbPassager) {
                resaNA.add(new ResaNADTO(best, nbPassager - nbPris));
            }

            listGroup.remove(best);
            resaCp.remove(best);
        }
    }

    // Helper Functions
    
    private void calculateAndUpdateDateRetourFor(List<Assignation> assignations) {
        calculateAndUpdateRetourAeroport(assignations);
    }
    
    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }
    
    private List<Reservation> getReservations(LocalDate date) {
        return reservationRepo.findByDate(date).stream()
                .filter(r -> r.getNbPassager() != null && r.getNbPassager() > 0)
                .collect(Collectors.toList());
    }

    private List<VehiculeDisponibiliteDTO> getVehiculesDisponibles() {
        List<Assignation> allAssignations = assignationRepo.findAll();
        Map<Integer, LocalDateTime> latestRetourByVehicule = new HashMap<>();

        for (Assignation assignation : allAssignations) {
            Integer vehiculeId = assignation.getVehicule();
            if (vehiculeId == null || assignation.getRetourAeroport() == null) continue;
            latestRetourByVehicule.merge(vehiculeId, assignation.getRetourAeroport(), this::max);
        }

        List<VehiculeDisponibiliteDTO> result = new ArrayList<>();
        for (Vehicule vehicule : vehiculeRepo.findAll()) {
            if (vehicule.getId() == null) continue;
            LocalDateTime heureDispo = latestRetourByVehicule.getOrDefault(vehicule.getId(), LocalDateTime.MIN);
            result.add(new VehiculeDisponibiliteDTO(vehicule.getId(), vehicule, heureDispo));
        }

        return result;
    }

    private List<Reservation> getReservationBetweenPlage(List<Reservation> reservations, LocalDateTime plageDebut, LocalDateTime plageFin) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getDateHeureArrivee() != null && !r.getDateHeureArrivee().isBefore(plageDebut) && !r.getDateHeureArrivee().isAfter(plageFin)) {
                result.add(r);
            }
        }
        return result;
    }

    private LocalDateTime getHeureMax(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getDateHeureArrivee)
                .filter(d -> d != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }

    private VehiculeDisponibiliteDTO selectBestVehicule(Reservation reservation, List<VehiculeDisponibiliteDTO> vehicules) {
        if (vehicules.isEmpty()) return null;
        int req = reservation.getNbPassager() != null ? reservation.getNbPassager() : 0;
        return vehicules.stream().min((a, b) -> {
            int ca = a.getVehicule().getPlace() != null ? a.getVehicule().getPlace() : 0;
            int cb = b.getVehicule().getPlace() != null ? b.getVehicule().getPlace() : 0;
            boolean af = ca >= req;
            boolean bf = cb >= req;
            if (af != bf) return af ? -1 : 1;
            int da = Math.abs(ca - req);
            int db = Math.abs(cb - req);
            if (da != db) return Integer.compare(da, db);
            if (a.getHeureDisponibilite() == null && b.getHeureDisponibilite() == null) return 0;
            if (a.getHeureDisponibilite() == null) return -1;
            if (b.getHeureDisponibilite() == null) return 1;
            return a.getHeureDisponibilite().compareTo(b.getHeureDisponibilite());
        }).orElse(null);
    }

    private ResaNADTO findBestResaNA(List<ResaNADTO> resaNA, int capaciteRestante) {
        ResaNADTO best = null;
        int minDiff = Integer.MAX_VALUE;
        for (ResaNADTO r : resaNA) {
            int diff = Math.abs(capaciteRestante - r.getRestePersonne());
            if (diff < minDiff) {
                minDiff = diff;
                best = r;
            }
        }
        return best;
    }

    private Reservation findBestReservation(List<Reservation> listGroup, int capaciteRestante) {
        Reservation best = null;
        int minDiff = Integer.MAX_VALUE;
        for (Reservation r : listGroup) {
            int pass = r.getNbPassager() != null ? r.getNbPassager() : 0;
            int diff = Math.abs(capaciteRestante - pass);
            if (diff < minDiff) {
                minDiff = diff;
                best = r;
            }
        }
        return best;
    }

    private Assignation createAssignation(Integer vehiculeId, LocalDateTime heureDepart) {
        if (vehiculeId == null || heureDepart == null) return null;
        Integer assignationId = assignationRepo.createAssignation(vehiculeId, heureDepart, null);
        if (assignationId == null) return null;
        return assignationRepo.findById(assignationId);
    }

    private void createAssignationDetail(Integer assignationId, Integer reservationId, int nbPris) {
        if (assignationId == null || reservationId == null || nbPris <= 0) return;
        assignationDetailRepo.createDetail(assignationId, reservationId, nbPris);
    }

    // Existing Functions
    private int getTa() throws Exception {
        String taString = paramRepo.getValueByKey("ta");
        if (taString == null) throw new RuntimeException("Parameter 'ta' not found");
        return Integer.parseInt(taString);
    }

    public List<Reservation> getUnassignedReservationsForDate(LocalDate date) {
        List<Reservation> allReservations = reservationRepo.findByDate(date);

        Map<Integer, Integer> assignedPassengersByReservation = new HashMap<>();
        List<AssignationWithDetails> assignationsForDate = assignationRepo.findWithDetailsByDate(date);
        for (AssignationWithDetails assignation : assignationsForDate) {
            if (assignation.getReservations() == null) continue;
            for (AssignationWithDetails.ReservationWithHotel assignedReservation : assignation.getReservations()) {
                Integer reservationId = assignedReservation.getReservationId();
                if (reservationId == null) continue;
                
                int assignedPassengers = assignedReservation.getNbPersPrises() != null
                        ? assignedReservation.getNbPersPrises()
                        : 0;
                if (assignedPassengers <= 0) continue;
                
                assignedPassengersByReservation.merge(reservationId, assignedPassengers, Integer::sum);
            }
        }

        List<Reservation> unassigned = new ArrayList<>();
        for (Reservation reservation : allReservations) {
            int totalPassengers = reservation.getNbPassager() != null ? reservation.getNbPassager() : 0;
            int assignedPassengers = assignedPassengersByReservation.getOrDefault(reservation.getId(), 0);
            int remainingPassengers = totalPassengers - assignedPassengers;

            if (remainingPassengers > 0) {
                if (remainingPassengers == totalPassengers) {
                    unassigned.add(reservation);
                } else {
                    unassigned.add(copyReservationWithPassengers(reservation, remainingPassengers));
                }
            }
        }

        return unassigned;
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

    public boolean calculateAndUpdateRetourAeroport(List<Assignation> assignations) {
        boolean allSuccess = true;
        try {
            for (Assignation assignation : assignations) {
                if (assignation != null && assignation.getId() != null) {
                    if (!calculateAndUpdateRetourAeroport(assignation)) allSuccess = false;
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
            System.err.println("Error config return for assignation " + assignation.getId() + ": " + e.getMessage());
            return false;
        }
    }

    public LocalDateTime calculateRetourAeroport(Assignation assignation) throws Exception {
        Trajet trajet = findTrajet(assignation.getId());
        String vmString = paramRepo.getValueByKey("vm");
        if (vmString == null) throw new RuntimeException("Parameter 'vm' not found in database");
        double vm = Double.parseDouble(vmString);
        double roundTripHours = (trajet.getDistance().doubleValue()) / vm;
        return assignation.getDepartAeroport().plusMinutes((long) (roundTripHours * 60));
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
                if (nearest == null) break;
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
            System.err.println("Error finding trajet " + assignationId + ": " + e.getMessage());
            throw e;
        }
    }
}

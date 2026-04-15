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
        assignationRepo.deleteByDepartDate(date);
        int TA = getTa();
        List<Reservation> reservations = getReservations(date);
        reservations.sort(Comparator.comparing(Reservation::getDateHeureArrivee));

        List<ResaNADTO> resaNA = new ArrayList<>();
        List<Reservation> resaCp = new ArrayList<>(reservations);
        List<Assignation> result = new ArrayList<>();

        while (!resaCp.isEmpty() || !resaNA.isEmpty()) {
            int beforeResaCpSize = resaCp.size();
            int beforeResaNASize = resaNA.size();
            int beforeResultSize = result.size();

            List<VehiculeDisponibiliteDTO> allVehicules = getVehiculesDisponibles(date);
            if (allVehicules.isEmpty()) break;

            LocalDateTime plageDebut = allVehicules.get(0).getHeureDisponibilite();

            if (resaNA.isEmpty() && !resaCp.isEmpty()) {
                plageDebut = min(plageDebut, resaCp.get(0).getDateHeureArrivee());
            }

            LocalDateTime plageFin = plageDebut.plusMinutes(TA);

            List<Reservation> listGroup = getReservationBetweenPlage(resaCp, plageDebut, plageFin);
            if (listGroup.isEmpty() && resaNA.isEmpty() && !resaCp.isEmpty()) {
                LocalDateTime prochaineArrivee = resaCp.get(0).getDateHeureArrivee();
                if (prochaineArrivee != null) {
                    plageDebut = prochaineArrivee;
                    plageFin = plageDebut.plusMinutes(TA);
                    listGroup = getReservationBetweenPlage(resaCp, plageDebut, plageFin);
                }
            }

            final LocalDateTime plageDebutFinal = plageDebut;
            List<VehiculeDisponibiliteDTO> vehicules = allVehicules.stream()
                    .filter(v -> v.getHeureDisponibilite() != null && !v.getHeureDisponibilite().isAfter(plageDebutFinal))
                    .collect(Collectors.toList());

            if (vehicules.isEmpty()) {
                // No vehicle is available at this slot yet, so move to the next vehicle availability.
                plageDebut = allVehicules.get(0).getHeureDisponibilite();
                plageFin = plageDebut.plusMinutes(TA);
                listGroup = getReservationBetweenPlage(resaCp, plageDebut, plageFin);

                final LocalDateTime adjustedPlageDebut = plageDebut;
                vehicules = allVehicules.stream()
                        .filter(v -> v.getHeureDisponibilite() != null && !v.getHeureDisponibilite().isAfter(adjustedPlageDebut))
                        .collect(Collectors.toList());

                if (vehicules.isEmpty()) {
                    break;
                }
            }

            listGroup.sort(Comparator.comparing(Reservation::getNbPassager).reversed());

            boolean startNextCycle = false;

            if (shouldPrioritizeResaNA(resaNA, listGroup, plageDebut)) {
                startNextCycle = traiterResaNAPrioritaire(resaNA, vehicules, listGroup, resaCp, result, plageDebut, plageFin, TA);
            }

            if (!startNextCycle) {
                startNextCycle = traiterGroupeNormal(resaNA, vehicules, listGroup, resaCp, result, plageDebut);
            }

            boolean noProgress = !startNextCycle && beforeResaCpSize == resaCp.size()
                    && beforeResaNASize == resaNA.size()
                    && beforeResultSize == result.size();
            if (noProgress) {
                break;
            }
        }

        return result;
    }

        private boolean shouldPrioritizeResaNA(List<ResaNADTO> resaNA, List<Reservation> listGroup, LocalDateTime plageDebut) {
        if (resaNA == null || resaNA.isEmpty()) return false;
        if (listGroup == null || listGroup.isEmpty()) return true;

        int maxResaNA = resaNA.stream()
            .mapToInt(ResaNADTO::getRestePersonne)
                .max()
                .orElse(0);

        int maxListGroupWaiting = listGroup.stream()
            .filter(r -> r.getDateHeureArrivee() != null && (plageDebut == null || !r.getDateHeureArrivee().isAfter(plageDebut)))
            .mapToInt(r -> r.getNbPassager() != null ? r.getNbPassager() : 0)
                .max()
                .orElse(0);

        return maxResaNA >= maxListGroupWaiting;
    }

    private boolean traiterResaNAPrioritaire(
            List<ResaNADTO> resaNA,
            List<VehiculeDisponibiliteDTO> vehicules,
            List<Reservation> listGroup,
            List<Reservation> resaCp,
            List<Assignation> result,
            LocalDateTime plageDebut,
            LocalDateTime plageFin,
            int TA) {

        if (vehicules.isEmpty() || resaNA.isEmpty()) return false;

        resaNA.sort(Comparator.comparing(ResaNADTO::getRestePersonne).reversed());
        LocalDateTime latestArriveePlage = getHeureMax(listGroup);
        
        // 1. Trouver l'heure des "premiers" véhicules arrivés (puisque la liste est triée par heure)
        LocalDateTime earliestTime = vehicules.get(0).getHeureDisponibilite();

        // 2. Regrouper tous les véhicules qui arrivent exactement "en même temps"
        List<VehiculeDisponibiliteDTO> earliestVehicles = new ArrayList<>();
        for (VehiculeDisponibiliteDTO v : vehicules) {
            if (v.getHeureDisponibilite().equals(earliestTime)) {
                earliestVehicles.add(v);
            } else {
                break; 
            }
        }

        // 3. Parmi les premiers arrivés, choisir le plus optimal pour la plus grosse resaNA restante
        int required = resaNA.get(0).getRestePersonne();
        VehiculeDisponibiliteDTO bestVeh = earliestVehicles.stream().min((a, b) -> {
            int ca = a.getVehicule().getPlace() != null ? a.getVehicule().getPlace() : 0;
            int cb = b.getVehicule().getPlace() != null ? b.getVehicule().getPlace() : 0;
            boolean af = ca >= required;
            boolean bf = cb >= required;
            if (af != bf) return af ? -1 : 1; // On privilégie la voiture qui a assez de places si possible
            int da = Math.abs(ca - required);
            int db = Math.abs(cb - required);
            if (da != db) return Integer.compare(da, db);
            return Integer.compare(earliestVehicles.indexOf(a), earliestVehicles.indexOf(b));
        }).orElse(earliestVehicles.get(0));

        VehiculeDisponibiliteDTO v = bestVeh;
        vehicules.remove(v);

        int capaciteRestante = v.getVehicule().getPlace() != null ? v.getVehicule().getPlace() : 0;

        Assignation A = createAssignation(
                v.getIdVehicule(),
                v.getHeureDisponibilite()
        );

        capaciteRestante = remplirDepuisResaNA(resaNA, capaciteRestante, A);

        if (capaciteRestante > 0) {
            Reservation exactMatch = null;
            for (Reservation r : listGroup) {
                if (r.getDateHeureArrivee() != null && r.getDateHeureArrivee().equals(v.getHeureDisponibilite())) {
                    int pass = r.getNbPassager() != null ? r.getNbPassager() : 0;
                    if (pass == capaciteRestante) {
                        exactMatch = r;
                        break;
                    }
                }
            }
            if (exactMatch != null) {
                createAssignationDetail(A.getId(), exactMatch.getId(), capaciteRestante);
                listGroup.remove(exactMatch);
                resaCp.remove(exactMatch);
                capaciteRestante = 0;
            }
        }

        if (capaciteRestante > 0) {
            capaciteRestante = completerVehicule(resaNA, listGroup, capaciteRestante, A, resaCp);
        }
        
        LocalDateTime departCible = max(v.getHeureDisponibilite(), latestArriveePlage);
        departCible = computeEffectiveDepartForAssignation(A, departCible);
        A.setDepartAeroport(departCible);
        assignationRepo.updateDepartAeroport(A.getId(), departCible);
        calculateAndUpdateRetourAeroport(A); // IMMEDIATE calculation
        result.add(A);
        
        return true;
    }

    private boolean traiterGroupeNormal(
            List<ResaNADTO> resaNA,
            List<VehiculeDisponibiliteDTO> vehicules,
            List<Reservation> listGroup,
            List<Reservation> resaCp,
            List<Assignation> result,
            LocalDateTime plageDebut) {

        if (listGroup.isEmpty()) return false;

        boolean processed = false;
        List<Assignation> regroupementAssignations = new ArrayList<>();
        LocalDateTime departRegroupement = null;

        for (Reservation r : new ArrayList<>(listGroup)) {

            if (!resaCp.contains(r)) continue;

            VehiculeDisponibiliteDTO v = selectBestVehicule(r, vehicules);
            if (v == null) continue;

                LocalDateTime departInitial = max(r.getDateHeureArrivee(), v.getHeureDisponibilite());

            Assignation A = createAssignation(
                    v.getIdVehicule(),
                    departInitial
            );

            int capacite = v.getVehicule().getPlace();
            int nbPassager = r.getNbPassager() == null ? 0 : r.getNbPassager();
            int nbPris = Math.min(capacite, nbPassager);

            createAssignationDetail(A.getId(), r.getId(), nbPris);

            processed = true;
            
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

            LocalDateTime departEffectifAssignation = computeEffectiveDepartForAssignation(A, departInitial);
            LocalDateTime latestArrival = getLatestArrivalForAssignation(A);
            boolean belongsToCurrentRegroupement = latestArrival != null
                    && (plageDebut == null || !latestArrival.isBefore(plageDebut));

            if (belongsToCurrentRegroupement) {
                regroupementAssignations.add(A);
                departRegroupement = max(departRegroupement, departEffectifAssignation);
            } else {
                A.setDepartAeroport(departEffectifAssignation);
                assignationRepo.updateDepartAeroport(A.getId(), departEffectifAssignation);
                calculateAndUpdateRetourAeroport(A);
            }

            result.add(A);
        }

        for (Assignation assignation : regroupementAssignations) {
            assignation.setDepartAeroport(departRegroupement);
            assignationRepo.updateDepartAeroport(assignation.getId(), departRegroupement);
            calculateAndUpdateRetourAeroport(assignation);
        }

        return processed;
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

    public int completerVehicule(
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
        
        return capaciteRestante;
    }

    // Helper Functions
    
    private void calculateAndUpdateDateRetourFor(List<Assignation> assignations) {
        calculateAndUpdateRetourAeroport(assignations);
    }

    private LocalDateTime computeEffectiveDepartForAssignation(Assignation assignation, LocalDateTime fallbackDepart) {
        if (assignation == null || assignation.getId() == null) return fallbackDepart;

        LocalDateTime latestArrival = getLatestArrivalForAssignation(assignation);
        return max(fallbackDepart, latestArrival);
    }

    private LocalDateTime getLatestArrivalForAssignation(Assignation assignation) {
        if (assignation == null || assignation.getId() == null) return null;

        try {
            AssignationWithDetails details = assignationRepo.getDetailAssignation(assignation.getId());
            if (details == null || details.getReservations() == null || details.getReservations().isEmpty()) {
                return null;
            }

            LocalDateTime latestArrival = null;
            for (AssignationWithDetails.ReservationWithHotel reservation : details.getReservations()) {
                latestArrival = max(latestArrival, reservation.getDateHeureArrivee());
            }
            return latestArrival;
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
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

    private List<VehiculeDisponibiliteDTO> getVehiculesDisponibles(LocalDate date) {
        // List<Assignation> allAssignations = assignationRepo.findAll();
        List<Assignation> allAssignations = assignationRepo.getByDate(date);
        Map<Integer, LocalDateTime> latestRetourByVehicule = new HashMap<>();
        Map<Integer, Integer> nbTrajetsByVehicule = new HashMap<>();

        for (Assignation assignation : allAssignations) {
            Integer vehiculeId = assignation.getVehicule();
            if (vehiculeId == null) continue;
            if (assignation.getRetourAeroport() != null) {
                latestRetourByVehicule.merge(vehiculeId, assignation.getRetourAeroport(), this::max);
            }
            nbTrajetsByVehicule.put(vehiculeId, nbTrajetsByVehicule.getOrDefault(vehiculeId, 0) + 1);
        }

        List<VehiculeDisponibiliteDTO> result = new ArrayList<>();
        for (Vehicule vehicule : vehiculeRepo.findAll()) {
            if (vehicule.getId() == null) continue;
            LocalDateTime heureDispoBase = date.atTime(
                    vehicule.getHeureDisponibilite() != null ? vehicule.getHeureDisponibilite() : java.time.LocalTime.MIDNIGHT
            );
            LocalDateTime latestRetour = latestRetourByVehicule.get(vehicule.getId());
            LocalDateTime heureDispo = max(heureDispoBase, latestRetour);
            result.add(new VehiculeDisponibiliteDTO(vehicule.getId(), vehicule, heureDispo));
        }

        result.sort(Comparator.comparing(VehiculeDisponibiliteDTO::getHeureDisponibilite)
                .thenComparingInt(v -> nbTrajetsByVehicule.getOrDefault(v.getIdVehicule(), 0))
                .thenComparingInt(v -> (v.getVehicule().getTypeCarburant() != null && v.getVehicule().getTypeCarburant().equalsIgnoreCase("D")) ? 0 : 1)
                .thenComparingInt(v -> v.getVehicule().getPlace() != null ? v.getVehicule().getPlace() : 0));

        return result;
    }

    private List<Reservation> getReservationBetweenPlage(List<Reservation> reservations, LocalDateTime plageDebut, LocalDateTime plageFin) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getDateHeureArrivee() != null && !r.getDateHeureArrivee().isAfter(plageFin)) {
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

        Map<Integer, Integer> nbTrajetsByVehicule = new HashMap<>();
        LocalDate dateRef = reservation.getDateHeureArrivee() != null ? reservation.getDateHeureArrivee().toLocalDate() : null;
        if (dateRef != null) {
            for (Assignation assignation : assignationRepo.getByDate(dateRef)) {
                Integer vehiculeId = assignation.getVehicule();
                if (vehiculeId == null) continue;
                nbTrajetsByVehicule.put(vehiculeId, nbTrajetsByVehicule.getOrDefault(vehiculeId, 0) + 1);
            }
        }

        return vehicules.stream().min((a, b) -> {
            int ca = a.getVehicule().getPlace() != null ? a.getVehicule().getPlace() : 0;
            int cb = b.getVehicule().getPlace() != null ? b.getVehicule().getPlace() : 0;
            boolean af = ca >= req;
            boolean bf = cb >= req;
            if (af != bf) return af ? -1 : 1;
            int da = Math.abs(ca - req);
            int db = Math.abs(cb - req);
            if (da != db) return Integer.compare(da, db);

            int ta = nbTrajetsByVehicule.getOrDefault(a.getIdVehicule(), 0);
            int tb = nbTrajetsByVehicule.getOrDefault(b.getIdVehicule(), 0);
            if (ta != tb) return Integer.compare(ta, tb);

            int fa = (a.getVehicule().getTypeCarburant() != null && a.getVehicule().getTypeCarburant().equalsIgnoreCase("D")) ? 0 : 1;
            int fb = (b.getVehicule().getTypeCarburant() != null && b.getVehicule().getTypeCarburant().equalsIgnoreCase("D")) ? 0 : 1;
            if (fa != fb) return Integer.compare(fa, fb);

            if (a.getHeureDisponibilite() == null && b.getHeureDisponibilite() == null) return 0;
            if (a.getHeureDisponibilite() == null) return -1;
            if (b.getHeureDisponibilite() == null) return 1;
            int timeCmp = a.getHeureDisponibilite().compareTo(b.getHeureDisponibilite());
            if (timeCmp != 0) return timeCmp;
            return Integer.compare(vehicules.indexOf(a), vehicules.indexOf(b));
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

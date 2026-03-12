package com.test.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.test.model.Trajet;
import com.test.repository.DistanceRepository;
import com.test.repository.LieuRepository;

public class TrajetService {
    private final DistanceRepository distanceRepo;
    private final LieuRepository lieuRepo;

    public TrajetService(DistanceRepository distanceRepo, LieuRepository lieuRepo) {
        this.distanceRepo = distanceRepo;
        this.lieuRepo = lieuRepo;
    }

    public TrajetService() {
        this.distanceRepo = new DistanceRepository();
        this.lieuRepo = new LieuRepository();
    }


    public Map<String, BigDecimal> getLieuAndDistanceMap(Trajet trajet) throws Exception{
        Map<String, BigDecimal> result = new java.util.HashMap<>();
        List<BigDecimal> segmentDistances = trajet.getSegmentDistances();
        List<Integer> lieuxIds = trajet.getLieuxIds();
        List<String> lieuNames = lieuRepo.getLibelle(lieuxIds);
        for (int i = 0; i < (lieuxIds.size() - 1); i++) {
            BigDecimal distance = segmentDistances.get(i);
            result.put(lieuNames.get(i), distance);
        }
        result.put("aeroport", segmentDistances.get(segmentDistances.size() - 1));
        return result;
    }
}

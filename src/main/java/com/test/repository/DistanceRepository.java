package com.test.repository;

import com.test.config.AppConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class DistanceRepository {
    public BigDecimal findDistanceFromAirportToLieu(Integer lieuId) throws Exception {
        Integer airportId = getLieuIdByCode("AIR");
        if (airportId == null) {
            throw new RuntimeException("Airport location not found");
        }

        return getDistanceBetween(airportId, lieuId);
    }

    public BigDecimal getDistanceBetween(Integer fromLieuId, Integer toLieuId) throws Exception {
        String sql = "SELECT distance FROM distance WHERE (\"from\" = ? AND \"to\" = ?) OR (\"from\" = ? AND \"to\" = ?)";

        try (Connection connection = AppConfig.createDataSource().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, fromLieuId);
            stmt.setInt(2, toLieuId);
            stmt.setInt(3, toLieuId);
            stmt.setInt(4, fromLieuId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("distance");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public Map.Entry<Integer, BigDecimal> findNearest(Integer fromLieuId, List<Integer> toLieuIds) throws Exception {
        Integer nearestLieuId = null;
        BigDecimal shortestDistance = new BigDecimal(Double.MAX_VALUE);

        for (Integer toLieuId : toLieuIds) {
            BigDecimal distance = getDistanceBetween(fromLieuId, toLieuId);
            if (distance != null && distance.compareTo(shortestDistance) < 0) {
                shortestDistance = distance;
                nearestLieuId = toLieuId;
            }
        }

        return nearestLieuId != null ? Map.entry(nearestLieuId, shortestDistance) : null;
    }

    /**
     * Get lieu ID by code
     */
    public Integer getLieuIdByCode(String code) throws Exception {
        String sql = "SELECT id FROM lieux WHERE code = ?";

        try (Connection connection = AppConfig.createDataSource().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return null;
    }
}

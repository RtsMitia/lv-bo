package com.test.repository;

import com.test.config.AppConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class DistanceRepository {

    /**
     * Find distance between two locations by their IDs using Dijkstra's algorithm
     */
    public BigDecimal findDistanceBetween(Integer fromLieuId, Integer toLieuId) throws Exception {
        if (fromLieuId.equals(toLieuId)) {
            return BigDecimal.ZERO;
        }

        // Build graph of all distances
        Map<Integer, Map<Integer, BigDecimal>> graph = buildDistanceGraph();
        
        // Use Dijkstra to find shortest path
        return dijkstra(graph, fromLieuId, toLieuId);
    }

    /**
     * Find distance from airport to a specific location by location code using Dijkstra
     */
    public BigDecimal findDistanceFromAirportToLieu(String lieuCode) throws Exception {
        // Get airport ID
        Integer airportId = getLieuIdByCode("AIR");
        if (airportId == null) {
            throw new RuntimeException("Airport location not found");
        }
        
        // Get destination ID
        Integer destinationId = getLieuIdByCode(lieuCode);
        if (destinationId == null) {
            throw new RuntimeException("Location not found: " + lieuCode);
        }
        
        return findDistanceBetween(airportId, destinationId);
    }

    /**
     * Build a graph of all distances (bidirectional)
     */
    private Map<Integer, Map<Integer, BigDecimal>> buildDistanceGraph() throws Exception {
        Map<Integer, Map<Integer, BigDecimal>> graph = new HashMap<>();
        
        String sql = "SELECT \"from\", \"to\", distance FROM distance";
        
        try (Connection connection = AppConfig.createDataSource().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Integer from = rs.getInt("from");
                Integer to = rs.getInt("to");
                BigDecimal distance = rs.getBigDecimal("distance");
                
                // Add bidirectional edges
                graph.putIfAbsent(from, new HashMap<>());
                graph.putIfAbsent(to, new HashMap<>());
                
                graph.get(from).put(to, distance);
                graph.get(to).put(from, distance); // Bidirectional
            }
        }
        
        return graph;
    }

    /**
     * Dijkstra's algorithm to find shortest path distance
     */
    private BigDecimal dijkstra(Map<Integer, Map<Integer, BigDecimal>> graph, 
                                Integer start, 
                                Integer end) {
        // Distance from start to each node
        Map<Integer, BigDecimal> distances = new HashMap<>();
        
        // Priority queue: [nodeId, distanceFromStart]
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparing(n -> n.distance));
        
        // Visited nodes
        Set<Integer> visited = new HashSet<>();
        
        // Initialize
        distances.put(start, BigDecimal.ZERO);
        pq.offer(new Node(start, BigDecimal.ZERO));
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            Integer currentId = current.id;
            
            // If we reached the destination
            if (currentId.equals(end)) {
                return current.distance;
            }
            
            // Skip if already visited
            if (visited.contains(currentId)) {
                continue;
            }
            
            visited.add(currentId);
            
            // Get neighbors
            Map<Integer, BigDecimal> neighbors = graph.get(currentId);
            if (neighbors == null) {
                continue;
            }
            
            // Process each neighbor
            for (Map.Entry<Integer, BigDecimal> entry : neighbors.entrySet()) {
                Integer neighborId = entry.getKey();
                BigDecimal edgeDistance = entry.getValue();
                
                if (visited.contains(neighborId)) {
                    continue;
                }
                
                BigDecimal newDistance = current.distance.add(edgeDistance);
                BigDecimal oldDistance = distances.getOrDefault(neighborId, new BigDecimal(Double.MAX_VALUE));
                
                if (newDistance.compareTo(oldDistance) < 0) {
                    distances.put(neighborId, newDistance);
                    pq.offer(new Node(neighborId, newDistance));
                }
            }
        }
        
        // No path found
        return null;
    }

    /**
     * Get lieu ID by code
     */
    private Integer getLieuIdByCode(String code) throws Exception {
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

    /**
     * Helper class for Dijkstra's algorithm
     */
    private static class Node {
        Integer id;
        BigDecimal distance;
        
        Node(Integer id, BigDecimal distance) {
            this.id = id;
            this.distance = distance;
        }
    }
}

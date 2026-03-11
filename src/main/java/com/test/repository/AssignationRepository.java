package com.test.repository;

import com.test.config.AppConfig;
import com.test.dto.AssignationWithDetails;
import com.test.model.Assignation;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssignationRepository {
    private final DataSource ds;

    public AssignationRepository() {
        this.ds = AppConfig.createDataSource();
    }

    public List<Assignation> findAll() {
        List<Assignation> list = new ArrayList<>();
        String sql = "SELECT id, vehicule, depart_aeroport, retour_aeroport FROM assignation";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Assignation a = new Assignation();
                a.setId(rs.getInt("id"));
                a.setVehicule(rs.getInt("vehicule"));
                Timestamp departTs = rs.getTimestamp("depart_aeroport");
                if (departTs != null)
                    a.setDepartAeroport(departTs.toLocalDateTime());
                Timestamp retourTs = rs.getTimestamp("retour_aeroport");
                if (retourTs != null)
                    a.setRetourAeroport(retourTs.toLocalDateTime());
                list.add(a);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignations", e);
        }
        return list;
    }

    public Assignation findById(int id) {
        String sql = "SELECT id, vehicule, depart_aeroport, retour_aeroport FROM assignation WHERE id = ?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Assignation a = new Assignation();
                    a.setId(rs.getInt("id"));
                    a.setVehicule(rs.getInt("vehicule"));
                    Timestamp departTs = rs.getTimestamp("depart_aeroport");
                    if (departTs != null)
                        a.setDepartAeroport(departTs.toLocalDateTime());
                    Timestamp retourTs = rs.getTimestamp("retour_aeroport");
                    if (retourTs != null)
                        a.setRetourAeroport(retourTs.toLocalDateTime());
                    return a;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignation by id", e);
        }
        return null;
    }

    public List<AssignationWithDetails> findWithDetailsByDate(LocalDate date) {
        List<AssignationWithDetails> list = new ArrayList<>();
        Map<Integer, AssignationWithDetails> assignationMap = new HashMap<>();

        String sql = "SELECT " +
                "a.id AS assignation_id, " +
                "a.vehicule AS vehicule_id, " +
                "a.nom_vehicule, " +
                "a.vehicule_place, " +
                "a.depart_aeroport, " +
                "a.retour_aeroport, " +
                "a.total_passagers, " +
                "a.reste_place, " +
                "ad.id_reservation, " +
                "ad.nb_pers_prises, " +
                "r.id_client, " +
                "r.nb_passager, " +
                "r.date_heure_arrivee, " +
                "r.id_hotel, " +
                "h.nom AS hotel_nom " +
                "FROM assignation_lib a " +
                "LEFT JOIN assignation_detail ad ON ad.id_association = a.id " +
                "LEFT JOIN reservation r ON r.id = ad.id_reservation " +
                "LEFT JOIN hotel h ON h.id = r.id_hotel " +
                "WHERE CAST(a.depart_aeroport AS DATE) = ? " +
                "ORDER BY a.id, ad.id_reservation";

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer assignationId = rs.getInt("assignation_id");

                    AssignationWithDetails assignation = assignationMap.get(assignationId);
                    if (assignation == null) {
                        assignation = new AssignationWithDetails();
                        assignation.setAssignationId(assignationId);
                        assignation.setVehiculeId(rs.getInt("vehicule_id"));
                        assignation.setVehiculeReference(rs.getString("nom_vehicule"));
                        assignation.setVehiculePlace(rs.getInt("vehicule_place"));

                        Timestamp departTs = rs.getTimestamp("depart_aeroport");
                        if (departTs != null)
                            assignation.setDepartAeroport(departTs.toLocalDateTime());

                        Timestamp retourTs = rs.getTimestamp("retour_aeroport");
                        if (retourTs != null)
                            assignation.setRetourAeroport(retourTs.toLocalDateTime());

                        assignation.setTotalPassagers(rs.getInt("total_passagers"));
                        assignation.setRestePlace(rs.getInt("reste_place"));

                        assignationMap.put(assignationId, assignation);
                        list.add(assignation);
                    }

                    Integer reservationId = (Integer) rs.getObject("id_reservation");
                    if (reservationId != null) {
                        AssignationWithDetails.ReservationWithHotel reservation = new AssignationWithDetails.ReservationWithHotel();
                        reservation.setReservationId(reservationId);
                        reservation.setIdClient(rs.getString("id_client"));
                        reservation.setNbPassager(rs.getInt("nb_passager"));

                        Timestamp dateHeureTs = rs.getTimestamp("date_heure_arrivee");
                        if (dateHeureTs != null)
                            reservation.setDateHeureArrivee(dateHeureTs.toLocalDateTime());

                        reservation.setIdHotel((Integer) rs.getObject("id_hotel"));
                        reservation.setHotelNom(rs.getString("hotel_nom"));
                        reservation.setNbPersPrises(rs.getInt("nb_pers_prises"));

                        assignation.addReservation(reservation);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignations with details by date", e);
        }

        return list;
    }

    /**
     * Create a new assignation (vehicle trip) and return the generated ID
     */
    public Integer createAssignation(Integer vehiculeId, java.time.LocalDateTime departAeroport,
            java.time.LocalDateTime retourAeroport) {
        String sql = "INSERT INTO assignation (vehicule, depart_aeroport, retour_aeroport) VALUES (?, ?, ?)";

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, vehiculeId);
            if (departAeroport != null) {
                ps.setTimestamp(2, Timestamp.valueOf(departAeroport));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            if (retourAeroport != null) {
                ps.setTimestamp(3, Timestamp.valueOf(retourAeroport));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating assignation", e);
        }
        return null;
    }

    public boolean updateHeureRetourAeroport(Integer assignationId, java.time.LocalDateTime retourAeroport) {
        String sql = "UPDATE assignation SET retour_aeroport = ? WHERE id = ?";

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(retourAeroport));
            ps.setInt(2, assignationId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating retour_aeroport for assignation id " + assignationId, e);
        }
    }

    /**
     * Returns IDs of vehicles that are currently on a trip at the given time:
     * either the trip has started but retour_aeroport is null (not yet calculated),
     * or retour_aeroport is set but hasn't passed yet.
     */
    public Set<Integer> findBusyVehiculeIds(LocalDateTime atTime) {
        String sql = "SELECT DISTINCT vehicule FROM assignation " +
                     "WHERE depart_aeroport <= ? " +
                     "AND (retour_aeroport IS NULL OR retour_aeroport > ?)";
        Set<Integer> busyIds = new HashSet<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(atTime));
            ps.setTimestamp(2, Timestamp.valueOf(atTime));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    busyIds.add(rs.getInt("vehicule"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching busy vehicule ids at " + atTime, e);
        }
        return busyIds;
    }

    public List<Integer> findLieuxIds(Integer assignationId) {
        String sql = "SELECT DISTINCT h.id_lieu FROM assignation a " +
                "JOIN assignation_detail ad ON a.id = ad.id_association " +
                "JOIN reservation r ON ad.id_reservation = r.id " +
                "JOIN hotel h ON r.id_hotel = h.id " +
                "WHERE a.id = ?";

        List<Integer> lieuxIds = new ArrayList<>();
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, assignationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lieuxIds.add(rs.getInt("id_lieu"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching lieux ids for assignation id " + assignationId, e);
        }
        return lieuxIds;
    }

    public AssignationWithDetails getDetailAssignation(Integer assignationId) throws Exception {
        if (assignationId == null) {
            throw new IllegalArgumentException("assignationId is null");
        }

        String sql = "SELECT " +
                "a.id AS assignation_id, " +
                "a.vehicule AS vehicule_id, " +
                "a.nom_vehicule, " +
                "a.vehicule_place, " +
                "a.depart_aeroport, " +
                "a.retour_aeroport, " +
                "a.total_passagers, " +
                "a.reste_place, " +
                "ad.id_reservation, " +
                "ad.nb_pers_prises, " +
                "r.id_client, " +
                "r.nb_passager, " +
                "r.date_heure_arrivee, " +
                "r.id_hotel, " +
                "h.nom AS hotel_nom " +
                "FROM assignation_lib a " +
                "LEFT JOIN assignation_detail ad ON ad.id_association = a.id " +
                "LEFT JOIN reservation r ON r.id = ad.id_reservation " +
                "LEFT JOIN hotel h ON h.id = r.id_hotel " +
                "WHERE a.id = ? " +
                "ORDER BY a.id, ad.id_reservation";

        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, assignationId);

            try (ResultSet rs = ps.executeQuery()) {
                AssignationWithDetails assignation = null;
                while (rs.next()) {
                    if (assignation == null) {
                        assignation = new AssignationWithDetails();
                        assignation.setAssignationId(rs.getInt("assignation_id"));
                        assignation.setVehiculeId(rs.getInt("vehicule_id"));
                        assignation.setVehiculeReference(rs.getString("nom_vehicule"));
                        assignation.setVehiculePlace(rs.getInt("vehicule_place"));

                        Timestamp departTs = rs.getTimestamp("depart_aeroport");
                        if (departTs != null)
                            assignation.setDepartAeroport(departTs.toLocalDateTime());

                        Timestamp retourTs = rs.getTimestamp("retour_aeroport");
                        if (retourTs != null)
                            assignation.setRetourAeroport(retourTs.toLocalDateTime());

                        assignation.setTotalPassagers(rs.getInt("total_passagers"));
                        assignation.setRestePlace(rs.getInt("reste_place"));
                    }

                    Integer reservationId = (Integer) rs.getObject("id_reservation");
                    if (reservationId != null) {
                        AssignationWithDetails.ReservationWithHotel reservation = new AssignationWithDetails.ReservationWithHotel();
                        reservation.setReservationId(reservationId);
                        reservation.setIdClient(rs.getString("id_client"));
                        reservation.setNbPassager(rs.getInt("nb_passager"));

                        Timestamp dateHeureTs = rs.getTimestamp("date_heure_arrivee");
                        if (dateHeureTs != null)
                            reservation.setDateHeureArrivee(dateHeureTs.toLocalDateTime());

                        reservation.setIdHotel((Integer) rs.getObject("id_hotel"));
                        reservation.setHotelNom(rs.getString("hotel_nom"));
                        reservation.setNbPersPrises(rs.getInt("nb_pers_prises"));

                        assignation.addReservation(reservation);
                    }
                }

                return assignation;
            }

        } catch (SQLException e) {
            throw new Exception("Error fetching assignation details for id " + assignationId, e);
        }
    }
}

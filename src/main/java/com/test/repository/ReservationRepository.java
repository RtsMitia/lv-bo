package com.test.repository;

import com.test.config.AppConfig;
import com.test.model.Reservation;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {
    private final DataSource ds;

    public ReservationRepository() {
        this.ds = AppConfig.createDataSource();
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT id, id_client, nb_passager, date_heure_arrivee, id_hotel FROM reservation";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setIdClient(rs.getString("id_client"));
                int nb = rs.getInt("nb_passager");
                r.setNbPassager(rs.wasNull() ? null : nb);
                Timestamp ts = rs.getTimestamp("date_heure_arrivee");
                if (ts != null)
                    r.setDateHeureArrivee(ts.toLocalDateTime());
                Object idHotelObj = rs.getObject("id_hotel");
                r.setIdHotel(idHotelObj == null ? null : ((Number) idHotelObj).intValue());
                list.add(r);
            }

            for (Reservation reservation : list) {
                System.out.println(reservation.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservations", e);
        }

        return list;
    }

    public List<Reservation> findByDate(java.time.LocalDate date) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT id, id_client, nb_passager, date_heure_arrivee, id_hotel FROM reservation WHERE CAST(date_heure_arrivee AS DATE) = ?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setIdClient(rs.getString("id_client"));
                    int nb = rs.getInt("nb_passager");
                    r.setNbPassager(rs.wasNull() ? null : nb);
                    Timestamp ts = rs.getTimestamp("date_heure_arrivee");
                    if (ts != null)
                        r.setDateHeureArrivee(ts.toLocalDateTime());
                    Object idHotelObj = rs.getObject("id_hotel");
                    r.setIdHotel(idHotelObj == null ? null : ((Number) idHotelObj).intValue());
                    list.add(r);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservations by date", e);
        }

        return list;
    }

    public Reservation save(Reservation r) {
        String sql = "INSERT INTO reservation (id_client, nb_passager, date_heure_arrivee, id_hotel) VALUES (?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getIdClient());

            if (r.getNbPassager() != null)
                ps.setInt(2, r.getNbPassager());
            else
                ps.setNull(2, Types.INTEGER);

            if (r.getDateHeureArrivee() != null)
                ps.setTimestamp(3, Timestamp.valueOf(r.getDateHeureArrivee()));
            else
                ps.setNull(3, Types.TIMESTAMP);

            if (r.getIdHotel() != null)
                ps.setInt(4, r.getIdHotel());
            else
                ps.setNull(4, Types.INTEGER);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getInt(1));
                }
            }

            return r;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving reservation", e);
        }
    }
}

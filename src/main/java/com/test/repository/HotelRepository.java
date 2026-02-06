package com.test.repository;

import com.test.config.AppConfig;
import com.test.model.Hotel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HotelRepository {
    private final DataSource ds;

    public HotelRepository() {
        this.ds = AppConfig.createDataSource();
    }

    public List<Hotel> findAll() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT id, nom FROM hotel";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Hotel h = new Hotel();
                h.setId(rs.getInt("id"));
                h.setNom(rs.getString("nom"));
                list.add(h);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching hotels", e);
        }
        return list;
    }

    public Hotel findById(int id) {
        String sql = "SELECT id, nom FROM hotel WHERE id = ?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Hotel h = new Hotel();
                    h.setId(rs.getInt("id"));
                    h.setNom(rs.getString("nom"));
                    return h;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching hotel by id", e);
        }
        return null;
    }
}

package com.test.repository;

import com.test.config.AppConfig;
import com.test.model.Lieu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuRepository {

    public List<Lieu> findAll() throws Exception {
        List<Lieu> lieux = new ArrayList<>();
        try (Connection connection = AppConfig.createDataSource().getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT id, code, libelle FROM lieux");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Lieu lieu = new Lieu();
                lieu.setId(rs.getInt("id"));
                lieu.setCode(rs.getString("code"));
                lieu.setLibelle(rs.getString("libelle"));
                lieux.add(lieu);
            }
        }
        return lieux;
    }

    public Lieu findByCode(String code) throws Exception {
        try (Connection connection = AppConfig.createDataSource().getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT id, code, libelle FROM lieux WHERE code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Lieu lieu = new Lieu();
                    lieu.setId(rs.getInt("id"));
                    lieu.setCode(rs.getString("code"));
                    lieu.setLibelle(rs.getString("libelle"));
                    return lieu;
                }
            }
        }
        return null;
    }

    public Lieu getById(Integer id) throws Exception {
        try (Connection connection = AppConfig.createDataSource().getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT id, code, libelle FROM lieux WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Lieu lieu = new Lieu();
                    lieu.setId(rs.getInt("id"));
                    lieu.setCode(rs.getString("code"));
                    lieu.setLibelle(rs.getString("libelle"));
                    return lieu;
                }
            }
        }
        return null;
    }
}

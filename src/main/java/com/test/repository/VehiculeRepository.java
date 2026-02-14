package com.test.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.sql.DataSource;

import com.test.config.AppConfig;
import com.test.model.Vehicule;

public class VehiculeRepository {

    private final DataSource ds;
    
    public VehiculeRepository() {
        this.ds = AppConfig.createDataSource();
    }
    
    public List<Vehicule> findAll() {

        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";
        try(Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setPlace(rs.getInt("place"));
                vehicule.setTypeCarburant(rs.getString("type_carburant"));
                vehicules.add(vehicule);
            }

            return vehicules;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching vehicules", e);
        }

    }

    public Vehicule getById(Integer id) {
        
        Vehicule vehicule = new Vehicule();
        String sql = "SELECT * FROM vehicule WHERE id = ?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setReference(rs.getString("reference"));
                    vehicule.setPlace(rs.getInt("place"));
                    vehicule.setTypeCarburant(rs.getString("type_carburant"));
                } else {
                    throw new RuntimeException("No vehicule with id = " + id);
                }
            } catch (Exception e) {
               throw new RuntimeException("Error fetching vehicule with id = " + id, e);
            }

            return vehicule;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching vehicules = " + id, e);
        }
    }

    public Vehicule save(Vehicule vehicule) {
        try (Connection c = ds.getConnection()) {

            String sql = "INSERT INTO vehicule (reference, place, type_carburant) VALUES (?, ?, ?)";
            if (vehicule.getId() != null) {
                sql = "UPDATE vehicule SET reference = ?, " + 
                        "place = ?, type_carburant = ? " + 
                        "WHERE id = ?";
                try (PreparedStatement ps2 = c.prepareStatement(sql)) {
                    ps2.setString(1, vehicule.getReference());
                    ps2.setInt(2, vehicule.getPlace());
                    ps2.setString(3, vehicule.getTypeCarburant());
                    ps2.setInt(4, vehicule.getId());
                    
                    int rowsAffected = ps2.executeUpdate();
                    return vehicule;
                } catch (RuntimeException e) {
                    throw new RuntimeException("Error while updating vehicule with reference = " + vehicule.getReference(), e);
                }
            } 

            try (PreparedStatement ps2 = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps2.setString(1, vehicule.getReference());
                ps2.setInt(2, vehicule.getPlace());
                ps2.setString(3, vehicule.getTypeCarburant());

                int rowsAffected = ps2.executeUpdate();
                try (ResultSet keys = ps2.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        try {
                            int generatedId = keys.getInt(1);
                            vehicule.setId(generatedId);
                        } catch (SQLException ignore) {
                            // ignore if driver doesn't return numeric id
                        }
                    }
                }
                return vehicule;

            } catch (Exception e) {
                throw new RuntimeException("Error while saving vehicule with reference = " + vehicule.getReference(), e);
            }
            
            
        } catch (Exception e) {
            throw new RuntimeException("Error while saving vehicule with reference = " + vehicule.getReference(), e);
        }
    }

    public void deleteById(Integer id) {
        String sql = "DELETE FROM vehicule WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting vehicule with id = " + id, e);
        }
    }
}

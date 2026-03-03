package com.test.repository;

import com.test.config.AppConfig;
import com.test.model.AssignationDetail;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignationDetailRepository {
    private final DataSource ds;

    public AssignationDetailRepository() {
        this.ds = AppConfig.createDataSource();
    }

    public List<AssignationDetail> findAll() {
        List<AssignationDetail> list = new ArrayList<>();
        String sql = "SELECT id, id_association, id_reservation, nb_pers_prises FROM assignation_detail";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AssignationDetail ad = new AssignationDetail();
                ad.setId(rs.getInt("id"));
                ad.setIdAssociation(rs.getInt("id_association"));
                ad.setIdReservation(rs.getInt("id_reservation"));
                ad.setNbPersPrises(rs.getInt("nb_pers_prises"));
                list.add(ad);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignation details", e);
        }
        return list;
    }

    public List<AssignationDetail> findByAssignationId(int assignationId) {
        List<AssignationDetail> list = new ArrayList<>();
        String sql = "SELECT id, id_association, id_reservation, nb_pers_prises FROM assignation_detail WHERE id_association = ?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assignationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssignationDetail ad = new AssignationDetail();
                    ad.setId(rs.getInt("id"));
                    ad.setIdAssociation(rs.getInt("id_association"));
                    ad.setIdReservation(rs.getInt("id_reservation"));
                    ad.setNbPersPrises(rs.getInt("nb_pers_prises"));
                    list.add(ad);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignation details by assignation id", e);
        }
        return list;
    }
}

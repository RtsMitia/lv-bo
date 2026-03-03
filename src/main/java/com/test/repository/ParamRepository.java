package com.test.repository;

import com.test.config.AppConfig;
import java.sql.*;

public class ParamRepository {

    /**
     * Get parameter value by key
     */
    public String getValueByKey(String key) throws Exception {
        try (Connection connection = AppConfig.createDataSource().getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT valeur FROM param WHERE cle = ?")) {
            stmt.setString(1, key);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valeur");
                }
            }
        }
        return null;
    }
}

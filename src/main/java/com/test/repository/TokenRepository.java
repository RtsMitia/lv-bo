package com.test.repository;

import com.test.config.AppConfig;
import com.test.model.ApiToken;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

public class TokenRepository {
    private final DataSource ds;

    public TokenRepository() {
        this.ds = AppConfig.createDataSource();
    }

    public ApiToken findLastValidToken() {
        String sql = "SELECT id, token, date_expiration FROM token WHERE date_expiration > now() ORDER BY date_expiration DESC LIMIT 1";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("id");
                    String token = rs.getString("token");
                    Timestamp ts = rs.getTimestamp("date_expiration");
                    LocalDateTime dateExpiration = (ts != null) ? ts.toLocalDateTime() : null;
                    return new ApiToken(id, token, dateExpiration);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to query token", e);
        }

        return null;
    }

    public ApiToken createAndSaveNewToken(int daysValid) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusDays(daysValid);

        String sql = "INSERT INTO token (token, date_expiration) VALUES (?, ?)";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(expiration));
            int updated = ps.executeUpdate();

            Integer id = null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys != null && keys.next()) {
                    id = keys.getInt(1);
                }
            }

            return new ApiToken(id, token, expiration);

        } catch (Exception e) {
            throw new RuntimeException("Failed to insert token", e);
        }
    }
}

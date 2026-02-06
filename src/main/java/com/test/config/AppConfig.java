package com.test.config;

import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;
import java.net.URI;

public class AppConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(".env")
            .ignoreIfMissing()
            .load();

    public static DataSource createDataSource() {
        HikariDataSource ds = new HikariDataSource();

        String envDbUrl = dotenv.get("DATABASE_URL");
        String envSchema = dotenv.get("DB_SCHEMA");

        try {
            if (envDbUrl != null) {
                if (envDbUrl.startsWith("jdbc:")) {
                    ds.setJdbcUrl(envDbUrl);
                } else {
                    URI dbUri = new URI(envDbUrl);
                    String jdbcUrl = "jdbc:postgresql://"
                            + dbUri.getHost() + ":" + dbUri.getPort()
                            + dbUri.getPath();
                    ds.setJdbcUrl(jdbcUrl);
                }

                ds.setUsername(dotenv.get("DATABASE_USERNAME"));
                ds.setPassword(dotenv.get("DATABASE_PASSWORD"));
                envSchema = (envSchema != null) ? envSchema : "public";

            } else {
                // Local fallback
                ds.setJdbcUrl("jdbc:postgresql://localhost:5432/locationvoiture");
                ds.setUsername("postgres");
                ds.setPassword("postgres");
                envSchema = "public";
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to configure datasource", e);
        }

        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(5);
        ds.setPoolName("HikariPool");

        System.out.println("Using DB schema: " + envSchema);

        return new SchemaAwareDataSource(ds, envSchema);
    }
}
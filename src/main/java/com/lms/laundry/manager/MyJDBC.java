package com.lms.laundry.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MyJDBC {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        String url = "jdbc:mysql://" + ENV.getValue("DB_HOST") +
                "/" + ENV.getValue("DB_NAME");
        config.setJdbcUrl(url);
        config.setUsername(ENV.getValue("DB_USER"));
        config.setPassword(ENV.getValue("DB_PASS"));

        // Pool Optimization
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

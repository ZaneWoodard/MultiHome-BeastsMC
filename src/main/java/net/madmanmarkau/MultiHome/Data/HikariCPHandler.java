package net.madmanmarkau.MultiHome.Data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.madmanmarkau.MultiHome.Settings;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariCPHandler {
    private final HikariDataSource ds;
    public HikariCPHandler() {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Settings.getDataStoreSettingString("sql", "url"));
        config.setUsername(Settings.getDataStoreSettingString("sql", "user"));
        config.setPassword(Settings.getDataStoreSettingString("sql", "pass"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "50");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        config.addDataSourceProperty("connectionTimeout", "7500");

        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void disable() {
        ds.close();
    }
}

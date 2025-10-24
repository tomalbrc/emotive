package com.cobblemonislands.emotive.storage.hikari;

import com.cobblemonislands.emotive.storage.DatabaseConfig;
import com.cobblemonislands.emotive.storage.EmoteStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public abstract class AbstractHikariStorage implements EmoteStorage {
    final HikariDataSource dataSource;

    public AbstractHikariStorage(Type type, DatabaseConfig cfg) {
        try {
            Class.forName(cfg.getDriverClassName(type));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + cfg.getDriverClassName(type), e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(cfg.getJdbcUrl(type));
        hikariConfig.setUsername(cfg.user);
        hikariConfig.setPassword(cfg.password);
        hikariConfig.setMaximumPoolSize(cfg.maxPoolSize);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        if (dataSource != null) dataSource.close();
    }
}

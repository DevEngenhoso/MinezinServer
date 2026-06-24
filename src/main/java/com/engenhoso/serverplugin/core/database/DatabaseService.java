package com.engenhoso.serverplugin.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseService {

    private final JavaPlugin plugin;
    private final DatabaseConfig databaseConfig;

    private HikariDataSource dataSource;
    private boolean started;

    public DatabaseService(JavaPlugin plugin, DatabaseConfig databaseConfig) {
        this.plugin = plugin;
        this.databaseConfig = databaseConfig;
    }

    public void start() {
        if (!databaseConfig.isEnabled()) {
            plugin.getLogger().warning("[Database] Banco de dados desativado no config.yml.");
            return;
        }

        if (started) {
            return;
        }

        try {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setPoolName("MinezinServerPool");
            hikariConfig.setJdbcUrl(databaseConfig.getJdbcUrl());
            hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
            hikariConfig.setUsername(databaseConfig.getUsername());
            hikariConfig.setPassword(databaseConfig.getPassword());

            hikariConfig.setMaximumPoolSize(databaseConfig.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(databaseConfig.getMinimumIdle());
            hikariConfig.setConnectionTimeout(databaseConfig.getConnectionTimeout());
            hikariConfig.setIdleTimeout(databaseConfig.getIdleTimeout());
            hikariConfig.setMaxLifetime(databaseConfig.getMaxLifetime());

            hikariConfig.addDataSourceProperty("useUnicode", "true");
            hikariConfig.addDataSourceProperty("characterEncoding", "utf8");

            this.dataSource = new HikariDataSource(hikariConfig);

            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isValid(5)) {
                    throw new SQLException("A conexão com o banco não foi validada.");
                }
            }

            this.started = true;

            plugin.getLogger().info("[Database] Conectado ao MariaDB com sucesso.");
        } catch (SQLException exception) {
            plugin.getLogger().severe("[Database] Falha ao conectar no banco de dados.");
            plugin.getLogger().severe("[Database] JDBC URL: " + databaseConfig.getJdbcUrl());
            exception.printStackTrace();

            throw new IllegalStateException("Não foi possível iniciar o banco de dados.", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("DatabaseService não está conectado.");
        }

        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("[Database] Conexão com banco encerrada.");
        }

        started = false;
    }

    public boolean isEnabled() {
        return databaseConfig.isEnabled();
    }

    public boolean isConnected() {
        return started && dataSource != null && !dataSource.isClosed();
    }
}
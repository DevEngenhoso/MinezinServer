package com.engenhoso.serverplugin.core.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseConfig {

    private final boolean enabled;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    private DatabaseConfig(
            boolean enabled,
            String host,
            int port,
            String database,
            String username,
            String password,
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout,
            long idleTimeout,
            long maxLifetime
    ) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maximumPoolSize = maximumPoolSize;
        this.minimumIdle = minimumIdle;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;
    }

    public static DatabaseConfig from(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        FileConfiguration config = plugin.getConfig();

        return new DatabaseConfig(
                config.getBoolean("database.enabled", true),
                config.getString("database.host", "127.0.0.1"),
                config.getInt("database.port", 3306),
                config.getString("database.database", "minezinserver"),
                config.getString("database.username", "minezin"),
                config.getString("database.password", ""),
                config.getInt("database.pool.maximumPoolSize", 10),
                config.getInt("database.pool.minimumIdle", 2),
                config.getLong("database.pool.connectionTimeout", 30000L),
                config.getLong("database.pool.idleTimeout", 600000L),
                config.getLong("database.pool.maxLifetime", 1800000L)
        );
    }

    public String getJdbcUrl() {
        return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }
}
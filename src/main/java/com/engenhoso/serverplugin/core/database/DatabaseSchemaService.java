package com.engenhoso.serverplugin.core.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchemaService {

    private final JavaPlugin plugin;
    private final DatabaseService databaseService;

    public DatabaseSchemaService(JavaPlugin plugin, DatabaseService databaseService) {
        this.plugin = plugin;
        this.databaseService = databaseService;
    }

    public void createSchema() {
        if (!databaseService.isEnabled()) {
            plugin.getLogger().warning("[Database] Schema não criado porque o banco está desativado.");
            return;
        }

        try (Connection connection = databaseService.getConnection();
             Statement statement = connection.createStatement()) {

            createSchemaVersionTable(statement);
            createPlayerProfilesTable(statement);
            createPlayerChapterProgressTable(statement);
            insertInitialSchemaVersion(statement);

            plugin.getLogger().info("[Database] Schema verificado/criado com sucesso.");
        } catch (SQLException exception) {
            plugin.getLogger().severe("[Database] Falha ao criar/verificar schema.");
            exception.printStackTrace();

            throw new IllegalStateException("Não foi possível criar/verificar as tabelas do banco.", exception);
        }
    }

    private void createSchemaVersionTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    id INT PRIMARY KEY,
                    version INT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """);
    }

    private void createPlayerProfilesTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_profiles (
                    uuid CHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    
                    classe VARCHAR(30) NULL,
                    prologo_concluido BOOLEAN NOT NULL DEFAULT FALSE,
                    
                    nivel INT NOT NULL DEFAULT 1,
                    xp BIGINT NOT NULL DEFAULT 0,
                    pontos_habilidade INT NOT NULL DEFAULT 0,
                    
                    party_atual VARCHAR(64) NULL,
                    instancia_atual VARCHAR(128) NULL,
                    
                    return_world VARCHAR(128) NULL,
                    return_x DOUBLE NULL,
                    return_y DOUBLE NULL,
                    return_z DOUBLE NULL,
                    return_yaw FLOAT NULL,
                    return_pitch FLOAT NULL,
                    
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    
                    INDEX idx_player_profiles_name (name),
                    INDEX idx_player_profiles_classe (classe)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """);
    }

    private void createPlayerChapterProgressTable(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_chapter_progress (
                    uuid CHAR(36) NOT NULL,
                    chapter_id VARCHAR(50) NOT NULL,
                    completed BOOLEAN NOT NULL DEFAULT FALSE,
                    completed_at TIMESTAMP NULL,
                    
                    PRIMARY KEY (uuid, chapter_id),
                    INDEX idx_chapter_progress_chapter_id (chapter_id),
                    
                    CONSTRAINT fk_chapter_progress_player
                        FOREIGN KEY (uuid)
                        REFERENCES player_profiles(uuid)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """);
    }

    private void insertInitialSchemaVersion(Statement statement) throws SQLException {
        statement.executeUpdate("""
                INSERT INTO schema_version (id, version)
                VALUES (1, 1)
                ON DUPLICATE KEY UPDATE version = version;
                """);
    }
}
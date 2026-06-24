package com.engenhoso.serverplugin.features.players;

import com.engenhoso.serverplugin.core.database.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PlayerProfileRepository {

    private final DatabaseService databaseService;

    public PlayerProfileRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public Optional<PlayerProfile> findByUuid(UUID uuid) throws SQLException {
        String sql = """
                SELECT
                    uuid,
                    name,
                    classe,
                    prologo_concluido,
                    nivel,
                    xp,
                    pontos_habilidade,
                    party_atual,
                    instancia_atual,
                    return_world,
                    return_x,
                    return_y,
                    return_z,
                    return_yaw,
                    return_pitch
                FROM player_profiles
                WHERE uuid = ?
                """;

        try (Connection connection = databaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(resultSet));
            }
        }
    }

    public PlayerProfile findOrCreate(UUID uuid, String name) throws SQLException {
        Optional<PlayerProfile> existingProfile = findByUuid(uuid);

        if (existingProfile.isPresent()) {
            PlayerProfile profile = existingProfile.get();

            if (!profile.getName().equals(name)) {
                profile.setName(name);
                save(profile);
            }

            return profile;
        }

        PlayerProfile profile = PlayerProfile.criarNovo(uuid, name);
        save(profile);

        return profile;
    }

    public void save(PlayerProfile profile) throws SQLException {
        String sql = """
                INSERT INTO player_profiles (
                    uuid,
                    name,
                    classe,
                    prologo_concluido,
                    nivel,
                    xp,
                    pontos_habilidade,
                    party_atual,
                    instancia_atual,
                    return_world,
                    return_x,
                    return_y,
                    return_z,
                    return_yaw,
                    return_pitch
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    classe = VALUES(classe),
                    prologo_concluido = VALUES(prologo_concluido),
                    nivel = VALUES(nivel),
                    xp = VALUES(xp),
                    pontos_habilidade = VALUES(pontos_habilidade),
                    party_atual = VALUES(party_atual),
                    instancia_atual = VALUES(instancia_atual),
                    return_world = VALUES(return_world),
                    return_x = VALUES(return_x),
                    return_y = VALUES(return_y),
                    return_z = VALUES(return_z),
                    return_yaw = VALUES(return_yaw),
                    return_pitch = VALUES(return_pitch)
                """;

        try (Connection connection = databaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, profile.getUuidString());
            statement.setString(2, profile.getName());
            setNullableString(statement, 3, profile.getClasse());
            statement.setBoolean(4, profile.isPrologoConcluido());
            statement.setInt(5, profile.getNivel());
            statement.setLong(6, profile.getXp());
            statement.setInt(7, profile.getPontosHabilidade());
            setNullableString(statement, 8, profile.getPartyAtual());
            setNullableString(statement, 9, profile.getInstanciaAtual());
            setNullableString(statement, 10, profile.getReturnWorld());
            setNullableDouble(statement, 11, profile.getReturnX());
            setNullableDouble(statement, 12, profile.getReturnY());
            setNullableDouble(statement, 13, profile.getReturnZ());
            setNullableFloat(statement, 14, profile.getReturnYaw());
            setNullableFloat(statement, 15, profile.getReturnPitch());

            statement.executeUpdate();
        }
    }

    public Optional<PlayerProfile> findByName(String name) throws SQLException {
        String sql = """
            SELECT
                uuid,
                name,
                classe,
                prologo_concluido,
                nivel,
                xp,
                pontos_habilidade,
                party_atual,
                instancia_atual,
                return_world,
                return_x,
                return_y,
                return_z,
                return_yaw,
                return_pitch
            FROM player_profiles
            WHERE LOWER(name) = LOWER(?)
            LIMIT 1
            """;

        try (Connection connection = databaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(resultSet));
            }
        }
    }

    private PlayerProfile map(ResultSet resultSet) throws SQLException {
        return new PlayerProfile(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("name"),
                resultSet.getString("classe"),
                resultSet.getBoolean("prologo_concluido"),
                resultSet.getInt("nivel"),
                resultSet.getLong("xp"),
                resultSet.getInt("pontos_habilidade"),
                resultSet.getString("party_atual"),
                resultSet.getString("instancia_atual"),
                resultSet.getString("return_world"),
                getNullableDouble(resultSet, "return_x"),
                getNullableDouble(resultSet, "return_y"),
                getNullableDouble(resultSet, "return_z"),
                getNullableFloat(resultSet, "return_yaw"),
                getNullableFloat(resultSet, "return_pitch")
        );
    }

    private Double getNullableDouble(ResultSet resultSet, String columnName) throws SQLException {
        double value = resultSet.getDouble(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Float getNullableFloat(ResultSet resultSet, String columnName) throws SQLException {
        float value = resultSet.getFloat(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, java.sql.Types.VARCHAR);
            return;
        }

        statement.setString(index, value);
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.DOUBLE);
            return;
        }

        statement.setDouble(index, value);
    }

    private void setNullableFloat(PreparedStatement statement, int index, Float value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.FLOAT);
            return;
        }

        statement.setFloat(index, value);
    }
}
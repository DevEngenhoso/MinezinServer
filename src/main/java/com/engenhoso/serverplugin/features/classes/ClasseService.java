package com.engenhoso.serverplugin.features.classes;

import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class ClasseService {

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;

    public ClasseService(JavaPlugin plugin, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
    }

    public boolean definirClasse(Player player, ClasseTipo classeTipo) {
        if (player == null || classeTipo == null) {
            return false;
        }

        return playerProfileService.definirClasse(player, classeTipo.name());
    }

    public Optional<ClasseTipo> obterClasse(Player player) {
        if (player == null) {
            return Optional.empty();
        }

        return obterClasse(player.getUniqueId());
    }

    public Optional<ClasseTipo> obterClasse(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        return playerProfileService.obterClasse(uuid)
                .flatMap(this::converterClasse);
    }

    public boolean possuiClasse(Player player) {
        return obterClasse(player).isPresent();
    }

    public boolean resetarClasse(Player player) {
        if (player == null) {
            return false;
        }

        return playerProfileService.resetarClasse(player);
    }

    public boolean resetarClassePorIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return false;
        }

        return playerProfileService.resetarClassePorIdentificador(identificador);
    }

    private Optional<ClasseTipo> converterClasse(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    ClasseTipo.valueOf(valor.trim().toUpperCase(Locale.ROOT))
            );
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(
                    "[Classes] Classe inválida salva no perfil: " + valor
            );

            return Optional.empty();
        }
    }
}

package com.engenhoso.serverplugin.fairy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class FairyReactionManager {

    private static final Map<FairySituation, List<String>> mensagensPorSituacao = new EnumMap<>(FairySituation.class);
    private static final Map<UUID, Map<FairySituation, Long>> cooldownsPorJogador = new HashMap<>();

    public static void carregarMensagens(File pastaPlugin) {
        File yml = new File(pastaPlugin, "fairy_messages.yml");
        if (!yml.exists()) {
            System.out.println("[Fada] Arquivo fairy_messages.yml não encontrado.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(yml);

        for (String chave : config.getKeys(false)) {
            try {
                FairySituation situacao = FairySituation.valueOf(chave);
                List<String> mensagens = config.getStringList(chave);
                if (!mensagens.isEmpty()) {
                    mensagensPorSituacao.put(situacao, mensagens);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("[Fada] Situação inválida no YAML: " + chave);
            }
        }
    }

    public static void reagir(FairySituation situacao, Player jogador) {
        if (!FairyManager.temFada(jogador)) return;
        if (!podeFalar(jogador, situacao)) return;

        List<String> mensagens = mensagensPorSituacao.getOrDefault(situacao, List.of());
        if (mensagens.isEmpty()) return;

        String msg = mensagens.get(new Random().nextInt(mensagens.size()));
        jogador.sendMessage("§d[✧ Fada] §f" + msg);
        registrarCooldown(jogador, situacao);
    }

    private static boolean podeFalar(Player jogador, FairySituation situacao) {
        long agora = System.currentTimeMillis();
        long cooldown = switch (situacao) {
            case PLAYER_TAKE_DAMAGE -> 60_000;
            case PLAYER_CRAFT_ITEM -> 120_000;
            case FADA_SENTE_SAUDADES -> 900_000;
            default -> 300_000;
        };

        Map<FairySituation, Long> cooldowns = cooldownsPorJogador.computeIfAbsent(jogador.getUniqueId(), k -> new EnumMap<>(FairySituation.class));
        long ultimo = cooldowns.getOrDefault(situacao, 0L);

        return agora - ultimo >= cooldown;
    }

    private static void registrarCooldown(Player jogador, FairySituation situacao) {
        cooldownsPorJogador.computeIfAbsent(jogador.getUniqueId(), k -> new EnumMap<>(FairySituation.class))
            .put(situacao, System.currentTimeMillis());
    }
}

package com.engenhoso.serverplugin.fairy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class FairyReactionManager {

    private static FileConfiguration mensagensConfig;

    public static void carregarMensagens(File pastaPlugin) {
        File arquivo = new File(pastaPlugin, "fairy_messages.yml");

        if (!arquivo.exists()) {
            Bukkit.getLogger().warning("[Fada] Arquivo fairy_messages.yml não encontrado. Nenhuma fala será carregada.");
            mensagensConfig = null;
            return;
        }

        mensagensConfig = YamlConfiguration.loadConfiguration(arquivo);
    }

    public static void reagir(FairySituation situacao, Player jogador) {
        if (mensagensConfig == null) return;

        String chave = situacao.name();
        List<String> falas = mensagensConfig.getStringList(chave);

        if (falas == null || falas.isEmpty()) return;

        String fala = falas.get(new Random().nextInt(falas.size()));
        jogador.sendMessage(ChatColor.LIGHT_PURPLE + "[Fada] " + ChatColor.WHITE + fala);
    }
}
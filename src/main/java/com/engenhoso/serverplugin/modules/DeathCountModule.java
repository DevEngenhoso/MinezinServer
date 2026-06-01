package com.engenhoso.serverplugin.modules;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeathCountModule {

    private final JavaPlugin plugin;

    public DeathCountModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void iniciarAtualizacaoAutomatica() {
        new BukkitRunnable() {
            @Override
            public void run() {
                atualizarScoreboards();
            }
        }.runTaskTimer(plugin, 0L, 20L * 5);
    }

    public void atualizarScoreboards() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            mostrarSidebar(jogador);
        }
    }

    public void mostrarSidebar(Player jogador) {
        Scoreboard scoreboard = jogador.getScoreboard();

        Objective objetivo = scoreboard.getObjective("death_ranking");
        if (objetivo == null) {
            objetivo = scoreboard.registerNewObjective("death_ranking", "dummy", "§c§lMORTES");
        }

        objetivo.setDisplaySlot(DisplaySlot.SIDEBAR);
        objetivo.setDisplayName("§c§lMORTES");

        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<OfflinePlayer> jogadoresOrdenados = new ArrayList<>(List.of(Bukkit.getOfflinePlayers()));

        jogadoresOrdenados.removeIf(p -> !p.hasPlayedBefore());

        jogadoresOrdenados.sort(Comparator.comparingInt(p -> -p.getStatistic(Statistic.DEATHS)));

        int max = Math.min(3, jogadoresOrdenados.size());

        for (int i = 0; i < max; i++) {
            OfflinePlayer p = jogadoresOrdenados.get(i);
            int mortes = p.getStatistic(Statistic.DEATHS);

            String nome = p.getName() != null ? p.getName() : "Desconhecido";
            String line = "§f" + (i + 1) + ". " + nome + " - §c";

            objetivo.getScore(line).setScore(mortes);
        }
    }
}
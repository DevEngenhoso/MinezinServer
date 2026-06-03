package com.engenhoso.serverplugin.features.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ScoreboardListener implements Listener {

    private final ScoreboardService scoreboardService;

    public ScoreboardListener(ScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        scoreboardService.mostrarSidebar(event.getPlayer());
    }
}
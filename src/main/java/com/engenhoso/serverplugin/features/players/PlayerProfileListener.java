package com.engenhoso.serverplugin.features.players;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerProfileListener implements Listener {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileListener(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        playerProfileService.carregarOuCriar(event.getPlayer());
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        playerProfileService.salvarERemover(event.getPlayer().getUniqueId());
    }
}
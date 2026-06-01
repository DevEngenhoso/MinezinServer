package com.engenhoso.serverplugin.listeners;

import com.engenhoso.serverplugin.MinezinServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final MinezinServer plugin;

    public PlayerListener(MinezinServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent e) {
        plugin.getDeathCountModule().mostrarSidebar(e.getPlayer());
    }
}
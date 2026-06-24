package com.engenhoso.serverplugin.features.limiar;

import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import com.engenhoso.serverplugin.shared.hologram.HologramInterfaceService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LimiarModule implements PluginModule {

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;
    private final HologramInterfaceService hologramInterfaceService;
    private final LimiarService limiarService;

    public LimiarModule(JavaPlugin plugin, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
        this.hologramInterfaceService = new HologramInterfaceService(plugin);
        this.limiarService = new LimiarService(plugin, playerProfileService, hologramInterfaceService);
    }

    @Override
    public String getName() {
        return "Limiar";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(
                new LimiarListener(plugin, limiarService, hologramInterfaceService),
                plugin
        );

        limiarService.iniciarTarefaDeMira();

        plugin.getLogger().info("[Limiar] Módulo preparado. Mundo principal: " + LimiarService.LIMIAR_WORLD_NAME);
    }

    @Override
    public void onDisable() {
        limiarService.pararTarefaDeMira();
        hologramInterfaceService.closeAll();
    }

    public PlayerProfileService getPlayerProfileService() {
        return playerProfileService;
    }

    public HologramInterfaceService getHologramInterfaceService() {
        return hologramInterfaceService;
    }

    public LimiarService getLimiarService() {
        return limiarService;
    }
}
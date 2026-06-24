package com.engenhoso.serverplugin.features.admin;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.limiar.LimiarService;
import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminModule implements PluginModule {

    private final JavaPlugin plugin;
    private final CommandRegistry commandRegistry;
    private final PlayerProfileService playerProfileService;
    private final LimiarService limiarService;

    public AdminModule(
            JavaPlugin plugin,
            CommandRegistry commandRegistry,
            PlayerProfileService playerProfileService,
            LimiarService limiarService
    ) {
        this.plugin = plugin;
        this.commandRegistry = commandRegistry;
        this.playerProfileService = playerProfileService;
        this.limiarService = limiarService;
    }

    @Override
    public String getName() {
        return "Admin";
    }

    @Override
    public void onEnable() {
        commandRegistry.register(
                "mz",
                new MinezinAdminCommand(plugin, playerProfileService, limiarService)
        );
    }

    @Override
    public void onDisable() {
        // Por enquanto não precisa encerrar nada.
    }
}
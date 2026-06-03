package com.engenhoso.serverplugin.features.scoreboard;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.dimensionlock.DimensionLockService;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardModule implements PluginModule {

    private final JavaPlugin plugin;
    private final CommandRegistry commandRegistry;
    private final ScoreboardService service;

    public ScoreboardModule(JavaPlugin plugin, CommandRegistry commandRegistry, DimensionLockService dimensionLockService) {
        this.plugin = plugin;
        this.commandRegistry = commandRegistry;
        this.service = new ScoreboardService(plugin, dimensionLockService);
    }

    @Override
    public String getName() {
        return "Scoreboard";
    }

    @Override
    public void onEnable() {
        service.iniciarAtualizacaoAutomatica();

        commandRegistry.register("scoreboard", new ScoreboardCommand(service));

        plugin.getServer().getPluginManager().registerEvents(new ScoreboardListener(service), plugin);
    }

    @Override
    public void onDisable() {
        service.pararAtualizacaoAutomatica();
    }

    public ScoreboardService getService() {
        return service;
    }
}
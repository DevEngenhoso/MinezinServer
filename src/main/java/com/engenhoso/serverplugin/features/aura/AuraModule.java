package com.engenhoso.serverplugin.features.aura;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.module.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

public class AuraModule implements PluginModule {

    private final JavaPlugin plugin;
    private final CommandRegistry commandRegistry;

    private final AuraService auraService;

    public AuraModule(JavaPlugin plugin, CommandRegistry commandRegistry) {
        this.plugin = plugin;
        this.commandRegistry = commandRegistry;

        this.auraService = new AuraService(plugin);
    }

    @Override
    public String getName() {
        return "Aura";
    }

    @Override
    public void onEnable() {
        auraService.iniciarAura();

        commandRegistry.register(
                "toggle",
                new com.engenhoso.serverplugin.features.aura.ToggleCommand(auraService)
        );
    }

    @Override
    public void onDisable() {
        auraService.pararAura();
    }
}
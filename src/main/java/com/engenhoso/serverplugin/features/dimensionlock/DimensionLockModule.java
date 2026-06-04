package com.engenhoso.serverplugin.features.dimensionlock;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.module.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

public class DimensionLockModule implements PluginModule {

    private final JavaPlugin plugin;
    private final CommandRegistry commandRegistry;
    private final DimensionLockService service;

    public DimensionLockModule(JavaPlugin plugin, CommandRegistry commandRegistry) {
        this.plugin = plugin;
        this.commandRegistry = commandRegistry;
        this.service = new DimensionLockService(plugin);
    }

    @Override
    public String getName() {
        return "DimensionLock";
    }

    @Override
    public void onEnable() {
        DimensionLockCommand command = new DimensionLockCommand(service);

        commandRegistry.register("lock", command);
        commandRegistry.register("unlock", command);

        plugin.getServer().getPluginManager().registerEvents(new DimensionLockListener(service), plugin);

        service.iniciarMonitoramentoAgendamentos();
    }

    @Override
    public void onDisable() {
        service.pararMonitoramentoAgendamentos();
    }

    public DimensionLockService getService() {
        return service;
    }
}
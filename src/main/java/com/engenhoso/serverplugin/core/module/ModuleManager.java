package com.engenhoso.serverplugin.core.module;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final JavaPlugin plugin;
    private final List<PluginModule> modules = new ArrayList<>();

    public ModuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerModule(PluginModule module) {
        modules.add(module);
    }

    public void enableModules() {
        for (PluginModule module : modules) {
            module.onEnable();
            plugin.getLogger().info("Módulo carregado: " + module.getName());
        }
    }

    public void disableModules() {
        for (int i = modules.size() - 1; i >= 0; i--) {
            PluginModule module = modules.get(i);
            module.onDisable();
            plugin.getLogger().info("Módulo encerrado: " + module.getName());
        }
    }
}
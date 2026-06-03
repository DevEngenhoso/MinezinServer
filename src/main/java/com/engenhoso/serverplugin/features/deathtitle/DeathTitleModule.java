package com.engenhoso.serverplugin.features.deathtitle;

import com.engenhoso.serverplugin.core.module.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathTitleModule implements PluginModule {

    private final JavaPlugin plugin;

    public DeathTitleModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "DeathTitle";
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new DeathTitleListener(plugin), plugin);
    }
}
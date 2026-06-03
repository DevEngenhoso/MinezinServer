package com.engenhoso.serverplugin.features.aura;

import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.aura.ultimate.BlackHoleUltimateCraftListener;
import com.engenhoso.serverplugin.features.aura.ultimate.BlackHoleUltimateItem;
import com.engenhoso.serverplugin.features.aura.ultimate.BlackHoleUltimateListener;
import com.engenhoso.serverplugin.features.aura.ultimate.BlackHoleUltimateRecipe;
import com.engenhoso.serverplugin.features.aura.ultimate.BlackHoleUltimateService;
import org.bukkit.plugin.java.JavaPlugin;

public class AuraModule implements PluginModule {

    private final JavaPlugin plugin;

    private final AuraService auraService;

    private final BlackHoleUltimateItem blackHoleUltimateItem;
    private final BlackHoleUltimateRecipe blackHoleUltimateRecipe;
    private final BlackHoleUltimateService blackHoleUltimateService;

    public AuraModule(JavaPlugin plugin) {
        this.plugin = plugin;

        this.auraService = new AuraService(plugin);

        this.blackHoleUltimateItem = new BlackHoleUltimateItem(plugin);
        this.blackHoleUltimateRecipe = new BlackHoleUltimateRecipe(plugin, blackHoleUltimateItem);
        this.blackHoleUltimateService = new BlackHoleUltimateService(plugin, blackHoleUltimateItem);
    }

    @Override
    public String getName() {
        return "Aura";
    }

    @Override
    public void onEnable() {
        auraService.iniciarAura();

        blackHoleUltimateRecipe.registrar();

        plugin.getServer().getPluginManager().registerEvents(
                new AuraListener(auraService),
                plugin
        );

        plugin.getServer().getPluginManager().registerEvents(
                new BlackHoleUltimateCraftListener(blackHoleUltimateItem),
                plugin
        );

        plugin.getServer().getPluginManager().registerEvents(
                new BlackHoleUltimateListener(blackHoleUltimateService, blackHoleUltimateItem),
                plugin
        );
    }

    @Override
    public void onDisable() {
        auraService.pararAura();

        blackHoleUltimateRecipe.remover();
        blackHoleUltimateService.parar();
    }
}
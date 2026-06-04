package com.engenhoso.serverplugin.features.aura;

import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.aura.skill.AuraSkillBarListener;
import com.engenhoso.serverplugin.features.aura.skill.AuraSkillBarService;
import org.bukkit.plugin.java.JavaPlugin;

public class AuraModule implements PluginModule {

    private final JavaPlugin plugin;

    private final AuraService auraService;
    private final AuraSkillBarService auraSkillBarService;

    public AuraModule(JavaPlugin plugin) {
        this.plugin = plugin;

        this.auraService = new AuraService(plugin);
        this.auraSkillBarService = new AuraSkillBarService(plugin);
    }

    @Override
    public String getName() {
        return "Aura";
    }

    @Override
    public void onEnable() {
        auraService.iniciarAura();
        auraSkillBarService.iniciarAtualizacaoVisual();

        plugin.getServer().getPluginManager().registerEvents(
                new AuraSkillBarListener(auraSkillBarService),
                plugin
        );
    }

    @Override
    public void onDisable() {
        auraService.pararAura();
        auraSkillBarService.parar();
    }
}

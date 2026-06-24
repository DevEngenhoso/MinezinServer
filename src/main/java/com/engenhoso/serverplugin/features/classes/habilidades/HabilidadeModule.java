package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.habilidades.execucao.HabilidadeExecutorService;
import com.engenhoso.serverplugin.features.classes.habilidades.skillbar.HabilidadeSkillBarListener;
import com.engenhoso.serverplugin.features.classes.habilidades.skillbar.HabilidadeSkillBarService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HabilidadeModule {

    private final HabilidadeRegistry registry;
    private final HabilidadeService service;
    private final HabilidadeMenu menu;
    private final HabilidadeSkillBarService skillBarService;
    private final HabilidadeExecutorService executorService;

    public HabilidadeModule(JavaPlugin plugin, ClasseResolver classeResolver) {
        this.registry = new HabilidadeRegistry();
        this.service = new HabilidadeService(plugin, registry);
        this.menu = new HabilidadeMenu(registry, service, classeResolver);

        this.executorService = new HabilidadeExecutorService(
                plugin,
                service,
                classeResolver
        );

        this.skillBarService = new HabilidadeSkillBarService(
                plugin,
                registry,
                service,
                classeResolver,
                executorService
        );

        Bukkit.getPluginManager().registerEvents(new HabilidadeMenuListener(menu), plugin);
        Bukkit.getPluginManager().registerEvents(new HabilidadeSkillBarListener(plugin, skillBarService), plugin);
        Bukkit.getPluginManager().registerEvents(executorService, plugin);
    }

    public HabilidadeRegistry getRegistry() {
        return registry;
    }

    public HabilidadeService getService() {
        return service;
    }

    public HabilidadeMenu getMenu() {
        return menu;
    }

    public HabilidadeSkillBarService getSkillBarService() {
        return skillBarService;
    }

    public void parar() {
        skillBarService.parar();
    }
}
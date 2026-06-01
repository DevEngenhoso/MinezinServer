package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.listeners.PlayerListener;
import com.engenhoso.serverplugin.modules.DeathCountModule;
import com.engenhoso.serverplugin.modules.DeathTitle;
import org.bukkit.plugin.java.JavaPlugin;

public class MinezinServer extends JavaPlugin {

    public String version = "1.4";

    private static MinezinServer instance;
    private DeathCountModule deathCountModule;

    @Override
    public void onEnable() {
        instance = this;

        // Módulo de mortes
        deathCountModule = new DeathCountModule(this);
        deathCountModule.iniciarAtualizacaoAutomatica();
        getServer().getPluginManager().registerEvents(new DeathTitle(this), this);

        // Eventos diversos
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("Plug-in Minezin Server inicializado. Versão " + version);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plug-in Minezin Server encerrado.");
    }

    public static MinezinServer getInstance() {
        return instance;
    }

    public DeathCountModule getDeathCountModule() {
        return deathCountModule;
    }
}
package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.commands.InvocarFadaCommand;
import com.engenhoso.serverplugin.commands.RenomearFadaCommand;
import com.engenhoso.serverplugin.commands.SumirFadaCommand;
import com.engenhoso.serverplugin.fairy.*;
import com.engenhoso.serverplugin.listeners.PlayerWorldReturnListener;
import com.engenhoso.serverplugin.listeners.PortalListener;
import com.engenhoso.serverplugin.modules.DeathTitle;
import com.engenhoso.serverplugin.modules.DeathCountModule;
import com.engenhoso.serverplugin.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MinezinServer extends JavaPlugin {

    public String version = "1.2";

    private static MinezinServer instance;
    private DeathCountModule deathCountModule;

    @Override
    public void onEnable() {
        instance = this;

        // Criar pasta do plugin, se necessário
        File pasta = getDataFolder();
        if (!pasta.exists()) pasta.mkdirs();

        // Criar arquivo de mensagens da fada se necessário
        FairyMessageFileCreator.criarSeNaoExistir(pasta);

        // Carregar falas da fada do YML
        FairyReactionManager.carregarMensagens(pasta);

        // Módulo de mortes
        deathCountModule = new DeathCountModule(this);
        deathCountModule.iniciarAtualizacaoAutomatica();
        getServer().getPluginManager().registerEvents(new DeathTitle(this), this);

        // Fada
        FairyManager.init(this);
        Bukkit.getPluginManager().registerEvents(new FairyListener(), this);
        new FairyFollowTask().runTaskTimer(this, 0L, 20L);
        getServer().getPluginManager().registerEvents(new FairyTalkListener(), this);
        FairyTalkManager.iniciarTarefaFalada();

        // Eventos diversos
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldReturnListener(), this);

        // Comandos
        RenomearFadaCommand cmd = new RenomearFadaCommand();
        getCommand("renomearfada").setExecutor(cmd);
        getCommand("renomearfada").setTabCompleter(cmd);
        getCommand("invocarfada").setExecutor(new InvocarFadaCommand());
        getCommand("sumirfada").setExecutor(new SumirFadaCommand());


        // Log
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

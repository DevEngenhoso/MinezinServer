package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.commands.fairycommands.InvocarFadaCommand;
import com.engenhoso.serverplugin.commands.fairycommands.RenomearFadaCommand;
import com.engenhoso.serverplugin.commands.fairycommands.SumirFadaCommand;
import com.engenhoso.serverplugin.modules.fairy.listeners.FairyListener;
import com.engenhoso.serverplugin.modules.fairy.listeners.FairySpawnListener;
import com.engenhoso.serverplugin.modules.fairy.listeners.FairyTalkListener;
import com.engenhoso.serverplugin.modules.fairy.core.*;
import com.engenhoso.serverplugin.modules.deathranking.DeathTitle;
import com.engenhoso.serverplugin.modules.deathranking.DeathCountModule;
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


        // Fada
        FairyManager.init(this);
        Bukkit.getPluginManager().registerEvents(new FairyListener(), this);
        new FairyFollowTask().runTaskTimer(this, 0L, 20L);
        FairyTalkManager.iniciarTarefaFalada();

        // Eventos
        getServer().getPluginManager().registerEvents(new FairySpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathTitle(this), this);
        getServer().getPluginManager().registerEvents(new FairyTalkListener(), this);

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

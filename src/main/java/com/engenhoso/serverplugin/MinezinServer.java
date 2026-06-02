package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.commands.ServerCommands;
import com.engenhoso.serverplugin.commands.ScoreboardCommand;
import com.engenhoso.serverplugin.listeners.DimensionLockListener;
import com.engenhoso.serverplugin.listeners.PlayerListener;
import com.engenhoso.serverplugin.modules.DimensionScoreboardModule;
import com.engenhoso.serverplugin.modules.DeathTitle;
import com.engenhoso.serverplugin.modules.DimensionLockModule;
import org.bukkit.plugin.java.JavaPlugin;

public class MinezinServer extends JavaPlugin {

    public String version = "1.4";

    private static MinezinServer instance;
    private DimensionScoreboardModule dimensionScoreboardModule;
    private DimensionLockModule dimensionLockModule;

    @Override
    public void onEnable() {
        instance = this;

        // Trava de dimensões
        dimensionLockModule = new DimensionLockModule(this);

        // Scoreboard das dimensões
        dimensionScoreboardModule = new DimensionScoreboardModule(this, dimensionLockModule);
        dimensionScoreboardModule.iniciarAtualizacaoAutomatica();

        // Eventos
        getServer().getPluginManager().registerEvents(new DeathTitle(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new DimensionLockListener(dimensionLockModule), this);

        // Comandos
        ServerCommands serverCommands = new ServerCommands(dimensionLockModule);

        getCommand("lock").setExecutor(serverCommands);
        getCommand("lock").setTabCompleter(serverCommands);

        getCommand("unlock").setExecutor(serverCommands);
        getCommand("unlock").setTabCompleter(serverCommands);

        ScoreboardCommand scoreboardCommand = new ScoreboardCommand(dimensionScoreboardModule);

        getCommand("scoreboard").setExecutor(scoreboardCommand);
        getCommand("scoreboard").setTabCompleter(scoreboardCommand);

        getLogger().info("Plug-in Minezin Server inicializado. Versão " + version);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plug-in Minezin Server encerrado.");
    }

    public static MinezinServer getInstance() {
        return instance;
    }

    public DimensionScoreboardModule getDimensionScoreboardModule() {
        return dimensionScoreboardModule;
    }

    public DimensionLockModule getDimensionLockModule() {
        return dimensionLockModule;
    }
}
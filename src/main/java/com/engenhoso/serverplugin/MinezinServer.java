package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.module.ModuleManager;
import com.engenhoso.serverplugin.features.aura.AuraModule;
import com.engenhoso.serverplugin.features.deathtitle.DeathTitleModule;
import com.engenhoso.serverplugin.features.dimensionlock.DimensionLockModule;
import com.engenhoso.serverplugin.features.dimensionlock.DimensionLockService;
import com.engenhoso.serverplugin.features.scoreboard.ScoreboardModule;
import com.engenhoso.serverplugin.features.scoreboard.ScoreboardService;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MinezinServer extends JavaPlugin {

    public String version = "1.4";

    private static MinezinServer instance;

    private ModuleManager moduleManager;

    private DimensionLockModule dimensionLockModule;
    private ScoreboardModule scoreboardModule;
    private DeathTitleModule deathTitleModule;
    private AuraModule auraModule;

    @Override
    public void onEnable() {
        instance = this;

        CommandRegistry commandRegistry = new CommandRegistry(this);

        moduleManager = new ModuleManager(this);

        dimensionLockModule = new DimensionLockModule(this, commandRegistry);
        scoreboardModule = new ScoreboardModule(this, commandRegistry, dimensionLockModule.getService());
        deathTitleModule = new DeathTitleModule(this);
        auraModule = new AuraModule(this);

        moduleManager.registerModule(dimensionLockModule);
        moduleManager.registerModule(scoreboardModule);
        moduleManager.registerModule(deathTitleModule);
        moduleManager.registerModule(auraModule);

        moduleManager.enableModules();

        imprimirLogoConsole();

        getLogger().info("Plug-in Minezin Server inicializado. Versão " + version);
    }

    private void imprimirLogoConsole() {
        String[] minezin = {
                " __  __ ___ _   _ _____ _____ ___ _   _ ",
                "|  \\/  |_ _| \\ | | ____|__  /_ _| \\ | |",
                "| |\\/| || ||  \\| |  _|   / / | ||  \\| |",
                "| |  | || || |\\  | |___ / /_ | || |\\  |",
                "|_|  |_|___|_| \\_|_____/____|___|_| \\_|"
        };

        String[] server = {
                " ____  _____ ______     _______ ____  ",
                "/ ___|| ____|  _ \\ \\   / / ____|  _ \\ ",
                "\\___ \\|  _| | |_) \\ \\ / /|  _| | |_) |",
                " ___) | |___|  _ < \\ V / | |___|  _ < ",
                "|____/|_____|_| \\_\\ \\_/  |_____|_| \\_\\"
        };

        for (String linha : minezin) {
            getServer().getConsoleSender().sendMessage(ChatColor.GREEN + linha);
        }

        getServer().getConsoleSender().sendMessage("");

        for (String linha : server) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + linha);
        }

        getServer().getConsoleSender().sendMessage(ChatColor.RESET.toString());
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableModules();
        }

        getLogger().info("Plug-in Minezin Server encerrado.");
    }

    public static MinezinServer getInstance() {
        return instance;
    }

    public DimensionLockService getDimensionLockService() {
        return dimensionLockModule.getService();
    }

    public ScoreboardService getScoreboardService() {
        return scoreboardModule.getService();
    }
}
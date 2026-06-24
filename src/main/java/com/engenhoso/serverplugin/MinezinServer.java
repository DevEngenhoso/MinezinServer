package com.engenhoso.serverplugin;

import com.engenhoso.serverplugin.core.command.CommandRegistry;
import com.engenhoso.serverplugin.core.database.DatabaseConfig;
import com.engenhoso.serverplugin.core.database.DatabaseSchemaService;
import com.engenhoso.serverplugin.core.database.DatabaseService;
import com.engenhoso.serverplugin.core.module.ModuleManager;
import com.engenhoso.serverplugin.features.admin.AdminModule;
import com.engenhoso.serverplugin.features.aura.AuraModule;
import com.engenhoso.serverplugin.features.classes.ClasseModule;
import com.engenhoso.serverplugin.features.deathtitle.DeathTitleModule;
import com.engenhoso.serverplugin.features.dimensionlock.DimensionLockModule;
import com.engenhoso.serverplugin.features.dimensionlock.DimensionLockService;
import com.engenhoso.serverplugin.features.limiar.LimiarModule;
import com.engenhoso.serverplugin.features.players.PlayerProfileModule;
import com.engenhoso.serverplugin.features.scoreboard.ScoreboardModule;
import com.engenhoso.serverplugin.features.scoreboard.ScoreboardService;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MinezinServer extends JavaPlugin {

    private static MinezinServer instance;

    public String version = "1.5-preview";

    private DatabaseService databaseService;
    private DatabaseSchemaService databaseSchemaService;

    private ModuleManager moduleManager;

    private PlayerProfileModule playerProfileModule;
    private LimiarModule limiarModule;
    private AdminModule adminModule;
    private DimensionLockModule dimensionLockModule;
    private ScoreboardModule scoreboardModule;
    private DeathTitleModule deathTitleModule;
    private AuraModule auraModule;
    private ClasseModule classeModule;

    @Override
    public void onEnable() {
        instance = this;

        DatabaseConfig databaseConfig = DatabaseConfig.from(this);

        databaseService = new DatabaseService(this, databaseConfig);
        databaseService.start();

        databaseSchemaService = new DatabaseSchemaService(this, databaseService);
        databaseSchemaService.createSchema();

        CommandRegistry commandRegistry = new CommandRegistry(this);

        moduleManager = new ModuleManager(this);

        playerProfileModule = new PlayerProfileModule(this, databaseService);

        limiarModule = new LimiarModule(
                this,
                playerProfileModule.getPlayerProfileService()
        );

        adminModule = new AdminModule(
                this,
                commandRegistry,
                playerProfileModule.getPlayerProfileService(),
                limiarModule.getLimiarService()
        );

        dimensionLockModule = new DimensionLockModule(this, commandRegistry);

        scoreboardModule = new ScoreboardModule(
                this,
                commandRegistry,
                dimensionLockModule.getService()
        );

        deathTitleModule = new DeathTitleModule(this);

        auraModule = new AuraModule(this, commandRegistry);

        classeModule = new ClasseModule(
                this,
                playerProfileModule.getPlayerProfileService()
        );

        moduleManager.registerModule(playerProfileModule);
        moduleManager.registerModule(limiarModule);
        moduleManager.registerModule(adminModule);
        moduleManager.registerModule(dimensionLockModule);
        moduleManager.registerModule(scoreboardModule);
        moduleManager.registerModule(deathTitleModule);
        moduleManager.registerModule(auraModule);
        moduleManager.registerModule(classeModule);

        moduleManager.enableModules();

        imprimirLogoConsole();

        getLogger().info("Plug-in Minezin Server inicializado. Versão " + version);
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableModules();
        }

        if (databaseService != null) {
            databaseService.shutdown();
        }

        getLogger().info("Plug-in Minezin Server encerrado.");
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

    public static MinezinServer getInstance() {
        return instance;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public PlayerProfileModule getPlayerProfileModule() {
        return playerProfileModule;
    }

    public LimiarModule getLimiarModule() {
        return limiarModule;
    }

    public AdminModule getAdminModule() {
        return adminModule;
    }

    public ClasseModule getClasseModule() {
        return classeModule;
    }

    public DimensionLockService getDimensionLockService() {
        return dimensionLockModule.getService();
    }

    public ScoreboardService getScoreboardService() {
        return scoreboardModule.getService();
    }
}
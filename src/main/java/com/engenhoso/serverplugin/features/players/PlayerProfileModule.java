package com.engenhoso.serverplugin.features.players;

import com.engenhoso.serverplugin.core.database.DatabaseService;
import com.engenhoso.serverplugin.core.module.PluginModule;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerProfileModule implements PluginModule {

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;

    public PlayerProfileModule(JavaPlugin plugin, DatabaseService databaseService) {
        this.plugin = plugin;

        PlayerProfileRepository repository = new PlayerProfileRepository(databaseService);
        this.playerProfileService = new PlayerProfileService(plugin, repository);
    }

    @Override
    public String getName() {
        return "PlayerProfile";
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerProfileListener(playerProfileService),
                plugin
        );

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playerProfileService.carregarOuCriar(player);
        }
    }

    @Override
    public void onDisable() {
        playerProfileService.salvarTodos();
    }

    public PlayerProfileService getPlayerProfileService() {
        return playerProfileService;
    }
}
package com.engenhoso.serverplugin.features.classes;

import com.engenhoso.serverplugin.core.module.PluginModule;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeCommand;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeModule;
import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ClasseModule implements PluginModule {

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;

    private ClasseService classeService;
    private HabilidadeModule habilidadeModule;

    public ClasseModule(JavaPlugin plugin, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
    }

    @Override
    public String getName() {
        return "Classes";
    }

    @Override
    public void onEnable() {
        classeService = new ClasseService(plugin, playerProfileService);

        habilidadeModule = new HabilidadeModule(
                plugin,
                player -> classeService.obterClasse(player).orElse(null)
        );

        registrarComandoHabilidades();
    }

    private void registrarComandoHabilidades() {
        PluginCommand habilidadesCommand = plugin.getCommand("habilidades");

        if (habilidadesCommand == null) {
            plugin.getLogger().warning("Comando /habilidades não encontrado no plugin.yml.");
            return;
        }

        habilidadesCommand.setExecutor(new HabilidadeCommand(habilidadeModule));
    }

    @Override
    public void onDisable() {
        if (habilidadeModule != null) {
            habilidadeModule.parar();
        }
    }

    public ClasseService getClasseService() {
        return classeService;
    }

    public HabilidadeModule getHabilidadeModule() {
        return habilidadeModule;
    }
}
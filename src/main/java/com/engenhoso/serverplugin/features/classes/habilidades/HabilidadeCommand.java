package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.limiar.LimiarService;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HabilidadeCommand implements CommandExecutor {

    private final HabilidadeModule habilidadeModule;

    public HabilidadeCommand(HabilidadeModule habilidadeModule) {
        this.habilidadeModule = habilidadeModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores.");
            return true;
        }

        if (player.getWorld().getName().equalsIgnoreCase(LimiarService.LIMIAR_WORLD_NAME)) {
            player.sendMessage("§5O Limiar não permite manipular habilidades.");
            return true;
        }

        habilidadeModule.getMenu().abrir(player);
        return true;
    }
}
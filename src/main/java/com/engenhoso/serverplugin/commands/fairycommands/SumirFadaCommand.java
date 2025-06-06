package com.engenhoso.serverplugin.commands.fairycommands;

import com.engenhoso.serverplugin.modules.fairy.core.FairyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SumirFadaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        boolean sucesso = FairyManager.removerFadaTemporariamente(player);
        if (sucesso) {
            player.sendMessage("§cSua fada desapareceu por enquanto.");
        } else {
            player.sendMessage("§eVocê não possui uma fada ativa no momento.");
        }

        return true;
    }
}

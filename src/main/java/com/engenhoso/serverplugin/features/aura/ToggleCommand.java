package com.engenhoso.serverplugin.features.aura;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToggleCommand implements CommandExecutor, TabCompleter {

    private final AuraService auraService;

    public ToggleCommand(AuraService auraService) {
        this.auraService = auraService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("aura")) {
            Boolean ativa = auraService.alternarAura(jogador);

            if (ativa == null) {
                jogador.sendMessage("§8[§5Aura§8] §cVocê não pode controlar esta aura.");
                return true;
            }

            if (ativa) {
                jogador.sendMessage("§8[§5Aura§8] §aAura ativada.");
            } else {
                jogador.sendMessage("§8[§5Aura§8] §cAura desligada totalmente.");
            }

            return true;
        }

        jogador.sendMessage("§eUso correto: §f/toggle aura");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player jogador)) {
            return Collections.emptyList();
        }

        if (!auraService.podeControlarAura(jogador)) {
            return Collections.emptyList();
        }

        if (args.length == 1 && "aura".startsWith(args[0].toLowerCase())) {
            List<String> resultado = new ArrayList<>();
            resultado.add("aura");
            return resultado;
        }

        return Collections.emptyList();
    }
}

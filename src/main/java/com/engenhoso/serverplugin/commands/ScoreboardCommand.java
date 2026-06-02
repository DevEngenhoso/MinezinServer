package com.engenhoso.serverplugin.commands;

import com.engenhoso.serverplugin.modules.DimensionScoreboardModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private final DimensionScoreboardModule dimensionScoreboardModule;

    public ScoreboardCommand(DimensionScoreboardModule dimensionScoreboardModule) {
        this.dimensionScoreboardModule = dimensionScoreboardModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            boolean visivel = dimensionScoreboardModule.alternarScoreboard(jogador);

            if (visivel) {
                jogador.sendMessage("§aScoreboard ativado.");
            } else {
                jogador.sendMessage("§cScoreboard ocultado. Use §f/scoreboard§c para mostrar novamente.");
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            dimensionScoreboardModule.setScoreboardVisivel(jogador, true);
            jogador.sendMessage("§aScoreboard ativado.");
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            dimensionScoreboardModule.setScoreboardVisivel(jogador, false);
            jogador.sendMessage("§cScoreboard ocultado. Use §f/scoreboard on§c para mostrar novamente.");
            return true;
        }

        jogador.sendMessage("§eUso correto: §f/scoreboard <on|off|toggle>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filtrar(Arrays.asList("on", "off", "toggle"), args[0]);
        }

        return Collections.emptyList();
    }

    private List<String> filtrar(List<String> opcoes, String texto) {
        List<String> resultado = new ArrayList<>();
        String comparacao = texto.toLowerCase();

        for (String opcao : opcoes) {
            if (opcao.startsWith(comparacao)) {
                resultado.add(opcao);
            }
        }

        return resultado;
    }
}
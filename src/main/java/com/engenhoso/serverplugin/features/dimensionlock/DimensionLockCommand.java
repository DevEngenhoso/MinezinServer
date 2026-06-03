package com.engenhoso.serverplugin.features.dimensionlock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DimensionLockCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSAO_ADMIN = "minezin.dimensionlock.admin";

    private final DimensionLockService dimensionLockService;

    public DimensionLockCommand(DimensionLockService dimensionLockService) {
        this.dimensionLockService = dimensionLockService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!podeUsar(sender)) {
            sender.sendMessage("§cApenas operadores podem usar este comando.");
            return true;
        }

        String nomeComando = command.getName().toLowerCase();

        if (nomeComando.equals("lock")) {
            return executarLock(sender, args);
        }

        if (nomeComando.equals("unlock")) {
            return executarUnlock(sender, args);
        }

        return false;
    }

    private boolean executarLock(CommandSender sender, String[] args) {
        if (args.length != 3) {
            enviarUsoLock(sender);
            return true;
        }

        String dimensao = args[0];
        int quantidade;

        try {
            quantidade = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantidade precisa ser um número inteiro.");
            sender.sendMessage("§7Exemplo: §f/lock nether 30 minutes");
            return true;
        }

        try {
            dimensionLockService.travar(dimensao, quantidade, args[2]);

            String nome = dimensionLockService.getNomeExibicao(dimensao);
            String tempo = dimensionLockService.formatarTempoRestante(dimensao);

            sender.sendMessage("§a" + nome + " travado por §e" + tempo + "§a.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c" + e.getMessage());
            enviarUsoLock(sender);
        }

        return true;
    }

    private boolean executarUnlock(CommandSender sender, String[] args) {
        if (args.length != 1) {
            enviarUsoUnlock(sender);
            return true;
        }

        String dimensao = args[0];

        try {
            dimensionLockService.destravar(dimensao);

            String nome = dimensionLockService.getNomeExibicao(dimensao);
            sender.sendMessage("§a" + nome + " destravado imediatamente.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c" + e.getMessage());
            enviarUsoUnlock(sender);
        }

        return true;
    }

    private void enviarUsoLock(CommandSender sender) {
        sender.sendMessage("§eUso correto:");
        sender.sendMessage("§f/lock <nether|end> <quantidade> <years|months|weeks|days|hours|minutes>");
        sender.sendMessage("§7Exemplos:");
        sender.sendMessage("§f/lock nether 7 days");
        sender.sendMessage("§f/lock end 2 weeks");
        sender.sendMessage("§f/lock nether 3 hours");
        sender.sendMessage("§f/lock end 30 minutes");
    }

    private void enviarUsoUnlock(CommandSender sender) {
        sender.sendMessage("§eUso correto:");
        sender.sendMessage("§f/unlock <nether|end>");
        sender.sendMessage("§7Exemplos:");
        sender.sendMessage("§f/unlock nether");
        sender.sendMessage("§f/unlock end");
    }

    private boolean podeUsar(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }

        return sender.isOp() && sender.hasPermission(PERMISSAO_ADMIN);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!podeUsar(sender)) {
            return Collections.emptyList();
        }

        String nomeComando = command.getName().toLowerCase();

        if (nomeComando.equals("lock")) {
            return completarLock(args);
        }

        if (nomeComando.equals("unlock")) {
            return completarUnlock(args);
        }

        return Collections.emptyList();
    }

    private List<String> completarLock(String[] args) {
        if (args.length == 1) {
            return filtrar(Arrays.asList("nether", "end"), args[0]);
        }

        if (args.length == 2) {
            return filtrar(Arrays.asList("1", "5", "10", "15", "30", "60"), args[1]);
        }

        if (args.length == 3) {
            return filtrar(Arrays.asList(
                    "years",
                    "months",
                    "weeks",
                    "days",
                    "hours",
                    "minutes",
                    "anos",
                    "meses",
                    "semanas",
                    "dias",
                    "horas",
                    "minutos"
            ), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> completarUnlock(String[] args) {
        if (args.length == 1) {
            return filtrar(Arrays.asList("nether", "end"), args[0]);
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
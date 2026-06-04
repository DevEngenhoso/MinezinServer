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
    private static final String PREFIXO = "§8[§5Dimensões§8] ";

    private static final List<String> DIMENSOES = Arrays.asList("nether", "end");
    private static final List<String> QUANTIDADES = Arrays.asList("1", "5", "10", "15", "30", "60");
    private static final List<String> UNIDADES = Arrays.asList(
            "minutes",
            "hours",
            "days",
            "weeks",
            "months",
            "years",
            "minutos",
            "horas",
            "dias",
            "semanas",
            "meses",
            "anos"
    );

    private final DimensionLockService dimensionLockService;

    public DimensionLockCommand(DimensionLockService dimensionLockService) {
        this.dimensionLockService = dimensionLockService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!podeUsar(sender)) {
            enviarErro(sender, "Você não tem permissão para usar este comando.");
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
        if (args.length == 3) {
            return executarLockPorDuracao(sender, args);
        }

        if (args.length == 5) {
            return executarLockAgendado(sender, args);
        }

        enviarUsoLock(sender);
        return true;
    }

    private boolean executarLockPorDuracao(CommandSender sender, String[] args) {
        String dimensao = args[0];
        int quantidade;

        try {
            quantidade = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            enviarErro(sender, "A quantidade precisa ser um número inteiro.");
            enviarDica(sender, "/lock nether 30 minutes");
            return true;
        }

        try {
            dimensionLockService.travar(dimensao, quantidade, args[2]);

            String nome = dimensionLockService.getNomeExibicao(dimensao);
            String tempo = dimensionLockService.formatarTempoRestante(dimensao);

            enviarSucesso(sender, nome + " travado por §e" + tempo + "§a.");
        } catch (IllegalArgumentException e) {
            enviarErro(sender, e.getMessage());
            enviarDicaLock(sender);
        }

        return true;
    }

    private boolean executarLockAgendado(CommandSender sender, String[] args) {
        String dimensao = args[0];

        try {
            dimensionLockService.travarAgendado(
                    dimensao,
                    args[1],
                    args[2],
                    args[3],
                    args[4]
            );

            String nome = dimensionLockService.getNomeExibicao(dimensao);

            enviarSucesso(sender, "Bloqueio agendado para §e" + nome + "§a.");
            sender.sendMessage("§8• §7" + formatarJanela(args));
        } catch (IllegalArgumentException e) {
            enviarErro(sender, e.getMessage());
            enviarDicaAgendamento(sender, "/lock nether 10/06/2026 20:00:00 11/06/2026 08:00:00");
        }

        return true;
    }

    private boolean executarUnlock(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return executarUnlockImediato(sender, args);
        }

        if (args.length == 5) {
            return executarUnlockAgendado(sender, args);
        }

        enviarUsoUnlock(sender);
        return true;
    }

    private boolean executarUnlockImediato(CommandSender sender, String[] args) {
        String dimensao = args[0];

        try {
            dimensionLockService.destravar(dimensao);

            String nome = dimensionLockService.getNomeExibicao(dimensao);
            enviarSucesso(sender, nome + " liberado.");
        } catch (IllegalArgumentException e) {
            enviarErro(sender, e.getMessage());
            enviarDicaUnlock(sender);
        }

        return true;
    }

    private boolean executarUnlockAgendado(CommandSender sender, String[] args) {
        String dimensao = args[0];

        try {
            dimensionLockService.destravarAgendado(
                    dimensao,
                    args[1],
                    args[2],
                    args[3],
                    args[4]
            );

            String nome = dimensionLockService.getNomeExibicao(dimensao);

            enviarSucesso(sender, "Janela de liberação agendada para §e" + nome + "§a.");
            sender.sendMessage("§8• §7" + formatarJanela(args));
        } catch (IllegalArgumentException e) {
            enviarErro(sender, e.getMessage());
            enviarDicaAgendamento(sender, "/unlock end 10/06/2026 20:00:00 11/06/2026 08:00:00");
        }

        return true;
    }

    private void enviarUsoLock(CommandSender sender) {
        sender.sendMessage("§8§m           §r §5§lLOCK §8§m           ");
        sender.sendMessage("§f/lock <dimensão> <tempo> <unidade>");
        sender.sendMessage("§8• §7Ex: §f/lock nether 7 days");
        sender.sendMessage("§f/lock <dimensão> <início> <hora> <fim> <hora>");
        sender.sendMessage("§8• §7Ex: §f/lock end 10/06/2026 20:00:00 11/06/2026 08:00:00");
    }

    private void enviarUsoUnlock(CommandSender sender) {
        sender.sendMessage("§8§m         §r §5§lUNLOCK §8§m         ");
        sender.sendMessage("§f/unlock <dimensão>");
        sender.sendMessage("§8• §7Ex: §f/unlock nether");
        sender.sendMessage("§f/unlock <dimensão> <início> <hora> <fim> <hora>");
        sender.sendMessage("§8• §7Ex: §f/unlock end 10/06/2026 20:00:00 11/06/2026 08:00:00");
    }

    private void enviarDicaLock(CommandSender sender) {
        enviarDica(sender, "/lock nether 7 days");
    }

    private void enviarDicaUnlock(CommandSender sender) {
        enviarDica(sender, "/unlock nether");
    }

    private void enviarDicaAgendamento(CommandSender sender, String exemplo) {
        enviarDica(sender, exemplo);
        sender.sendMessage("§8• §7Data: §fdd/MM/aaaa §8| §7Hora: §fHH:mm:ss");
    }

    private void enviarDica(CommandSender sender, String exemplo) {
        sender.sendMessage("§8• §7Exemplo: §f" + exemplo);
    }

    private void enviarSucesso(CommandSender sender, String mensagem) {
        sender.sendMessage(PREFIXO + "§a" + mensagem);
    }

    private void enviarErro(CommandSender sender, String mensagem) {
        sender.sendMessage(PREFIXO + "§c" + mensagem);
    }

    private String formatarJanela(String[] args) {
        return args[1] + " " + args[2] + " §8→ §7" + args[3] + " " + args[4];
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
            return filtrar(DIMENSOES, args[0]);
        }

        if (args.length == 2) {
            return filtrar(juntar(QUANTIDADES, Collections.singletonList("dd/MM/aaaa")), args[1]);
        }

        if (args.length == 3) {
            return filtrar(juntar(UNIDADES, Collections.singletonList("HH:mm:ss")), args[2]);
        }

        if (args.length == 4) {
            return filtrar(Collections.singletonList("dd/MM/aaaa"), args[3]);
        }

        if (args.length == 5) {
            return filtrar(Collections.singletonList("HH:mm:ss"), args[4]);
        }

        return Collections.emptyList();
    }

    private List<String> completarUnlock(String[] args) {
        if (args.length == 1) {
            return filtrar(DIMENSOES, args[0]);
        }

        if (args.length == 2) {
            return filtrar(Collections.singletonList("dd/MM/aaaa"), args[1]);
        }

        if (args.length == 3) {
            return filtrar(Collections.singletonList("HH:mm:ss"), args[2]);
        }

        if (args.length == 4) {
            return filtrar(Collections.singletonList("dd/MM/aaaa"), args[3]);
        }

        if (args.length == 5) {
            return filtrar(Collections.singletonList("HH:mm:ss"), args[4]);
        }

        return Collections.emptyList();
    }

    private List<String> juntar(List<String> primeira, List<String> segunda) {
        List<String> resultado = new ArrayList<>();
        resultado.addAll(primeira);
        resultado.addAll(segunda);
        return resultado;
    }

    private List<String> filtrar(List<String> opcoes, String texto) {
        List<String> resultado = new ArrayList<>();
        String comparacao = texto.toLowerCase();

        for (String opcao : opcoes) {
            if (opcao.toLowerCase().startsWith(comparacao)) {
                resultado.add(opcao);
            }
        }

        return resultado;
    }
}
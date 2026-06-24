package com.engenhoso.serverplugin.features.admin;

import com.engenhoso.serverplugin.features.limiar.LimiarService;
import com.engenhoso.serverplugin.features.limiar.LimiarTotemTipo;
import com.engenhoso.serverplugin.features.players.PlayerProfile;
import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MinezinAdminCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;
    private final LimiarService limiarService;

    public MinezinAdminCommand(
            JavaPlugin plugin,
            PlayerProfileService playerProfileService,
            LimiarService limiarService
    ) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
        this.limiarService = limiarService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minezin.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            enviarAjuda(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("profile")) {
            executarProfile(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("limiar")) {
            executarLimiar(sender, args);
            return true;
        }

        enviarAjuda(sender);
        return true;
    }

    private void executarProfile(CommandSender sender, String[] args) {
        if (args.length < 2) {
            enviarAjudaProfile(sender);
            return;
        }

        if (args.length == 2) {
            mostrarProfile(sender, args[1]);
            return;
        }

        String subcomando = args[1].toLowerCase();

        switch (subcomando) {
            case "setlevel" -> executarSetLevel(sender, args);
            case "setxp" -> executarSetXp(sender, args);
            case "addxp" -> executarAddXp(sender, args);
            case "setpoints" -> executarSetPoints(sender, args);
            case "addpoints" -> executarAddPoints(sender, args);
            case "resetclass" -> executarResetClass(sender, args);
            default -> enviarAjudaProfile(sender);
        }
    }

    private void executarLimiar(CommandSender sender, String[] args) {
        if (args.length < 2) {
            enviarAjudaLimiar(sender);
            return;
        }

        String subcomando = args[1].toLowerCase();

        switch (subcomando) {
            case "create" -> {
                limiarService.criarOuCarregarLimiar(sender);
                limiarService.criarOuCarregarSurvival(sender);
            }

            case "createlimiar" -> limiarService.criarOuCarregarLimiar(sender);

            case "createsurvival" -> limiarService.criarOuCarregarSurvival(sender);

            case "tp" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
                    return;
                }

                limiarService.teleportarAdminParaLimiar(player);
            }

            case "tpsurvival" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
                    return;
                }

                limiarService.teleportarAdminParaSurvival(player);
            }

            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
                    return;
                }

                limiarService.salvarLimiarSpawnAtual(player);
            }

            case "setsurvivalspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
                    return;
                }

                limiarService.salvarSurvivalSpawnAtual(player);
            }

            case "spawntotem" -> executarSpawnTotem(sender, args);

            case "removetotem" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
                    return;
                }

                limiarService.removerTotemMirado(player);
            }

            case "cleartotems" -> limiarService.limparTotens(sender);

            case "listtotems" -> limiarService.listarTotens(sender);

            case "info" -> limiarService.enviarInfo(sender);

            default -> enviarAjudaLimiar(sender);
        }
    }

    private void executarSpawnTotem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cEste comando só pode ser usado por um jogador.");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUso correto: §f/mz limiar spawntotem <tanque|guerreiro|atirador|mago|sacerdote|survival>");
            return;
        }

        Optional<LimiarTotemTipo> optionalTipo = LimiarTotemTipo.fromString(args[2]);

        if (optionalTipo.isEmpty()) {
            sender.sendMessage("§cTipo de totem inválido: §f" + args[2]);
            sender.sendMessage("§7Tipos: §ftanque, guerreiro, atirador, mago, sacerdote, survival");
            return;
        }

        limiarService.spawnarTotem(player, optionalTipo.get());
    }

    private void mostrarProfile(CommandSender sender, String identificador) {
        Optional<PlayerProfile> optionalProfile = playerProfileService.buscarPerfilPorIdentificador(identificador);

        if (optionalProfile.isEmpty()) {
            sender.sendMessage("§cPerfil não encontrado para: §f" + identificador);
            return;
        }

        enviarResumoPerfil(sender, optionalProfile.get());
    }

    private void executarSetLevel(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUso correto: §f/mz profile setlevel <player|uuid> <nivel>");
            return;
        }

        Optional<Integer> optionalNivel = parseInt(args[3]);

        if (optionalNivel.isEmpty() || optionalNivel.get() < 1) {
            sender.sendMessage("§cO nível precisa ser um número inteiro maior ou igual a 1.");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            profile.setNivel(optionalNivel.get());
            playerProfileService.salvar(profile);

            sender.sendMessage("§aNível de §f" + profile.getName() + " §aalterado para §f" + profile.getNivel() + "§a.");
        });
    }

    private void executarSetXp(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUso correto: §f/mz profile setxp <player|uuid> <xp>");
            return;
        }

        Optional<Long> optionalXp = parseLong(args[3]);

        if (optionalXp.isEmpty() || optionalXp.get() < 0) {
            sender.sendMessage("§cO XP precisa ser um número maior ou igual a 0.");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            profile.setXp(optionalXp.get());
            playerProfileService.salvar(profile);

            sender.sendMessage("§aXP de §f" + profile.getName() + " §aalterado para §f" + profile.getXp() + "§a.");
        });
    }

    private void executarAddXp(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUso correto: §f/mz profile addxp <player|uuid> <quantidade>");
            return;
        }

        Optional<Long> optionalXp = parseLong(args[3]);

        if (optionalXp.isEmpty()) {
            sender.sendMessage("§cA quantidade de XP precisa ser um número válido.");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            long xpAtual = profile.getXp();
            long xpNovo = xpAtual + optionalXp.get();

            if (xpNovo < 0) {
                xpNovo = 0;
            }

            profile.setXp(xpNovo);
            playerProfileService.salvar(profile);

            sender.sendMessage("§aXP de §f" + profile.getName() + " §aalterado de §f" + xpAtual + " §apara §f" + profile.getXp() + "§a.");
        });
    }

    private void executarSetPoints(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUso correto: §f/mz profile setpoints <player|uuid> <pontos>");
            return;
        }

        Optional<Integer> optionalPontos = parseInt(args[3]);

        if (optionalPontos.isEmpty() || optionalPontos.get() < 0) {
            sender.sendMessage("§cOs pontos precisam ser um número inteiro maior ou igual a 0.");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            profile.setPontosHabilidade(optionalPontos.get());
            playerProfileService.salvar(profile);

            sender.sendMessage("§aPontos de habilidade de §f" + profile.getName() + " §aalterados para §f" + profile.getPontosHabilidade() + "§a.");
        });
    }

    private void executarAddPoints(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUso correto: §f/mz profile addpoints <player|uuid> <quantidade>");
            return;
        }

        Optional<Integer> optionalPontos = parseInt(args[3]);

        if (optionalPontos.isEmpty()) {
            sender.sendMessage("§cA quantidade de pontos precisa ser um número inteiro válido.");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            int pontosAtuais = profile.getPontosHabilidade();
            int pontosNovos = pontosAtuais + optionalPontos.get();

            if (pontosNovos < 0) {
                pontosNovos = 0;
            }

            profile.setPontosHabilidade(pontosNovos);
            playerProfileService.salvar(profile);

            sender.sendMessage("§aPontos de habilidade de §f" + profile.getName() + " §aalterados de §f" + pontosAtuais + " §apara §f" + profile.getPontosHabilidade() + "§a.");
        });
    }

    private void executarResetClass(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUso correto: §f/mz profile resetclass <player|uuid>");
            return;
        }

        alterarPerfil(sender, args[2], profile -> {
            profile.setClasse(null);
            profile.setPrologoConcluido(false);
            playerProfileService.salvar(profile);

            sender.sendMessage("§aClasse de §f" + profile.getName() + " §aresetada com sucesso.");
        });
    }

    private void alterarPerfil(CommandSender sender, String identificador, ProfileAction action) {
        Optional<PlayerProfile> optionalProfile = playerProfileService.buscarPerfilPorIdentificador(identificador);

        if (optionalProfile.isEmpty()) {
            sender.sendMessage("§cPerfil não encontrado para: §f" + identificador);
            return;
        }

        action.execute(optionalProfile.get());
    }

    private void enviarResumoPerfil(CommandSender sender, PlayerProfile profile) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lPerfil do Jogador");
        sender.sendMessage("§7Nome: §f" + profile.getName());
        sender.sendMessage("§7UUID: §f" + profile.getUuid());
        sender.sendMessage("§7Classe: §f" + formatarVazio(profile.getClasse()));
        sender.sendMessage("§7Prólogo concluído: §f" + formatarBoolean(profile.isPrologoConcluido()));
        sender.sendMessage("§7Nível: §f" + profile.getNivel());
        sender.sendMessage("§7XP: §f" + profile.getXp());
        sender.sendMessage("§7Pontos de habilidade: §f" + profile.getPontosHabilidade());
        sender.sendMessage("§7Party atual: §f" + formatarVazio(profile.getPartyAtual()));
        sender.sendMessage("§7Instância atual: §f" + formatarVazio(profile.getInstanciaAtual()));
        sender.sendMessage("§7Return location: §f" + formatarReturnLocation(profile));
        sender.sendMessage("§8§m--------------------------------");
    }

    private void enviarAjuda(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lMinezinServer Admin");
        sender.sendMessage("§e/mz profile <player|uuid> §7- Mostra o perfil persistente do jogador.");
        sender.sendMessage("§e/mz profile setlevel <player|uuid> <nivel> §7- Define o nível.");
        sender.sendMessage("§e/mz profile setxp <player|uuid> <xp> §7- Define o XP.");
        sender.sendMessage("§e/mz profile addxp <player|uuid> <quantidade> §7- Soma ou remove XP.");
        sender.sendMessage("§e/mz profile setpoints <player|uuid> <pontos> §7- Define pontos de habilidade.");
        sender.sendMessage("§e/mz profile addpoints <player|uuid> <quantidade> §7- Soma ou remove pontos.");
        sender.sendMessage("§e/mz profile resetclass <player|uuid> §7- Reseta a classe.");
        sender.sendMessage("§e/mz limiar §7- Comandos do Limiar da Vigília.");
        sender.sendMessage("§8§m--------------------------------");
    }

    private void enviarAjudaProfile(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lMinezinServer Profile");
        sender.sendMessage("§e/mz profile <player|uuid>");
        sender.sendMessage("§e/mz profile setlevel <player|uuid> <nivel>");
        sender.sendMessage("§e/mz profile setxp <player|uuid> <xp>");
        sender.sendMessage("§e/mz profile addxp <player|uuid> <quantidade>");
        sender.sendMessage("§e/mz profile setpoints <player|uuid> <pontos>");
        sender.sendMessage("§e/mz profile addpoints <player|uuid> <quantidade>");
        sender.sendMessage("§e/mz profile resetclass <player|uuid>");
        sender.sendMessage("§8§m--------------------------------");
    }

    private void enviarAjudaLimiar(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lMinezinServer Limiar");
        sender.sendMessage("§e/mz limiar create §7- Cria/carrega Limiar e Survival.");
        sender.sendMessage("§e/mz limiar createlimiar §7- Cria/carrega apenas o Limiar.");
        sender.sendMessage("§e/mz limiar createsurvival §7- Cria/carrega apenas o Survival.");
        sender.sendMessage("§e/mz limiar tp §7- Teleporta você para o Limiar.");
        sender.sendMessage("§e/mz limiar tpsurvival §7- Teleporta você para o Survival.");
        sender.sendMessage("§e/mz limiar setspawn §7- Define o spawn do Limiar.");
        sender.sendMessage("§e/mz limiar setsurvivalspawn §7- Define o spawn do Survival.");
        sender.sendMessage("§e/mz limiar spawntotem <tipo> §7- Spawna um manequim de totem.");
        sender.sendMessage("§e/mz limiar removetotem §7- Remove o totem que você está mirando.");
        sender.sendMessage("§e/mz limiar cleartotems §7- Remove todos os totens.");
        sender.sendMessage("§e/mz limiar listtotems §7- Lista todos os totens.");
        sender.sendMessage("§e/mz limiar info §7- Mostra informações do Limiar.");
        sender.sendMessage("§8§m--------------------------------");
    }

    private String formatarVazio(String valor) {
        if (valor == null || valor.isBlank()) {
            return "§8nenhum";
        }

        return valor;
    }

    private String formatarBoolean(boolean valor) {
        return valor ? "§aSim" : "§cNão";
    }

    private String formatarReturnLocation(PlayerProfile profile) {
        if (profile.getReturnWorld() == null || profile.getReturnWorld().isBlank()) {
            return "§8nenhuma";
        }

        return profile.getReturnWorld()
                + ", "
                + arredondar(profile.getReturnX())
                + ", "
                + arredondar(profile.getReturnY())
                + ", "
                + arredondar(profile.getReturnZ());
    }

    private String arredondar(Double valor) {
        if (valor == null) {
            return "null";
        }

        return String.format("%.2f", valor);
    }

    private Optional<Integer> parseInt(String valor) {
        try {
            return Optional.of(Integer.parseInt(valor));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseLong(String valor) {
        try {
            return Optional.of(Long.parseLong(valor));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("minezin.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filtrar(List.of("profile", "limiar"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("limiar")) {
            return filtrar(List.of(
                    "create",
                    "createlimiar",
                    "createsurvival",
                    "tp",
                    "tpsurvival",
                    "setspawn",
                    "setsurvivalspawn",
                    "spawntotem",
                    "removetotem",
                    "cleartotems",
                    "listtotems",
                    "info"
            ), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("limiar") && args[1].equalsIgnoreCase("spawntotem")) {
            return filtrar(List.of(
                    "tanque",
                    "guerreiro",
                    "atirador",
                    "arqueiro",
                    "mago",
                    "sacerdote",
                    "survival"
            ), args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("profile")) {
            List<String> opcoes = new ArrayList<>();

            opcoes.add("setlevel");
            opcoes.add("setxp");
            opcoes.add("addxp");
            opcoes.add("setpoints");
            opcoes.add("addpoints");
            opcoes.add("resetclass");

            for (Player player : Bukkit.getOnlinePlayers()) {
                opcoes.add(player.getName());
            }

            return filtrar(opcoes, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("profile")) {
            String subcomando = args[1].toLowerCase();

            if (List.of("setlevel", "setxp", "addxp", "setpoints", "addpoints", "resetclass").contains(subcomando)) {
                List<String> nomes = new ArrayList<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    nomes.add(player.getName());
                }

                return filtrar(nomes, args[2]);
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("profile")) {
            String subcomando = args[1].toLowerCase();

            return switch (subcomando) {
                case "setlevel" -> filtrar(List.of("1", "5", "10", "25", "50"), args[3]);
                case "setxp", "addxp" -> filtrar(List.of("0", "100", "500", "1000", "-100"), args[3]);
                case "setpoints", "addpoints" -> filtrar(List.of("0", "1", "5", "10", "-1"), args[3]);
                default -> Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

    private List<String> filtrar(List<String> opcoes, String argumento) {
        String argumentoLower = argumento.toLowerCase();

        List<String> resultado = new ArrayList<>();

        for (String opcao : opcoes) {
            if (opcao.toLowerCase().startsWith(argumentoLower)) {
                resultado.add(opcao);
            }
        }

        return resultado;
    }

    @FunctionalInterface
    private interface ProfileAction {
        void execute(PlayerProfile profile);
    }
}
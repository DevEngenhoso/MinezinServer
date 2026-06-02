package com.engenhoso.serverplugin.modules;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

public class DimensionLockModule {

    private final JavaPlugin plugin;
    private final File arquivo;
    private final FileConfiguration config;

    private long netherUnlockAt;
    private long endUnlockAt;

    public DimensionLockModule(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.arquivo = new File(plugin.getDataFolder(), "dimension_locks.yml");
        this.config = YamlConfiguration.loadConfiguration(arquivo);

        carregar();
    }

    private void carregar() {
        this.netherUnlockAt = config.getLong("nether.unlock-at", 0L);
        this.endUnlockAt = config.getLong("end.unlock-at", 0L);
    }

    private void salvar() {
        config.set("nether.unlock-at", netherUnlockAt);
        config.set("end.unlock-at", endUnlockAt);

        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar dimension_locks.yml: " + e.getMessage());
        }
    }

    public void travar(String dimensao, int quantidade, String unidade) {
        String dim = normalizarDimensao(dimensao);
        long unlockAt = calcularUnlockAt(quantidade, unidade);

        setUnlockAt(dim, unlockAt);
        salvar();

        teleportarJogadoresDaDimensao(getEnvironment(dim));
    }

    public void destravar(String dimensao) {
        String dim = normalizarDimensao(dimensao);

        setUnlockAt(dim, 0L);
        salvar();
    }

    public boolean estaTravado(World.Environment environment) {
        String dim = getDimension(environment);

        if (dim == null) {
            return false;
        }

        return estaTravado(dim);
    }

    public boolean estaTravado(String dimensao) {
        String dim = normalizarDimensao(dimensao);
        long unlockAt = getUnlockAt(dim);

        if (unlockAt <= 0L) {
            return false;
        }

        if (System.currentTimeMillis() >= unlockAt) {
            setUnlockAt(dim, 0L);
            salvar();
            return false;
        }

        return true;
    }

    public String formatarTempoRestante(String dimensao) {
        String dim = normalizarDimensao(dimensao);

        if (!estaTravado(dim)) {
            return "Liberado";
        }

        long restante = Math.max(0L, getUnlockAt(dim) - System.currentTimeMillis());

        long segundos = restante / 1000;
        long dias = segundos / 86400;
        segundos %= 86400;

        long horas = segundos / 3600;
        segundos %= 3600;

        long minutos = segundos / 60;
        segundos %= 60;

        if (dias > 0) {
            return dias + "d " + horas + "h";
        }

        if (horas > 0) {
            return horas + "h " + minutos + "m";
        }

        if (minutos > 0) {
            return minutos + "m " + segundos + "s";
        }

        return segundos + "s";
    }

    public String getMensagemBloqueio(World.Environment environment) {
        String dim = getDimension(environment);

        if (dim == null) {
            return "§cEsta dimensão está bloqueada.";
        }

        return "§cO " + getNomeExibicao(dim) + " está bloqueado por mais §e" + formatarTempoRestante(dim) + "§c.";
    }

    public String getNomeExibicao(String dimensao) {
        String dim = normalizarDimensao(dimensao);

        if (dim.equals("nether")) {
            return "Nether";
        }

        return "End";
    }

    public void teleportarParaOverworld(Player jogador) {
        World overworld = getOverworld();

        if (overworld == null) {
            jogador.sendMessage("§cNão foi possível encontrar o Overworld.");
            return;
        }

        jogador.teleport(overworld.getSpawnLocation());
    }

    private void teleportarJogadoresDaDimensao(World.Environment environment) {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            if (jogador.getWorld().getEnvironment() == environment) {
                teleportarParaOverworld(jogador);
                jogador.sendMessage("§cEsta dimensão foi bloqueada. Você voltou para o Overworld.");
            }
        }
    }

    private World getOverworld() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                return world;
            }
        }

        return null;
    }

    private long calcularUnlockAt(int quantidade, String unidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade precisa ser maior que zero.");
        }

        ZonedDateTime data = ZonedDateTime.now();
        String unit = unidade.toLowerCase();

        switch (unit) {
            case "year":
            case "years":
            case "ano":
            case "anos":
                data = data.plusYears(quantidade);
                break;

            case "month":
            case "months":
            case "mes":
            case "meses":
                data = data.plusMonths(quantidade);
                break;

            case "week":
            case "weeks":
            case "semana":
            case "semanas":
                data = data.plusWeeks(quantidade);
                break;

            case "day":
            case "days":
            case "dia":
            case "dias":
                data = data.plusDays(quantidade);
                break;

            case "hour":
            case "hours":
            case "hora":
            case "horas":
                data = data.plusHours(quantidade);
                break;

            case "minute":
            case "minutes":
            case "minuto":
            case "minutos":
                data = data.plusMinutes(quantidade);
                break;

            default:
                throw new IllegalArgumentException("Unidade inválida. Use years, months, weeks, days, hours ou minutes.");
        }

        return data.toInstant().toEpochMilli();
    }

    private String normalizarDimensao(String dimensao) {
        String dim = dimensao.toLowerCase();

        if (dim.equals("nether")) {
            return "nether";
        }

        if (dim.equals("end")) {
            return "end";
        }

        throw new IllegalArgumentException("Dimensão inválida. Use nether ou end.");
    }

    private World.Environment getEnvironment(String dimensao) {
        if (dimensao.equals("nether")) {
            return World.Environment.NETHER;
        }

        return World.Environment.THE_END;
    }

    private String getDimension(World.Environment environment) {
        if (environment == World.Environment.NETHER) {
            return "nether";
        }

        if (environment == World.Environment.THE_END) {
            return "end";
        }

        return null;
    }

    private long getUnlockAt(String dimensao) {
        if (dimensao.equals("nether")) {
            return netherUnlockAt;
        }

        return endUnlockAt;
    }

    private void setUnlockAt(String dimensao, long unlockAt) {
        if (dimensao.equals("nether")) {
            netherUnlockAt = unlockAt;
            return;
        }

        endUnlockAt = unlockAt;
    }
}
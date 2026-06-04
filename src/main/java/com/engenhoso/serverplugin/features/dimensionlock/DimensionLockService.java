package com.engenhoso.serverplugin.features.dimensionlock;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DimensionLockService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private final JavaPlugin plugin;
    private final File arquivo;
    private final FileConfiguration config;

    private long netherUnlockAt;
    private long endUnlockAt;

    private long netherLockStartAt;
    private long netherLockEndAt;
    private long endLockStartAt;
    private long endLockEndAt;

    private long netherAvailableStartAt;
    private long netherAvailableEndAt;
    private long endAvailableStartAt;
    private long endAvailableEndAt;

    private BukkitTask tarefaMonitoramento;

    public DimensionLockService(JavaPlugin plugin) {
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

        this.netherLockStartAt = config.getLong("nether.lock-window.start-at", 0L);
        this.netherLockEndAt = config.getLong("nether.lock-window.end-at", 0L);
        this.endLockStartAt = config.getLong("end.lock-window.start-at", 0L);
        this.endLockEndAt = config.getLong("end.lock-window.end-at", 0L);

        this.netherAvailableStartAt = config.getLong("nether.available-window.start-at", 0L);
        this.netherAvailableEndAt = config.getLong("nether.available-window.end-at", 0L);
        this.endAvailableStartAt = config.getLong("end.available-window.start-at", 0L);
        this.endAvailableEndAt = config.getLong("end.available-window.end-at", 0L);
    }

    private void salvar() {
        config.set("nether.unlock-at", netherUnlockAt);
        config.set("end.unlock-at", endUnlockAt);

        config.set("nether.lock-window.start-at", netherLockStartAt);
        config.set("nether.lock-window.end-at", netherLockEndAt);
        config.set("end.lock-window.start-at", endLockStartAt);
        config.set("end.lock-window.end-at", endLockEndAt);

        config.set("nether.available-window.start-at", netherAvailableStartAt);
        config.set("nether.available-window.end-at", netherAvailableEndAt);
        config.set("end.available-window.start-at", endAvailableStartAt);
        config.set("end.available-window.end-at", endAvailableEndAt);

        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar dimension_locks.yml: " + e.getMessage());
        }
    }

    public void iniciarMonitoramentoAgendamentos() {
        if (tarefaMonitoramento != null) {
            return;
        }

        tarefaMonitoramento = new BukkitRunnable() {
            @Override
            public void run() {
                verificarJogadoresEmDimensoesBloqueadas();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void pararMonitoramentoAgendamentos() {
        if (tarefaMonitoramento == null) {
            return;
        }

        tarefaMonitoramento.cancel();
        tarefaMonitoramento = null;
    }

    public void travar(String dimensao, int quantidade, String unidade) {
        String dim = normalizarDimensao(dimensao);
        long unlockAt = calcularUnlockAt(quantidade, unidade);

        limparAgendamentos(dim);
        setUnlockAt(dim, unlockAt);
        salvar();

        teleportarJogadoresDaDimensao(getEnvironment(dim));
    }

    public void travarAgendado(
            String dimensao,
            String dataInicio,
            String horaInicio,
            String dataFim,
            String horaFim
    ) {
        String dim = normalizarDimensao(dimensao);
        long inicioAt = converterParaMillis(dataInicio, horaInicio);
        long fimAt = converterParaMillis(dataFim, horaFim);

        validarJanela(inicioAt, fimAt);

        limparAgendamentos(dim);
        setLockWindow(dim, inicioAt, fimAt);
        salvar();

        if (estaTravado(dim)) {
            teleportarJogadoresDaDimensao(getEnvironment(dim));
        }
    }

    public void destravar(String dimensao) {
        String dim = normalizarDimensao(dimensao);

        setUnlockAt(dim, 0L);
        limparAgendamentos(dim);
        salvar();
    }

    public void destravarAgendado(
            String dimensao,
            String dataInicio,
            String horaInicio,
            String dataFim,
            String horaFim
    ) {
        String dim = normalizarDimensao(dimensao);
        long inicioAt = converterParaMillis(dataInicio, horaInicio);
        long fimAt = converterParaMillis(dataFim, horaFim);

        validarJanela(inicioAt, fimAt);

        limparAgendamentos(dim);
        setAvailableWindow(dim, inicioAt, fimAt);
        salvar();

        if (estaTravado(dim)) {
            teleportarJogadoresDaDimensao(getEnvironment(dim));
        }
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
        long agora = System.currentTimeMillis();

        if (temAvailableWindow(dim)) {
            long inicio = getAvailableStartAt(dim);
            long fim = getAvailableEndAt(dim);

            return agora < inicio || agora > fim;
        }

        if (temLockWindow(dim)) {
            long inicio = getLockStartAt(dim);
            long fim = getLockEndAt(dim);

            if (agora < inicio) {
                return false;
            }

            if (agora <= fim) {
                return true;
            }

            limparLockWindow(dim);
            salvar();
            return false;
        }

        long unlockAt = getUnlockAt(dim);

        if (unlockAt <= 0L) {
            return false;
        }

        if (agora >= unlockAt) {
            setUnlockAt(dim, 0L);
            salvar();
            return false;
        }

        return true;
    }

    public String formatarTempoRestante(String dimensao) {
        String dim = normalizarDimensao(dimensao);
        long agora = System.currentTimeMillis();

        if (temAvailableWindow(dim)) {
            long inicio = getAvailableStartAt(dim);
            long fim = getAvailableEndAt(dim);

            if (agora < inicio) {
                return "Abre em " + formatarDuracao(inicio - agora);
            }

            if (agora <= fim) {
                return "Liberado";
            }

            return "Bloqueado";
        }

        if (temLockWindow(dim)) {
            long inicio = getLockStartAt(dim);
            long fim = getLockEndAt(dim);

            if (agora < inicio) {
                return "Liberado";
            }

            if (agora <= fim) {
                return formatarDuracao(fim - agora);
            }

            limparLockWindow(dim);
            salvar();
            return "Liberado";
        }

        if (!estaTravado(dim)) {
            return "Liberado";
        }

        long restante = Math.max(0L, getUnlockAt(dim) - agora);
        return formatarDuracao(restante);
    }

    public String getMensagemBloqueio(World.Environment environment) {
        String dim = getDimension(environment);

        if (dim == null) {
            return "§cEsta dimensão está bloqueada.";
        }

        if (temAvailableWindow(dim)) {
            long agora = System.currentTimeMillis();
            long inicio = getAvailableStartAt(dim);
            long fim = getAvailableEndAt(dim);

            if (agora < inicio) {
                return "§cO " + getNomeExibicao(dim) + " ficará disponível em §e"
                        + formatarDuracao(inicio - agora) + "§c.";
            }

            if (agora > fim) {
                return "§cO " + getNomeExibicao(dim)
                        + " não está disponível fora do período agendado.";
            }
        }

        return "§cO " + getNomeExibicao(dim) + " está bloqueado por mais §e"
                + formatarTempoRestante(dim) + "§c.";
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

    private void verificarJogadoresEmDimensoesBloqueadas() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            World.Environment ambienteAtual = jogador.getWorld().getEnvironment();

            if (!estaTravado(ambienteAtual)) {
                continue;
            }

            teleportarParaOverworld(jogador);
            jogador.sendMessage(getMensagemBloqueio(ambienteAtual));
        }
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

    private long converterParaMillis(String dataTexto, String horaTexto) {
        try {
            LocalDate data = LocalDate.parse(dataTexto, FORMATO_DATA);
            LocalTime hora = LocalTime.parse(horaTexto, FORMATO_HORA);
            LocalDateTime dataHora = LocalDateTime.of(data, hora);

            return dataHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data ou hora inválida. Use data no formato dd/MM/aaaa e hora no formato HH:mm:ss."
            );
        }
    }

    private void validarJanela(long inicioAt, long fimAt) {
        if (inicioAt >= fimAt) {
            throw new IllegalArgumentException("A data/hora inicial precisa ser menor que a data/hora final.");
        }

        if (fimAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("A data/hora final precisa estar no futuro.");
        }
    }

    private String formatarDuracao(long millis) {
        long segundos = Math.max(0L, millis / 1000);
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

    private boolean temLockWindow(String dimensao) {
        return getLockStartAt(dimensao) > 0L && getLockEndAt(dimensao) > 0L;
    }

    private boolean temAvailableWindow(String dimensao) {
        return getAvailableStartAt(dimensao) > 0L && getAvailableEndAt(dimensao) > 0L;
    }

    private long getLockStartAt(String dimensao) {
        if (dimensao.equals("nether")) {
            return netherLockStartAt;
        }

        return endLockStartAt;
    }

    private long getLockEndAt(String dimensao) {
        if (dimensao.equals("nether")) {
            return netherLockEndAt;
        }

        return endLockEndAt;
    }

    private void setLockWindow(String dimensao, long inicioAt, long fimAt) {
        if (dimensao.equals("nether")) {
            netherLockStartAt = inicioAt;
            netherLockEndAt = fimAt;
            return;
        }

        endLockStartAt = inicioAt;
        endLockEndAt = fimAt;
    }

    private void limparLockWindow(String dimensao) {
        setLockWindow(dimensao, 0L, 0L);
    }

    private long getAvailableStartAt(String dimensao) {
        if (dimensao.equals("nether")) {
            return netherAvailableStartAt;
        }

        return endAvailableStartAt;
    }

    private long getAvailableEndAt(String dimensao) {
        if (dimensao.equals("nether")) {
            return netherAvailableEndAt;
        }

        return endAvailableEndAt;
    }

    private void setAvailableWindow(String dimensao, long inicioAt, long fimAt) {
        if (dimensao.equals("nether")) {
            netherAvailableStartAt = inicioAt;
            netherAvailableEndAt = fimAt;
            return;
        }

        endAvailableStartAt = inicioAt;
        endAvailableEndAt = fimAt;
    }

    private void limparAvailableWindow(String dimensao) {
        setAvailableWindow(dimensao, 0L, 0L);
    }

    private void limparAgendamentos(String dimensao) {
        limparLockWindow(dimensao);
        limparAvailableWindow(dimensao);
    }
}
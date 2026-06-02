package com.engenhoso.serverplugin.modules;

import com.engenhoso.serverplugin.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class DimensionScoreboardModule {

    private static final String OBJETIVO_ID = "minezin_sidebar";
    private static final String TITULO = "MINEZIN SERVER";

    private static final ChatColor[] CORES_ARCO_IRIS = {
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.BLUE,
            ChatColor.LIGHT_PURPLE
    };

    private final JavaPlugin plugin;
    private final DimensionLockModule dimensionLockModule;

    private final File arquivo;
    private final FileConfiguration config;

    private int tituloAnimacaoOffset = 0;

    public DimensionScoreboardModule(JavaPlugin plugin, DimensionLockModule dimensionLockModule) {
        this.plugin = plugin;
        this.dimensionLockModule = dimensionLockModule;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.arquivo = new File(plugin.getDataFolder(), "scoreboard.yml");
        this.config = YamlConfiguration.loadConfiguration(arquivo);
    }

    public void iniciarAtualizacaoAutomatica() {
        new BukkitRunnable() {
            @Override
            public void run() {
                atualizarScoreboards();
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    public void atualizarScoreboards() {
        tituloAnimacaoOffset++;

        for (Player jogador : Bukkit.getOnlinePlayers()) {
            mostrarSidebar(jogador);
        }
    }

    public void mostrarSidebar(Player jogador) {
        if (!isScoreboardVisivel(jogador)) {
            esconderScoreboard(jogador);
            return;
        }

        Scoreboard scoreboard = jogador.getScoreboard();

        Objective objetivo = scoreboard.getObjective(OBJETIVO_ID);
        if (objetivo == null) {
            objetivo = scoreboard.registerNewObjective(
                    OBJETIVO_ID,
                    "dummy",
                    getTituloArcoIrisAnimado()
            );
        }

        objetivo.setDisplaySlot(DisplaySlot.SIDEBAR);
        objetivo.setDisplayName(getTituloArcoIrisAnimado());

        // Paper 1.20.3+ remove os números do lado direito.
        ScoreboardUtil.esconderNumerosDaDireita(objetivo);

        for (String entry : new HashSet<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }

        int linha = 4;

        adicionarLinha(objetivo, ChatColor.BLACK.toString(), linha--);
        adicionarLinha(objetivo, ChatColor.WHITE.toString() + ChatColor.BOLD + "Dimensões", linha--);
        adicionarLinha(objetivo, formatarLinhaDimensao(ChatColor.RED + " Nether", "nether"), linha--);
        adicionarLinha(objetivo, formatarLinhaDimensao(ChatColor.DARK_PURPLE + " End", "end"), linha--);
    }

    public boolean alternarScoreboard(Player jogador) {
        boolean novoValor = !isScoreboardVisivel(jogador);
        setScoreboardVisivel(jogador, novoValor);
        return novoValor;
    }

    public void setScoreboardVisivel(Player jogador, boolean visivel) {
        config.set("players." + jogador.getUniqueId() + ".visible", visivel);
        salvar();

        if (visivel) {
            mostrarSidebar(jogador);
        } else {
            esconderScoreboard(jogador);
        }
    }

    public boolean isScoreboardVisivel(Player jogador) {
        return config.getBoolean("players." + jogador.getUniqueId() + ".visible", true);
    }

    private void esconderScoreboard(Player jogador) {
        Scoreboard scoreboard = jogador.getScoreboard();
        Objective objetivo = scoreboard.getObjective(OBJETIVO_ID);

        if (objetivo != null) {
            objetivo.setDisplaySlot(null);
        }
    }

    private void salvar() {
        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar scoreboard.yml: " + e.getMessage());
        }
    }

    private String getTituloArcoIrisAnimado() {
        StringBuilder titulo = new StringBuilder();

        for (int i = 0; i < TITULO.length(); i++) {
            char letra = TITULO.charAt(i);

            if (letra == ' ') {
                titulo.append(" ");
                continue;
            }

            int indiceCor = Math.floorMod(i - tituloAnimacaoOffset, CORES_ARCO_IRIS.length);

            titulo.append(CORES_ARCO_IRIS[indiceCor]);
            titulo.append(ChatColor.BOLD);
            titulo.append(letra);
        }

        return titulo.toString();
    }

    private String formatarLinhaDimensao(String nome, String dimensao) {
        String tempo = dimensionLockModule.formatarTempoRestante(dimensao);

        if (tempo.equalsIgnoreCase("Liberado")) {
            return nome + ChatColor.WHITE + ": " + ChatColor.GREEN + "Liberado";
        }

        return nome + ChatColor.WHITE + ": " + ChatColor.DARK_RED + tempo;
    }

    private void adicionarLinha(Objective objetivo, String texto, int scoreValor) {
        Score score = objetivo.getScore(texto);

        score.setScore(scoreValor);

        // Reforço por linha.
        ScoreboardUtil.esconderNumeroDaLinha(score);
    }
}
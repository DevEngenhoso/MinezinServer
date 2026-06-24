package com.engenhoso.serverplugin.features.aura;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AuraService {

    private static final String NICK_DONO_DA_AURA = "Engenhoso";
    private static final String NOME_ESPADA_EXPANSAO = "Skeleton Piercer";

    private static final long INTERVALO_TICKS = 3L;

    private static final double DISTANCIA_DAS_COSTAS = 0.25;
    private static final double VELOCIDADE_PARTICULA_NORMAL = 0.014;

    private static final int PONTOS_AURA_NORMAL = 3;

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private final File arquivo;
    private final FileConfiguration config;

    private BukkitTask tarefaAura;
    private double angulo = 0.0;
    private double intensidadeExpansao = 0.0;

    public AuraService(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.arquivo = new File(plugin.getDataFolder(), "aura.yml");
        this.config = YamlConfiguration.loadConfiguration(arquivo);
    }

    public void iniciarAura() {
        if (tarefaAura != null) {
            return;
        }

        tarefaAura = new BukkitRunnable() {
            @Override
            public void run() {
                Player jogador = Bukkit.getPlayerExact(NICK_DONO_DA_AURA);

                if (jogador == null || !jogador.isOnline()) {
                    return;
                }

                if (!isAuraAtiva(jogador)) {
                    intensidadeExpansao = 0.0;
                    return;
                }

                atualizarIntensidade(jogador);

                if (intensidadeExpansao > 0.03) {
                    emitirAuraExpandida(jogador);
                } else {
                    emitirAuraNormal(jogador);
                }
            }
        }.runTaskTimer(plugin, 20L, INTERVALO_TICKS);
    }

    public void pararAura() {
        if (tarefaAura == null) {
            return;
        }

        tarefaAura.cancel();
        tarefaAura = null;
    }

    private void atualizarIntensidade(Player jogador) {
        if (estaCanalizandoExpansaoDaAura(jogador)) {
            intensidadeExpansao = Math.min(1.0, intensidadeExpansao + 0.075);
            return;
        }

        intensidadeExpansao = Math.max(0.0, intensidadeExpansao - 0.055);
    }

    public boolean podeControlarAura(Player jogador) {
        return jogador != null && jogador.getName().equalsIgnoreCase(NICK_DONO_DA_AURA);
    }

    public boolean isAuraAtiva(Player jogador) {
        if (!podeControlarAura(jogador)) {
            return false;
        }

        return config.getBoolean("players." + jogador.getUniqueId() + ".ativa", true);
    }

    public Boolean alternarAura(Player jogador) {
        if (!podeControlarAura(jogador)) {
            return null;
        }

        boolean novoValor = !isAuraAtiva(jogador);

        config.set("players." + jogador.getUniqueId() + ".nome", jogador.getName());
        config.set("players." + jogador.getUniqueId() + ".ativa", novoValor);
        salvar();

        if (!novoValor) {
            intensidadeExpansao = 0.0;
        }

        return novoValor;
    }

    private void salvar() {
        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar aura.yml: " + e.getMessage());
        }
    }

    private boolean estaCanalizandoExpansaoDaAura(Player jogador) {
        return jogador.isSneaking() && estaSegurandoSkeletonPiercer(jogador);
    }

    private boolean estaSegurandoSkeletonPiercer(Player jogador) {
        ItemStack item = jogador.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        if (!item.getType().name().endsWith("_SWORD")) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String nome = ChatColor.stripColor(meta.getDisplayName());

        return nome != null && nome.equalsIgnoreCase(NOME_ESPADA_EXPANSAO);
    }

    private void emitirAuraNormal(Player jogador) {
        World mundo = jogador.getWorld();
        Location base = jogador.getLocation();

        Vector direcao = base.getDirection();
        direcao.setY(0);

        if (direcao.lengthSquared() == 0) {
            return;
        }

        direcao.normalize();

        Vector costas = direcao.clone().multiply(-1);
        Vector lateral = new Vector(-direcao.getZ(), 0, direcao.getX());

        if (lateral.lengthSquared() == 0) {
            lateral = new Vector(1, 0, 0);
        }

        lateral.normalize();

        angulo += Math.PI / 34;

        for (int i = 0; i < PONTOS_AURA_NORMAL; i++) {
            double altura = 0.35 + (i * 0.42) + (Math.sin(angulo + i) * 0.045);
            double oscilacaoLateral = Math.sin(angulo + (i * 1.7)) * 0.12;

            Location localParticula = base.clone()
                    .add(0, altura, 0)
                    .add(costas.clone().multiply(DISTANCIA_DAS_COSTAS))
                    .add(lateral.clone().multiply(oscilacaoLateral));

            mundo.spawnParticle(
                    Particle.SMOKE,
                    localParticula,
                    0,
                    costas.getX(),
                    0.018,
                    costas.getZ(),
                    VELOCIDADE_PARTICULA_NORMAL
            );
        }

        if (random.nextDouble() < 0.35) {
            Location sombraBaixa = base.clone()
                    .add(costas.clone().multiply(0.18))
                    .add(
                            (random.nextDouble() - 0.5) * 0.28,
                            0.12 + random.nextDouble() * 0.22,
                            (random.nextDouble() - 0.5) * 0.28
                    );

            mundo.spawnParticle(
                    Particle.SMOKE,
                    sombraBaixa,
                    2,
                    0.04,
                    0.05,
                    0.04,
                    0.004
            );
        }
    }

    private void emitirAuraExpandida(Player jogador) {
        World mundo = jogador.getWorld();
        Location base = jogador.getLocation();

        angulo += Math.PI / 18;

        double raioBase = 0.45 + (intensidadeExpansao * 1.45);
        double alturaMaxima = 1.35 + (intensidadeExpansao * 1.15);

        emitirAneisDePressao(mundo, base, raioBase);
        emitirColunasDeFumaca(mundo, base, alturaMaxima);
        emitirFragmentosOrbitais(mundo, base, raioBase, alturaMaxima);
        emitirPulsoEscuro(mundo, base, raioBase);
    }

    private void emitirAneisDePressao(World mundo, Location base, double raioBase) {
        int aneis = 2;
        int pontosPorAnel = 18 + (int) (intensidadeExpansao * 18);

        for (int anel = 0; anel < aneis; anel++) {
            double altura = 0.18 + (anel * 0.82);
            double raio = raioBase * (1.0 - (anel * 0.18));
            double giro = angulo + (anel * Math.PI / 2.0);

            for (int ponto = 0; ponto < pontosPorAnel; ponto++) {
                double anguloAtual = giro + ponto * ((Math.PI * 2.0) / pontosPorAnel);
                double ondulacao = Math.sin(angulo + ponto * 0.55) * 0.10;

                double x = Math.cos(anguloAtual) * (raio + ondulacao);
                double z = Math.sin(anguloAtual) * (raio + ondulacao);

                Location localParticula = base.clone().add(x, altura, z);

                Vector direcaoParaFora = new Vector(x, 0.05 + intensidadeExpansao * 0.08, z);

                if (direcaoParaFora.lengthSquared() == 0) {
                    continue;
                }

                direcaoParaFora.normalize();

                mundo.spawnParticle(
                        Particle.SMOKE,
                        localParticula,
                        0,
                        direcaoParaFora.getX(),
                        direcaoParaFora.getY(),
                        direcaoParaFora.getZ(),
                        0.018 + (intensidadeExpansao * 0.015)
                );
            }
        }
    }

    private void emitirColunasDeFumaca(World mundo, Location base, double alturaMaxima) {
        int quantidade = 5 + (int) (intensidadeExpansao * 11);

        for (int i = 0; i < quantidade; i++) {
            double deslocamentoX = (random.nextDouble() - 0.5) * (0.35 + intensidadeExpansao * 0.75);
            double deslocamentoZ = (random.nextDouble() - 0.5) * (0.35 + intensidadeExpansao * 0.75);
            double altura = 0.12 + (random.nextDouble() * alturaMaxima);

            Location localParticula = base.clone().add(deslocamentoX, altura, deslocamentoZ);

            mundo.spawnParticle(
                    Particle.SMOKE,
                    localParticula,
                    0,
                    deslocamentoX * 0.02,
                    0.10 + (intensidadeExpansao * 0.08),
                    deslocamentoZ * 0.02,
                    0.026 + (intensidadeExpansao * 0.012)
            );
        }
    }

    private void emitirFragmentosOrbitais(World mundo, Location base, double raioBase, double alturaMaxima) {
        int pontos = 8 + (int) (intensidadeExpansao * 10);

        for (int i = 0; i < pontos; i++) {
            double progresso = i / (double) pontos;
            double anguloAtual = angulo * 1.35 + progresso * Math.PI * 2.0;
            double altura = 0.35 + (Math.sin(angulo + progresso * Math.PI * 2.0) * 0.5 + 0.5) * alturaMaxima;
            double raio = raioBase * (0.55 + progresso * 0.35);

            double x = Math.cos(anguloAtual) * raio;
            double z = Math.sin(anguloAtual) * raio;

            Location ponto = base.clone().add(x, altura, z);

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    3,
                    0.05,
                    0.06,
                    0.05,
                    0.006
            );

            if (random.nextDouble() < 0.18 + intensidadeExpansao * 0.18) {
                mundo.spawnParticle(
                        Particle.SOUL,
                        ponto,
                        1,
                        0.03,
                        0.04,
                        0.03,
                        0.004
                );
            }
        }
    }

    private void emitirPulsoEscuro(World mundo, Location base, double raioBase) {
        if (random.nextDouble() > 0.22 + intensidadeExpansao * 0.12) {
            return;
        }

        int pontos = 20;
        double raio = raioBase * 0.85;

        for (int i = 0; i < pontos; i++) {
            double anguloAtual = i * ((Math.PI * 2.0) / pontos);
            double x = Math.cos(anguloAtual) * raio;
            double z = Math.sin(anguloAtual) * raio;

            Location ponto = base.clone().add(x, 0.08, z);
            Vector direcao = new Vector(x, 0.02, z);

            if (direcao.lengthSquared() == 0) {
                continue;
            }

            direcao.normalize();

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    0,
                    direcao.getX(),
                    direcao.getY(),
                    direcao.getZ(),
                    0.055 + intensidadeExpansao * 0.045
            );
        }
    }
}

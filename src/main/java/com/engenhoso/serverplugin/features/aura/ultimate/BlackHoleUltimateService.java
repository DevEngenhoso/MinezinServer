package com.engenhoso.serverplugin.features.aura.ultimate;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BlackHoleUltimateService {

    private static final String NICK_DONO_DA_AURA = "Engenhoso";

    private static final long COOLDOWN_MS = 1_000L;

    private static final int TEMPO_IMPLOSAO_TICKS = 20;
    private static final int DURACAO_DOMO_TICKS = 20 * 10;

    private static final double RAIO_DOMO = 8.0;
    private static final double DANO_POR_SEGUNDO = 2.0;

    private static final int SEGUNDO_CONGELAMENTO_TOTAL = 7;

    private static final int NIVEL_ABSORCAO = 4; // amplifier 4 = Absorção V
    private static final int DURACAO_ABSORCAO_TICKS = 45;

    private final JavaPlugin plugin;
    private final BlackHoleUltimateItem blackHoleUltimateItem;

    private final Random random = new Random();

    private final Map<UUID, Long> ultimosUsos = new HashMap<>();

    private final Set<BukkitTask> tarefasAtivas = new HashSet<>();
    private final Set<Projectile> projeteisCongelados = new HashSet<>();
    private final Map<LivingEntity, Boolean> estadoOriginalAi = new HashMap<>();

    public BlackHoleUltimateService(JavaPlugin plugin, BlackHoleUltimateItem blackHoleUltimateItem) {
        this.plugin = plugin;
        this.blackHoleUltimateItem = blackHoleUltimateItem;
    }

    public void tentarArremessar(Player jogador) {
        if (!ehDonoDaAura(jogador)) {
            jogador.sendMessage("§cA Singularidade Sombria não responde à sua aura.");
            return;
        }

        long segundosRestantes = getSegundosRestantesCooldown(jogador);

        if (segundosRestantes > 0) {
            jogador.sendMessage("§cA Singularidade Sombria está recarregando. Aguarde §e"
                    + segundosRestantes + "s§c.");
            return;
        }

        Snowball projetil = jogador.launchProjectile(Snowball.class);

        projetil.setItem(blackHoleUltimateItem.criarItemVisualDoProjetil());
        projetil.setShooter(jogador);
        projetil.setGravity(true);

        blackHoleUltimateItem.marcarProjetil(projetil);

        ultimosUsos.put(jogador.getUniqueId(), System.currentTimeMillis());

        jogador.getWorld().playSound(
                jogador.getLocation(),
                Sound.ENTITY_WITHER_SHOOT,
                1.3f,
                0.55f
        );
    }

    public void iniciarImplosaoEDomo(Player dono, Location impacto) {
        if (impacto == null || impacto.getWorld() == null) {
            return;
        }

        World mundo = impacto.getWorld();
        Location centro = impacto.clone();

        mundo.playSound(
                centro,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                1.8f,
                0.45f
        );

        BukkitTask tarefa = new BukkitRunnable() {
            private int tempo = 0;

            @Override
            public void run() {
                if (tempo >= TEMPO_IMPLOSAO_TICKS) {
                    mundo.spawnParticle(
                            Particle.SMOKE,
                            centro,
                            85,
                            0.35,
                            0.35,
                            0.35,
                            0.12
                    );

                    mundo.playSound(
                            centro,
                            Sound.ENTITY_GENERIC_EXPLODE,
                            0.8f,
                            0.55f
                    );

                    criarDomo(dono, mundo, centro);

                    this.cancel();
                    return;
                }

                emitirImplosao(mundo, centro, tempo);

                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        tarefasAtivas.add(tarefa);
    }

    public void parar() {
        for (BukkitTask tarefa : tarefasAtivas) {
            if (tarefa != null) {
                tarefa.cancel();
            }
        }

        tarefasAtivas.clear();

        restaurarProjeteisCongelados();
        restaurarAiDasCriaturas();
    }

    private void criarDomo(Player dono, World mundo, Location centro) {
        mundo.playSound(
                centro,
                Sound.ENTITY_WITHER_AMBIENT,
                2.0f,
                0.35f
        );

        BukkitTask tarefa = new BukkitRunnable() {
            private int tempo = 0;

            @Override
            public void run() {
                if (tempo >= DURACAO_DOMO_TICKS) {
                    finalizarDomo(mundo, centro);
                    this.cancel();
                    return;
                }

                emitirBordaDoDomo(mundo, centro, tempo);
                emitirNeveSombriaAcelerada(mundo, centro);
                aplicarDistorcaoDoTempo(dono, mundo, centro, tempo);
                aplicarAbsorcaoNoDono(dono, centro);

                if (tempo % 20 == 0) {
                    aplicarDanoNoDomo(dono, mundo, centro);
                    emitirPulsoDoDomo(mundo, centro);
                }

                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        tarefasAtivas.add(tarefa);
    }

    private void emitirImplosao(World mundo, Location centro, int tempo) {
        double progresso = tempo / (double) TEMPO_IMPLOSAO_TICKS;
        double raio = 2.8 * (1.0 - progresso) + 0.15;

        int pontos = 70;

        for (int i = 0; i < pontos; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos((2.0 * random.nextDouble()) - 1.0);

            double x = Math.cos(theta) * Math.sin(phi) * raio;
            double y = Math.cos(phi) * raio;
            double z = Math.sin(theta) * Math.sin(phi) * raio;

            Location ponto = centro.clone().add(x, y, z);

            Vector direcaoParaCentro = centro.toVector().subtract(ponto.toVector());

            if (direcaoParaCentro.lengthSquared() == 0) {
                continue;
            }

            direcaoParaCentro.normalize();

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    0,
                    direcaoParaCentro.getX(),
                    direcaoParaCentro.getY(),
                    direcaoParaCentro.getZ(),
                    0.28
            );
        }

        if (tempo % 5 == 0) {
            mundo.playSound(
                    centro,
                    Sound.ENTITY_ENDERMAN_TELEPORT,
                    0.45f,
                    0.35f + (float) (progresso * 0.25f)
            );
        }
    }

    private void emitirBordaDoDomo(World mundo, Location centro, int tempo) {
        if (tempo % 2 != 0) {
            return;
        }

        double fase = tempo * 0.08;

        int camadas = 9;
        int pontosPorAnel = 40;

        for (int camada = 0; camada < camadas; camada++) {
            double progressoVertical = -1.0 + ((2.0 * camada) / (camadas - 1.0));
            double y = progressoVertical * RAIO_DOMO;

            double raioAnel = Math.sqrt(Math.max(0.0, (RAIO_DOMO * RAIO_DOMO) - (y * y)));
            double giroCamada = fase + (camada * 0.42);

            for (int ponto = 0; ponto < pontosPorAnel; ponto++) {
                double angulo = giroCamada + ponto * ((Math.PI * 2.0) / pontosPorAnel);

                double x = Math.cos(angulo) * raioAnel;
                double z = Math.sin(angulo) * raioAnel;

                Location localParticula = centro.clone().add(x, y, z);

                Vector direcaoParaFora = localParticula.toVector().subtract(centro.toVector());

                if (direcaoParaFora.lengthSquared() == 0) {
                    continue;
                }

                direcaoParaFora.normalize();

                mundo.spawnParticle(
                        Particle.SMOKE,
                        localParticula,
                        0,
                        direcaoParaFora.getX(),
                        direcaoParaFora.getY() * 0.35,
                        direcaoParaFora.getZ(),
                        0.025
                );
            }
        }
    }

    private void emitirNeveSombriaAcelerada(World mundo, Location centro) {
        int particulas = 38;

        for (int i = 0; i < particulas; i++) {
            double x = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO;
            double y = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO;
            double z = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO;

            if ((x * x) + (y * y) + (z * z) > RAIO_DOMO * RAIO_DOMO) {
                continue;
            }

            Location localParticula = centro.clone().add(x, y, z);

            double ventoX = (random.nextDouble() - 0.5) * 0.08;
            double ventoZ = (random.nextDouble() - 0.5) * 0.08;

            mundo.spawnParticle(
                    Particle.SMOKE,
                    localParticula,
                    0,
                    ventoX,
                    -1.0,
                    ventoZ,
                    0.36
            );
        }
    }

    private void aplicarDistorcaoDoTempo(Player dono, World mundo, Location centro, int tempo) {
        int segundoAtual = tempo / 20;
        double fatorVelocidade = calcularFatorVelocidadePorTick(segundoAtual);
        boolean congelamentoTotal = segundoAtual >= SEGUNDO_CONGELAMENTO_TOTAL;

        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_DOMO,
                RAIO_DOMO,
                RAIO_DOMO
        )) {
            if (!estaDentroDoDomo(entidade.getLocation(), centro)) {
                continue;
            }

            if (ehDono(entidade, dono)) {
                continue;
            }

            if (entidade instanceof LivingEntity criatura) {
                aplicarLentidaoEmCriatura(criatura, segundoAtual, fatorVelocidade, congelamentoTotal);
                continue;
            }

            if (entidade instanceof Projectile projectile) {
                aplicarLentidaoEmProjetil(projectile, fatorVelocidade, congelamentoTotal);
            }
        }
    }

    private void aplicarLentidaoEmCriatura(
            LivingEntity criatura,
            int segundoAtual,
            double fatorVelocidade,
            boolean congelamentoTotal
    ) {
        if (criatura.isDead()) {
            return;
        }

        int amplificadorLentidao = calcularAmplificadorLentidao(segundoAtual);

        criatura.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                45,
                amplificadorLentidao,
                true,
                false,
                true
        ));

        if (congelamentoTotal) {
            criatura.setVelocity(new Vector(0, 0, 0));

            if (!(criatura instanceof Player)) {
                if (!estadoOriginalAi.containsKey(criatura)) {
                    estadoOriginalAi.put(criatura, criatura.hasAI());
                }

                criatura.setAI(false);
            }

            return;
        }

        Vector velocidade = criatura.getVelocity();

        velocidade.setX(velocidade.getX() * fatorVelocidade);
        velocidade.setZ(velocidade.getZ() * fatorVelocidade);

        criatura.setVelocity(velocidade);
    }

    private void aplicarLentidaoEmProjetil(
            Projectile projectile,
            double fatorVelocidade,
            boolean congelamentoTotal
    ) {
        if (projectile.isDead() || !projectile.isValid()) {
            return;
        }

        if (blackHoleUltimateItem.ehProjetilUltimate(projectile)) {
            return;
        }

        if (congelamentoTotal) {
            projectile.setVelocity(new Vector(0, 0, 0));
            projectile.setGravity(false);

            projeteisCongelados.add(projectile);
            return;
        }

        Vector velocidade = projectile.getVelocity().multiply(fatorVelocidade);
        projectile.setVelocity(velocidade);
    }

    private void aplicarDanoNoDomo(Player dono, World mundo, Location centro) {
        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_DOMO,
                RAIO_DOMO,
                RAIO_DOMO
        )) {
            if (!(entidade instanceof LivingEntity criatura)) {
                continue;
            }

            if (criatura.isDead()) {
                continue;
            }

            if (!estaDentroDoDomo(criatura.getLocation(), centro)) {
                continue;
            }

            if (ehDono(criatura, dono)) {
                continue;
            }

            if (dono != null && dono.isOnline()) {
                criatura.damage(DANO_POR_SEGUNDO, dono);
            } else {
                criatura.damage(DANO_POR_SEGUNDO);
            }
        }
    }

    private void aplicarAbsorcaoNoDono(Player dono, Location centro) {
        if (dono == null || !dono.isOnline()) {
            return;
        }

        if (!estaDentroDoDomo(dono.getLocation(), centro)) {
            return;
        }

        dono.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                DURACAO_ABSORCAO_TICKS,
                NIVEL_ABSORCAO,
                true,
                false,
                true
        ));
    }

    private void emitirPulsoDoDomo(World mundo, Location centro) {
        mundo.playSound(
                centro,
                Sound.ENTITY_WITHER_AMBIENT,
                1.1f,
                0.25f
        );

        int pontos = 96;

        for (int i = 0; i < pontos; i++) {
            double angulo = i * ((Math.PI * 2.0) / pontos);

            double x = Math.cos(angulo) * RAIO_DOMO;
            double z = Math.sin(angulo) * RAIO_DOMO;

            Location ponto = centro.clone().add(x, 0, z);

            Vector direcaoParaDentro = centro.toVector().subtract(ponto.toVector());

            if (direcaoParaDentro.lengthSquared() == 0) {
                continue;
            }

            direcaoParaDentro.normalize();

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    0,
                    direcaoParaDentro.getX(),
                    0.08,
                    direcaoParaDentro.getZ(),
                    0.18
            );
        }
    }

    private void finalizarDomo(World mundo, Location centro) {
        mundo.playSound(
                centro,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                1.4f,
                0.3f
        );

        mundo.spawnParticle(
                Particle.SMOKE,
                centro,
                120,
                RAIO_DOMO * 0.35,
                RAIO_DOMO * 0.35,
                RAIO_DOMO * 0.35,
                0.08
        );

        restaurarProjeteisCongelados();
        restaurarAiDasCriaturas();
    }

    private double calcularFatorVelocidadePorTick(int segundoAtual) {
        if (segundoAtual <= 0) {
            return 0.96;
        }

        if (segundoAtual == 1) {
            return 0.92;
        }

        if (segundoAtual == 2) {
            return 0.86;
        }

        if (segundoAtual == 3) {
            return 0.78;
        }

        if (segundoAtual == 4) {
            return 0.68;
        }

        if (segundoAtual == 5) {
            return 0.55;
        }

        if (segundoAtual == 6) {
            return 0.35;
        }

        return 0.0;
    }

    private int calcularAmplificadorLentidao(int segundoAtual) {
        if (segundoAtual >= SEGUNDO_CONGELAMENTO_TOTAL) {
            return 255;
        }

        return Math.min(7, 1 + segundoAtual);
    }

    private long getSegundosRestantesCooldown(Player jogador) {
        long ultimoUso = ultimosUsos.getOrDefault(jogador.getUniqueId(), 0L);
        long restante = COOLDOWN_MS - (System.currentTimeMillis() - ultimoUso);

        if (restante <= 0) {
            return 0;
        }

        return (long) Math.ceil(restante / 1000.0);
    }

    private boolean estaDentroDoDomo(Location local, Location centro) {
        if (local == null || centro == null) {
            return false;
        }

        if (local.getWorld() == null || centro.getWorld() == null) {
            return false;
        }

        if (!local.getWorld().equals(centro.getWorld())) {
            return false;
        }

        return local.distanceSquared(centro) <= RAIO_DOMO * RAIO_DOMO;
    }

    private boolean ehDono(Entity entidade, Player dono) {
        if (dono == null) {
            return false;
        }

        if (!(entidade instanceof Player jogador)) {
            return false;
        }

        return jogador.getUniqueId().equals(dono.getUniqueId());
    }

    private boolean ehDonoDaAura(Player jogador) {
        return jogador.getName().equalsIgnoreCase(NICK_DONO_DA_AURA);
    }

    private void restaurarProjeteisCongelados() {
        for (Projectile projectile : projeteisCongelados) {
            if (projectile == null) {
                continue;
            }

            if (projectile.isDead() || !projectile.isValid()) {
                continue;
            }

            projectile.setGravity(true);
        }

        projeteisCongelados.clear();
    }

    private void restaurarAiDasCriaturas() {
        for (Map.Entry<LivingEntity, Boolean> entry : estadoOriginalAi.entrySet()) {
            LivingEntity criatura = entry.getKey();
            Boolean tinhaAi = entry.getValue();

            if (criatura == null || tinhaAi == null) {
                continue;
            }

            if (criatura.isDead() || !criatura.isValid()) {
                continue;
            }

            criatura.setAI(tinhaAi);
        }

        estadoOriginalAi.clear();
    }
}
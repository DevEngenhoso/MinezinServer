package com.engenhoso.serverplugin.features.aura;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

public class AuraService {

    private static final String NICK_DONO_DA_AURA = "Engenhoso";
    private static final String NOME_ESPADA_SECRETA = "Skeleton Piercer";

    private static final long INTERVALO_TICKS = 5L;
    private static final int TEMPO_PARA_ATIVAR_TICKS = 20;

    private static final double DISTANCIA_DAS_COSTAS = 0.22;
    private static final double VELOCIDADE_PARTICULA_NORMAL = 0.012;

    private static final int PONTOS_AURA_NORMAL = 2;

    private static final double DISTANCIA_AVANCO = 10.0;
    private static final long COOLDOWN_AVANCO_MS = 1200L;

    private static final int DURACAO_ESFERA_TICKS = 20 * 5;
    private static final double RAIO_ATRACAO_ESFERA = 4.0;
    private static final double RAIO_NUCLEO_ESFERA = 0.55;
    private static final double FORCA_ATRACAO_ESFERA = 0.28;
    private static final double FORCA_REPULSAO_EXPLOSAO = 1.65;
    private static final double FORCA_VERTICAL_EXPLOSAO = 0.75;

    private static final Sound[] SONS_WITHER_IDLE = {
            Sound.ENTITY_WITHER_AMBIENT
    };

    private static final Sound SOM_EXPLOSAO_ORBE = Sound.ENTITY_GENERIC_EXPLODE;

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private BukkitTask tarefaAura;
    private double angulo = 0.0;

    private int ticksCarregando = 0;
    private double poderIntimidacao = 0.0;

    private long ultimoAvanco = 0L;

    public AuraService(JavaPlugin plugin) {
        this.plugin = plugin;
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

                atualizarEstadoSecreto(jogador);

                if (poderIntimidacao > 0.0) {
                    emitirAuraIntimidacao(jogador);
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

    public boolean podeExecutarAvancoSombrio(Player jogador) {
        if (!ehDonoDaAura(jogador)) {
            return false;
        }

        if (!jogador.isSneaking()) {
            return false;
        }

        if (!estaSegurandoSkeletonPiercer(jogador)) {
            return false;
        }

        if (poderIntimidacao <= 0.0) {
            return false;
        }

        long agora = System.currentTimeMillis();

        return agora - ultimoAvanco >= COOLDOWN_AVANCO_MS;
    }

    public void executarAvancoSombrio(Player jogador) {
        Location origem = jogador.getLocation();
        World mundo = jogador.getWorld();

        Vector direcao = origem.getDirection();
        direcao.setY(0);

        if (direcao.lengthSquared() == 0) {
            return;
        }

        direcao.normalize();

        Location pontoAdiante = origem.clone().add(direcao.multiply(DISTANCIA_AVANCO));

        int blocoX = pontoAdiante.getBlockX();
        int blocoZ = pontoAdiante.getBlockZ();

        Block blocoMaisAlto = mundo.getHighestBlockAt(blocoX, blocoZ);

        Location destino = new Location(
                mundo,
                blocoX + 0.5,
                blocoMaisAlto.getY() + 1.0,
                blocoZ + 0.5,
                origem.getYaw(),
                origem.getPitch()
        );

        emitirRastroDoAvanco(mundo, origem, destino);

        tocarSomWitherIdle(mundo, origem);

        criarEsferaSombriaDeAtracao(mundo, origem.clone().add(0, 1.0, 0));

        jogador.teleport(destino);
        jogador.setFallDistance(0);

        tocarSomWitherIdle(mundo, destino);

        mundo.spawnParticle(
                Particle.SMOKE,
                destino.clone().add(0, 1.0, 0),
                18,
                0.25,
                0.75,
                0.25,
                0.03
        );

        ultimoAvanco = System.currentTimeMillis();

        ticksCarregando = 0;
        poderIntimidacao = 0.0;
    }

    private void atualizarEstadoSecreto(Player jogador) {
        boolean podeCarregar = jogador.isSneaking() && estaSegurandoSkeletonPiercer(jogador);

        if (!podeCarregar) {
            ticksCarregando = 0;
            poderIntimidacao = Math.max(0.0, poderIntimidacao - 0.12);
            return;
        }

        ticksCarregando += INTERVALO_TICKS;

        if (ticksCarregando < TEMPO_PARA_ATIVAR_TICKS) {
            poderIntimidacao = 0.0;
            return;
        }

        poderIntimidacao = Math.min(1.0, poderIntimidacao + 0.08);
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

        angulo += Math.PI / 36;

        for (int i = 0; i < PONTOS_AURA_NORMAL; i++) {
            double altura = 0.45 + (i * 0.45) + (Math.sin(angulo + i) * 0.03);

            Location localParticula = base.clone()
                    .add(0, altura, 0)
                    .add(costas.clone().multiply(DISTANCIA_DAS_COSTAS));

            mundo.spawnParticle(
                    Particle.SMOKE,
                    localParticula,
                    0,
                    costas.getX(),
                    0.01,
                    costas.getZ(),
                    VELOCIDADE_PARTICULA_NORMAL
            );
        }
    }

    private void emitirAuraIntimidacao(Player jogador) {
        World mundo = jogador.getWorld();
        Location base = jogador.getLocation();

        angulo += Math.PI / 18;

        double raioBase = 0.30 + (poderIntimidacao * 0.85);
        double alturaMaxima = 1.20 + (poderIntimidacao * 1.00);

        int camadas = 3 + (int) (poderIntimidacao * 4);
        int pontosPorCamada = 5 + (int) (poderIntimidacao * 7);

        for (int camada = 0; camada < camadas; camada++) {
            double progressoAltura = camada / Math.max(1.0, camadas - 1.0);
            double altura = 0.15 + (alturaMaxima * progressoAltura);

            double raio = raioBase * (1.0 - (progressoAltura * 0.35));
            double giroCamada = angulo + (camada * 0.55);

            for (int ponto = 0; ponto < pontosPorCamada; ponto++) {
                double anguloAtual = giroCamada + ponto * ((Math.PI * 2) / pontosPorCamada);

                double x = Math.cos(anguloAtual) * raio;
                double z = Math.sin(anguloAtual) * raio;

                Location localParticula = base.clone().add(x, altura, z);

                Vector direcaoParaFora = new Vector(x, 0.12 + (poderIntimidacao * 0.05), z);

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
                        0.018 + (poderIntimidacao * 0.012)
                );
            }
        }

        emitirExplosaoVertical(base, mundo);
    }

    private void emitirExplosaoVertical(Location base, World mundo) {
        int quantidade = 4 + (int) (poderIntimidacao * 8);

        for (int i = 0; i < quantidade; i++) {
            double deslocamentoX = (random.nextDouble() - 0.5) * 0.35;
            double deslocamentoZ = (random.nextDouble() - 0.5) * 0.35;
            double altura = 0.15 + (random.nextDouble() * 1.85);

            Location localParticula = base.clone().add(deslocamentoX, altura, deslocamentoZ);

            mundo.spawnParticle(
                    Particle.SMOKE,
                    localParticula,
                    0,
                    deslocamentoX * 0.02,
                    0.08 + (poderIntimidacao * 0.04),
                    deslocamentoZ * 0.02,
                    0.02
            );
        }
    }

    private void emitirRastroDoAvanco(World mundo, Location origem, Location destino) {
        Location inicio = origem.clone().add(0, 1.0, 0);
        Location fim = destino.clone().add(0, 1.0, 0);

        Vector diferenca = fim.toVector().subtract(inicio.toVector());
        double distancia = diferenca.length();

        if (distancia <= 0) {
            return;
        }

        Vector direcao = diferenca.clone().normalize();

        Vector lateral = new Vector(-direcao.getZ(), 0, direcao.getX());

        if (lateral.lengthSquared() == 0) {
            lateral = new Vector(1, 0, 0);
        }

        lateral.normalize();

        Vector vertical = new Vector(0, 1, 0);

        double passo = 0.16;
        double fase = random.nextDouble() * Math.PI * 2;

        for (double percorrido = 0; percorrido < distancia; percorrido += passo) {
            double progresso = percorrido / distancia;

            Location pontoBase = inicio.clone().add(direcao.clone().multiply(percorrido));

            double curvaPrincipal = Math.sin((progresso * Math.PI * 5.0) + fase) * 0.38;
            double curvaSecundaria = Math.sin((progresso * Math.PI * 11.0) + fase) * 0.12;
            double oscilacaoVertical = Math.cos((progresso * Math.PI * 7.0) + fase) * 0.18;

            double jitterLateral = (random.nextDouble() - 0.5) * 0.22;
            double jitterVertical = (random.nextDouble() - 0.5) * 0.16;

            Vector deslocamento = lateral.clone()
                    .multiply(curvaPrincipal + curvaSecundaria + jitterLateral)
                    .add(vertical.clone().multiply(oscilacaoVertical + jitterVertical));

            Location pontoCurvo = pontoBase.clone().add(deslocamento);

            mundo.spawnParticle(
                    Particle.SMOKE,
                    pontoCurvo,
                    7,
                    0.07,
                    0.07,
                    0.07,
                    0.004
            );

            if (random.nextDouble() < 0.65) {
                Location pontoFragmento = pontoCurvo.clone()
                        .add(lateral.clone().multiply((random.nextDouble() - 0.5) * 0.55))
                        .add(0, (random.nextDouble() - 0.5) * 0.35, 0);

                mundo.spawnParticle(
                        Particle.SMOKE,
                        pontoFragmento,
                        4,
                        0.06,
                        0.08,
                        0.06,
                        0.003
                );
            }

            if (random.nextDouble() < 0.35) {
                Location pontoEstouro = pontoCurvo.clone()
                        .add(lateral.clone().multiply((random.nextDouble() - 0.5) * 0.85))
                        .add(0, (random.nextDouble() - 0.5) * 0.55, 0);

                mundo.spawnParticle(
                        Particle.SMOKE,
                        pontoEstouro,
                        8,
                        0.10,
                        0.12,
                        0.10,
                        0.006
                );
            }
        }

        mundo.spawnParticle(
                Particle.SMOKE,
                inicio,
                20,
                0.35,
                0.55,
                0.35,
                0.015
        );

        mundo.spawnParticle(
                Particle.SMOKE,
                fim,
                24,
                0.38,
                0.65,
                0.38,
                0.018
        );
    }

    private void criarEsferaSombriaDeAtracao(World mundo, Location centro) {
        new BukkitRunnable() {
            private int tempo = 0;
            private double giro = random.nextDouble() * Math.PI * 2;

            @Override
            public void run() {
                if (tempo >= DURACAO_ESFERA_TICKS) {
                    explodirEsferaSombria(mundo, centro);
                    this.cancel();
                    return;
                }

                emitirNucleoDaEsfera(mundo, centro);
                emitirEspiralDaEsfera(mundo, centro, giro);
                atrairCriaturasParaEsfera(mundo, centro);

                giro += Math.PI / 10;
                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void emitirNucleoDaEsfera(World mundo, Location centro) {
        mundo.spawnParticle(
                Particle.SMOKE,
                centro,
                24,
                RAIO_NUCLEO_ESFERA,
                RAIO_NUCLEO_ESFERA,
                RAIO_NUCLEO_ESFERA,
                0.015
        );

        for (int i = 0; i < 8; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = random.nextDouble() * Math.PI;

            double x = Math.cos(theta) * Math.sin(phi) * RAIO_NUCLEO_ESFERA;
            double y = Math.cos(phi) * RAIO_NUCLEO_ESFERA;
            double z = Math.sin(theta) * Math.sin(phi) * RAIO_NUCLEO_ESFERA;

            Location ponto = centro.clone().add(x, y, z);

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    3,
                    0.04,
                    0.04,
                    0.04,
                    0.004
            );
        }
    }

    private void emitirEspiralDaEsfera(World mundo, Location centro, double giro) {
        int pontos = 22;

        for (int i = 0; i < pontos; i++) {
            double progresso = i / (double) pontos;

            double anguloAtual = giro + (progresso * Math.PI * 2.0);
            double altura = Math.sin((giro * 0.65) + (progresso * Math.PI * 2.0)) * 1.25;

            double raioOndulado = RAIO_ATRACAO_ESFERA
                    + Math.sin((giro * 0.8) + (progresso * Math.PI * 6.0)) * 0.22;

            double x = Math.cos(anguloAtual) * raioOndulado;
            double z = Math.sin(anguloAtual) * raioOndulado;

            Location pontoEspiral = centro.clone().add(x, altura, z);

            Vector direcaoParaCentro = centro.toVector().subtract(pontoEspiral.toVector());

            if (direcaoParaCentro.lengthSquared() > 0) {
                direcaoParaCentro.normalize().multiply(0.02);
            }

            mundo.spawnParticle(
                    Particle.SMOKE,
                    pontoEspiral,
                    4,
                    0.08,
                    0.08,
                    0.08,
                    0.006
            );

            mundo.spawnParticle(
                    Particle.SMOKE,
                    pontoEspiral,
                    0,
                    direcaoParaCentro.getX(),
                    direcaoParaCentro.getY(),
                    direcaoParaCentro.getZ(),
                    0.035
            );
        }
    }

    private void atrairCriaturasParaEsfera(World mundo, Location centro) {
        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_ATRACAO_ESFERA,
                RAIO_ATRACAO_ESFERA,
                RAIO_ATRACAO_ESFERA
        )) {
            if (!(entidade instanceof LivingEntity criatura)) {
                continue;
            }

            if (deveIgnorarEntidade(criatura)) {
                continue;
            }

            if (criatura.isDead()) {
                continue;
            }

            Location pontoCorpo = obterPontoCentralDoCorpo(criatura);

            if (!pontoCorpo.getWorld().equals(mundo)) {
                continue;
            }

            double distancia = pontoCorpo.distance(centro);

            if (distancia > RAIO_ATRACAO_ESFERA || distancia < 0.45) {
                continue;
            }

            Vector direcaoParaCentro = centro.toVector().subtract(pontoCorpo.toVector());

            if (direcaoParaCentro.lengthSquared() == 0) {
                continue;
            }

            direcaoParaCentro.normalize();

            double intensidade = 1.0 - (distancia / RAIO_ATRACAO_ESFERA);
            double forca = FORCA_ATRACAO_ESFERA + (intensidade * 0.18);

            Vector velocidade = direcaoParaCentro.multiply(forca);

            double yLimitado = limitar(velocidade.getY(), -0.22, 0.22);
            velocidade.setY(yLimitado);

            if (distancia < 1.1) {
                velocidade.multiply(0.55);
            }

            criatura.setVelocity(velocidade);
        }
    }

    private void explodirEsferaSombria(World mundo, Location centro) {
        mundo.playSound(
                centro,
                SOM_EXPLOSAO_ORBE,
                2.0f,
                0.75f
        );

        emitirExplosaoDeFumacaDaEsfera(mundo, centro);
        repelirCriaturasDaEsfera(mundo, centro);
    }

    private void emitirExplosaoDeFumacaDaEsfera(World mundo, Location centro) {
        mundo.spawnParticle(
                Particle.SMOKE,
                centro,
                80,
                0.65,
                0.65,
                0.65,
                0.08
        );

        int pontos = 90;

        for (int i = 0; i < pontos; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = random.nextDouble() * Math.PI;

            double distancia = 0.5 + (random.nextDouble() * RAIO_ATRACAO_ESFERA);

            double x = Math.cos(theta) * Math.sin(phi) * distancia;
            double y = Math.cos(phi) * distancia;
            double z = Math.sin(theta) * Math.sin(phi) * distancia;

            Location pontoExplosao = centro.clone().add(x, y, z);

            Vector direcaoParaFora = pontoExplosao.toVector().subtract(centro.toVector());

            if (direcaoParaFora.lengthSquared() == 0) {
                continue;
            }

            direcaoParaFora.normalize();

            mundo.spawnParticle(
                    Particle.SMOKE,
                    pontoExplosao,
                    0,
                    direcaoParaFora.getX(),
                    direcaoParaFora.getY(),
                    direcaoParaFora.getZ(),
                    0.22
            );

            if (random.nextDouble() < 0.45) {
                mundo.spawnParticle(
                        Particle.SMOKE,
                        pontoExplosao,
                        5,
                        0.18,
                        0.18,
                        0.18,
                        0.035
                );
            }
        }

        for (double raio = 0.7; raio <= RAIO_ATRACAO_ESFERA; raio += 0.45) {
            int pontosDoAnel = 24;

            for (int i = 0; i < pontosDoAnel; i++) {
                double anguloAtual = i * ((Math.PI * 2) / pontosDoAnel);

                double x = Math.cos(anguloAtual) * raio;
                double z = Math.sin(anguloAtual) * raio;

                Location pontoAnel = centro.clone().add(x, 0, z);

                Vector direcaoParaFora = new Vector(x, 0.12, z);

                if (direcaoParaFora.lengthSquared() == 0) {
                    continue;
                }

                direcaoParaFora.normalize();

                mundo.spawnParticle(
                        Particle.SMOKE,
                        pontoAnel,
                        0,
                        direcaoParaFora.getX(),
                        direcaoParaFora.getY(),
                        direcaoParaFora.getZ(),
                        0.18
                );
            }
        }
    }

    private void repelirCriaturasDaEsfera(World mundo, Location centro) {
        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_ATRACAO_ESFERA,
                RAIO_ATRACAO_ESFERA,
                RAIO_ATRACAO_ESFERA
        )) {
            if (!(entidade instanceof LivingEntity criatura)) {
                continue;
            }

            if (deveIgnorarEntidade(criatura)) {
                continue;
            }

            if (criatura.isDead()) {
                continue;
            }

            Location pontoCorpo = obterPontoCentralDoCorpo(criatura);

            if (!pontoCorpo.getWorld().equals(mundo)) {
                continue;
            }

            double distancia = pontoCorpo.distance(centro);

            if (distancia > RAIO_ATRACAO_ESFERA) {
                continue;
            }

            Vector direcaoParaFora = pontoCorpo.toVector().subtract(centro.toVector());

            if (direcaoParaFora.lengthSquared() == 0) {
                direcaoParaFora = new Vector(
                        random.nextDouble() - 0.5,
                        0.2,
                        random.nextDouble() - 0.5
                );
            }

            direcaoParaFora.normalize();

            double proximidade = 1.0 - (distancia / RAIO_ATRACAO_ESFERA);
            double forcaHorizontal = FORCA_REPULSAO_EXPLOSAO + (proximidade * 0.85);

            Vector velocidade = direcaoParaFora.multiply(forcaHorizontal);
            velocidade.setY(FORCA_VERTICAL_EXPLOSAO + (proximidade * 0.35));

            criatura.setVelocity(velocidade);
        }
    }

    private Location obterPontoCentralDoCorpo(LivingEntity criatura) {
        return criatura.getLocation().clone().add(0, 0.9, 0);
    }

    private boolean deveIgnorarEntidade(LivingEntity criatura) {
        if (!(criatura instanceof Player player)) {
            return false;
        }

        return ehDonoDaAura(player);
    }

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private void tocarSomWitherIdle(World mundo, Location local) {
        Sound som = SONS_WITHER_IDLE[random.nextInt(SONS_WITHER_IDLE.length)];
        float pitch = 0.75f + (random.nextFloat() * 0.35f);

        mundo.playSound(
                local,
                som,
                1.4f,
                pitch
        );
    }

    private boolean ehDonoDaAura(Player jogador) {
        return jogador.getName().equalsIgnoreCase(NICK_DONO_DA_AURA);
    }

    private boolean estaSegurandoSkeletonPiercer(Player jogador) {
        ItemStack item = jogador.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            return false;
        }

        if (!item.getType().name().endsWith("_SWORD")) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String nomeItem = ChatColor.stripColor(meta.getDisplayName());

        return NOME_ESPADA_SECRETA.equalsIgnoreCase(nomeItem);
    }
}
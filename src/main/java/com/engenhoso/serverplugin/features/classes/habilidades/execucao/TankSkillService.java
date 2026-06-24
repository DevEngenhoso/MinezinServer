package com.engenhoso.serverplugin.features.classes.habilidades.execucao;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import com.engenhoso.serverplugin.features.classes.habilidades.ClasseResolver;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeLoadout;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitTask;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TankSkillService {

    private static final boolean MODO_TESTE_COOLDOWNS = true;
    private static final double COOLDOWN_TESTE_SEGUNDOS = 1.0;
    private static final long COOLDOWN_TESTE_MS = 1000L;

    private static final long DURACAO_PASSOS_GIGANTES_MS = 10_000L;
    private static final int DURACAO_PASSOS_GIGANTES_TICKS = 200;
    private static final double RAIO_PRESSAO_PASSOS_GIGANTES = 5.0;
    private static final double RAIO_ESMAGADA_GIGANTE = 4.5;
    private static final long COOLDOWN_ESMAGADA_GIGANTE_MS = 3_000L;

    private static final String BATIDA_DEFENSIVA = "tanque_batida_defensiva";
    private static final String AURA_DEBILITANTE = "tanque_aura_debilitante";
    private static final String ESCUDO_DE_FORCA = "tanque_escudo_de_forca";

    private static final String INVESTIDA_ARDILOSA = "tanque_investida_ardilosa";
    private static final String QUEBRA_CHAO = "tanque_quebra_chao";
    private static final String VINGANCA = "tanque_vinganca";
    private static final String ENFRENTAMENTO = "tanque_enfrentamento";
    private static final String ANEL_DA_INERCIA = "tanque_anel_da_inercia";
    private static final String GEISER_DE_PODER = "tanque_geiser_de_poder";
    private static final String RACHA_TERRA = "tanque_racha_terra";

    private static final String MALDICAO_DO_ENCOLHIMENTO = "tanque_maldicao_do_encolhimento";
    private static final String AURORA_ABENCOADA = "tanque_aurora_abencoada";
    private static final String SALTO_PROFUNDO = "tanque_salto_profundo";
    private static final String PASSOS_GIGANTES = "tanque_passos_gigantes";
    private static final String MASSACRE = "tanque_massacre";
    private static final String HIPERESTATICA = "tanque_hiperestatica";

    private final JavaPlugin plugin;
    private final HabilidadeService habilidadeService;
    private final ClasseResolver classeResolver;

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Long> stunnedAte = new HashMap<>();
    private final Map<UUID, Long> imuneAte = new HashMap<>();
    private final Map<UUID, Long> giganteAte = new HashMap<>();
    private final Map<UUID, Long> esmagadaGiganteCooldownAte = new HashMap<>();
    private final Map<UUID, Double> escalaOriginalGigante = new HashMap<>();
    private final Map<UUID, BukkitTask> passosGigantesAtivos = new HashMap<>();
    private final Map<UUID, ReducaoDano> reducoesDeDanoCausado = new HashMap<>();
    private final Map<UUID, Integer> stacksEstaticos = new HashMap<>();
    private final Map<UUID, TextDisplay> indicadoresResistencia = new HashMap<>();
    private final Map<UUID, BukkitTask> aurasDebilitantes = new HashMap<>();
    private final Map<UUID, BukkitTask> escudosDeForcaAtivos = new HashMap<>();

    private final Set<UUID> danoDeHabilidade = new HashSet<>();

    public TankSkillService(
            JavaPlugin plugin,
            HabilidadeService habilidadeService,
            ClasseResolver classeResolver
    ) {
        this.plugin = plugin;
        this.habilidadeService = habilidadeService;
        this.classeResolver = classeResolver;
    }

    public boolean executar(Player player, String habilidadeId) {
        if (!ehTanque(player)) {
            player.sendMessage("§cVocê precisa ser Tanque para usar essa habilidade.");
            return true;
        }

        switch (habilidadeId.toLowerCase(Locale.ROOT)) {
            case INVESTIDA_ARDILOSA:
                return usarInvestidaArdilosa(player);

            case QUEBRA_CHAO:
                return usarQuebraChao(player);

            case VINGANCA:
                return usarVinganca(player);

            case ENFRENTAMENTO:
                return usarEnfrentamento(player);

            case ANEL_DA_INERCIA:
                return usarAnelDaInercia(player);

            case GEISER_DE_PODER:
                return usarGeiserDePoder(player);

            case RACHA_TERRA:
                return usarRachaTerra(player);

            case MALDICAO_DO_ENCOLHIMENTO:
                return usarMaldicaoDoEncolhimento(player);

            case AURORA_ABENCOADA:
                return usarAuroraAbencoada(player);

            case SALTO_PROFUNDO:
                return usarSaltoProfundo(player);

            case PASSOS_GIGANTES:
                return usarPassosGigantes(player);

            case MASSACRE:
                return usarMassacre(player);

            case HIPERESTATICA:
                return usarHiperestatica(player);

            default:
                return false;
        }
    }

    public void aoCausarDano(EntityDamageByEntityEvent event) {
        LivingEntity causador = obterCausador(event.getDamager());

        if (causador != null) {
            ReducaoDano reducao = reducoesDeDanoCausado.get(causador.getUniqueId());

            if (reducao != null) {
                if (reducao.ativo()) {
                    event.setDamage(event.getDamage() * reducao.getMultiplicador());
                } else {
                    reducoesDeDanoCausado.remove(causador.getUniqueId());
                }
            }
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!ehTanque(player)) {
            return;
        }

        if (!passivaEquipada(player, BATIDA_DEFENSIVA)) {
            return;
        }

        if (danoDeHabilidade.contains(player.getUniqueId())) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }

        if (passivaCooldown(player, BATIDA_DEFENSIVA, 1.9)) {
            ativarBatidaDefensiva(player);
        }
    }

    public void aoReceberDano(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (estaImune(player)) {
            event.setCancelled(true);
            return;
        }

        if (!ehTanque(player)) {
            return;
        }

        if (passivaEquipada(player, AURA_DEBILITANTE)) {
            ativarAuraDebilitante(player);
        }

        if (passivaEquipada(player, ESCUDO_DE_FORCA)) {
            ativarEscudoDeForca(player);
        }
    }

    public void aoMover(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!estaAtordoado(player)) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Location destino = event.getFrom().clone();
        destino.setYaw(event.getTo().getYaw());
        destino.setPitch(event.getTo().getPitch());

        event.setTo(destino);
    }

    public void limpar(Player player) {
        UUID uuid = player.getUniqueId();

        cooldowns.remove(uuid);
        stunnedAte.remove(uuid);
        imuneAte.remove(uuid);
        finalizarPassosGigantes(player, false);
        esmagadaGiganteCooldownAte.remove(uuid);
        reducoesDeDanoCausado.remove(uuid);
        stacksEstaticos.remove(uuid);
        danoDeHabilidade.remove(uuid);
        removerIndicadorResistencia(uuid);
        cancelarAuraDebilitante(uuid);
        cancelarEscudoDeForca(uuid);
    }

    private boolean usarInvestidaArdilosa(Player player) {
        if (!iniciarCooldown(player, INVESTIDA_ARDILOSA, 14.27)) {
            return true;
        }

        Vector direcao = direcaoHorizontal(player);

        // Movimento real, sem teleport.
        // Y ~0.62 gera um salto por volta de 2 blocos.
        Vector impulso = direcao.clone().multiply(1.15);
        impulso.setY(0.62);

        player.setFallDistance(0.0f);
        player.setVelocity(impulso);

        tocarSom(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.65f, 1.45f);

        new BukkitRunnable() {
            private int ticks = 0;
            private boolean impactou = false;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                player.setFallDistance(0.0f);
                desenharRastroBrancoInvestida(player.getLocation());

                // Mantém levemente a direção horizontal sem travar a física vertical.
                if (ticks <= 10) {
                    Vector atual = player.getVelocity();
                    Vector horizontal = direcao.clone().multiply(0.95);
                    horizontal.setY(atual.getY());
                    player.setVelocity(horizontal);
                }

                boolean podeImpactar = ticks >= 6;
                boolean aterrissou = estaNoChao(player);

                if (!impactou && podeImpactar && aterrissou) {
                    impactou = true;
                    impactoInvestidaArdilosa(player, player.getLocation());
                    cancel();
                    return;
                }

                // Segurança caso bata em parede, bugue em meio bloco ou não detecte chão.
                if (!impactou && ticks >= 28) {
                    impactou = true;
                    impactoInvestidaArdilosa(player, player.getLocation());
                    cancel();
                    return;
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private boolean usarQuebraChao(Player player) {
        if (!iniciarCooldown(player, QUEBRA_CHAO, 9.51)) {
            return true;
        }

        Location centro = ajustarCentroNoChao(pontoAlvo(player, 8.0));

        impactoQuebraChao(player, centro);

        return true;
    }

    private boolean usarVinganca(Player player) {
        if (!iniciarCooldown(player, VINGANCA, 23.78)) {
            return true;
        }

        Location origem = player.getEyeLocation().clone();
        Vector direcao = origem.getDirection().normalize();

        tocarSom(origem, Sound.ENTITY_WITHER_SHOOT, 0.9f, 0.75f);

        new BukkitRunnable() {
            private double distancia = 0.0;
            private final double alcance = 25.0;
            private final double passo = 0.65;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                distancia += passo;

                if (distancia > alcance) {
                    cancel();
                    return;
                }

                Location ponto = origem.clone().add(direcao.clone().multiply(distancia));

                desenharRastroVinganca(ponto);

                for (LivingEntity alvo : inimigosProximos(player, ponto, 1.1)) {
                    tocarSom(alvo.getLocation(), Sound.BLOCK_CHAIN_HIT, 0.9f, 0.7f);
                    impactoVinganca(player, alvo);
                    cancel();
                    return;
                }

                if (ponto.getBlock().getType().isSolid()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private boolean usarEnfrentamento(Player player) {
        if (!iniciarCooldown(player, ENFRENTAMENTO, 19.02)) {
            return true;
        }

        Vector direcao = direcaoHorizontal(player);
        Set<UUID> atingidos = new HashSet<>();

        player.setVelocity(direcao.clone().multiply(1.6).setY(0.18));
        tocarSom(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 0.9f, 0.8f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks++ > 16) {
                    cancel();
                    return;
                }

                Location centro = player.getLocation();

                particulas(
                        centro,
                        Particle.CLOUD,
                        10,
                        0.45,
                        0.2,
                        0.45,
                        0.01
                );

                for (LivingEntity alvo : inimigosProximos(player, centro, 2.2)) {
                    if (!atingidos.add(alvo.getUniqueId())) {
                        continue;
                    }

                    causarDano(player, alvo, 5.5);
                    atordoar(alvo, 60);
                    alvo.setVelocity(direcao.clone().multiply(1.3).setY(0.28));
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private boolean usarAnelDaInercia(Player player) {
        if (!iniciarCooldown(player, ANEL_DA_INERCIA, 19.02)) {
            return true;
        }

        Location centro = player.getLocation().clone();

        tocarSom(centro, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 0.7f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > 100) {
                    cancel();
                    return;
                }

                ticks += 5;

                desenharCirculo(centro, 6.5, Particle.CLOUD);

                for (LivingEntity alvo : inimigosProximos(player, centro, 6.8)) {
                    aplicarEfeito(alvo, 40, 4, "SLOW", "SLOWNESS");
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);

        return true;
    }

    private boolean usarGeiserDePoder(Player player) {
        if (!iniciarCooldown(player, GEISER_DE_PODER, 9.51)) {
            return true;
        }

        Location centro = pontoAlvo(player, 11.0);

        tocarSom(centro, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.6f);

        new BukkitRunnable() {
            @Override
            public void run() {
                tocarSom(centro, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);

                particulas(
                        centro,
                        Particle.CLOUD,
                        60,
                        1.5,
                        0.3,
                        1.5,
                        0.05
                );

                for (LivingEntity alvo : inimigosProximos(player, centro, 3.0)) {
                    causarDano(player, alvo, 7.0);
                    alvo.setVelocity(new Vector(0, 1.25, 0));
                    atordoar(alvo, 20);
                }

                for (Player aliado : aliadosProximos(player, centro, 3.0)) {
                    if (aliado.equals(player)) {
                        continue;
                    }

                    empurrarParaFora(aliado, centro, 1.35);
                }
            }
        }.runTaskLater(plugin, 8L);

        return true;
    }

    private boolean usarRachaTerra(Player player) {
        if (!iniciarCooldown(player, RACHA_TERRA, 14.27)) {
            return true;
        }

        Location centro = player.getLocation();

        tocarSom(centro, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.55f);
        desenharCirculo(centro, 5.5, Particle.CLOUD);

        for (LivingEntity alvo : inimigosProximos(player, centro, 5.5)) {
            causarDano(player, alvo, 6.5);
            atordoar(alvo, 54);

            if (alvo instanceof Creature creature) {
                creature.setTarget(player);
            }
        }

        return true;
    }

    private boolean usarMaldicaoDoEncolhimento(Player player) {
        if (!iniciarCooldown(player, MALDICAO_DO_ENCOLHIMENTO, 28.54)) {
            return true;
        }

        List<LivingEntity> alvos = alvosEmCone(player, 10.0, 65.0);

        if (alvos.isEmpty()) {
            player.sendMessage("§cNenhum alvo atingido.");
            return true;
        }

        tocarSom(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.8f, 0.6f);

        for (LivingEntity alvo : alvos) {
            causarDano(player, alvo, 9.0);
            aplicarEfeito(alvo, 160, 1, "WEAKNESS");
            aplicarEfeito(alvo, 160, 1, "WITHER");
            aplicarReducaoDanoCausado(alvo, 0.60, 160);

            particulas(
                    alvo.getLocation().add(0, 1, 0),
                    Particle.CLOUD,
                    20,
                    0.5,
                    0.5,
                    0.5,
                    0.03
            );
        }

        return true;
    }

    private boolean usarAuroraAbencoada(Player player) {
        if (!iniciarCooldown(player, AURORA_ABENCOADA, 28.54)) {
            return true;
        }

        tocarSom(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > 100) {
                    cancel();
                    return;
                }

                ticks += 10;

                Location centro = player.getLocation();

                desenharCirculo(centro, 8.0, Particle.CLOUD);

                for (Player aliado : aliadosProximos(player, centro, 8.0)) {
                    aplicarEfeito(aliado, 60, 1, "SPEED");
                    aplicarEfeito(aliado, 60, 1, "ABSORPTION");
                    aplicarEfeito(aliado, 60, 0, "REGENERATION");
                    curar(aliado, 1.5);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        return true;
    }

    private boolean usarSaltoProfundo(Player player) {
        if (!iniciarCooldown(player, SALTO_PROFUNDO, 23.78)) {
            return true;
        }

        Location destino = pontoAlvo(player, 11.0);

        imuneAte.put(player.getUniqueId(), agora() + 1200L);

        player.setVelocity(new Vector(0, 1.05, 0));

        tocarSom(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 0.8f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }

                player.teleport(destino);

                Location impacto = player.getLocation();

                tocarSom(impacto, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
                desenharCirculo(impacto, 5.0, Particle.CLOUD);

                for (LivingEntity alvo : inimigosProximos(player, impacto, 5.0)) {
                    causarDano(player, alvo, 9.0);
                    aplicarEfeito(alvo, 80, 3, "SLOW", "SLOWNESS");

                    if (alvo.getLocation().distanceSquared(impacto) <= 9.0) {
                        atordoar(alvo, 44);
                    }
                }

                imuneAte.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, 12L);

        return true;
    }

    private boolean usarPassosGigantes(Player player) {
        if (estaGigante(player)) {
            return usarEsmagadaGigante(player);
        }

        if (!iniciarCooldown(player, PASSOS_GIGANTES, 28.54)) {
            return true;
        }

        UUID uuid = player.getUniqueId();

        giganteAte.put(uuid, agora() + DURACAO_PASSOS_GIGANTES_MS);
        alterarEscalaGigante(player, 2.35);

        aplicarEfeito(player, DURACAO_PASSOS_GIGANTES_TICKS, 2, "DAMAGE_RESISTANCE", "RESISTANCE");
        aplicarEfeito(player, DURACAO_PASSOS_GIGANTES_TICKS, 1, "INCREASE_DAMAGE", "STRENGTH");
        aplicarEfeito(player, DURACAO_PASSOS_GIGANTES_TICKS, 0, "SLOW", "SLOWNESS");

        tocarSom(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.65f);
        tocarSom(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.25f, 0.50f);
        tocarSom(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.65f, 0.70f);

        iniciarRotinaPassosGigantes(player);

        return true;
    }

    private boolean usarEsmagadaGigante(Player player) {
        long cooldownAte = esmagadaGiganteCooldownAte.getOrDefault(player.getUniqueId(), 0L);

        if (cooldownAte > agora()) {
            double restante = (cooldownAte - agora()) / 1000.0;
            player.sendMessage("§cEsmagada Gigante em recarga: §f" + String.format(Locale.US, "%.1f", restante) + "s");
            return true;
        }

        esmagadaGiganteCooldownAte.put(player.getUniqueId(), agora() + COOLDOWN_ESMAGADA_GIGANTE_MS);

        Location centro = ajustarCentroNoChao(player.getLocation());

        tocarSom(centro, Sound.BLOCK_ANVIL_LAND, 1.25f, 0.42f);
        tocarSom(centro, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.72f);
        tocarSom(centro, Sound.BLOCK_STONE_BREAK, 1.0f, 0.55f);

        desenharImpactoQuebraChaoVisual(centro, RAIO_ESMAGADA_GIGANTE);
        lancarFragmentosGigante(centro, RAIO_ESMAGADA_GIGANTE);

        for (LivingEntity alvo : inimigosProximos(player, centro, RAIO_ESMAGADA_GIGANTE)) {
            causarDano(player, alvo, 8.5);
            aplicarEfeito(alvo, 60, 1, "SLOW", "SLOWNESS");

            Vector direcao = alvo.getLocation().toVector().subtract(centro.toVector());

            if (direcao.lengthSquared() <= 0.01) {
                direcao = new Vector(0, 0, 1);
            }

            direcao.normalize().multiply(0.55);
            direcao.setY(0.95);

            alvo.setVelocity(direcao);
            atordoar(alvo, 10);
        }

        return true;
    }

    private void iniciarRotinaPassosGigantes(Player player) {
        UUID uuid = player.getUniqueId();

        cancelarRotinaPassosGigantes(uuid);

        BukkitTask task = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    passosGigantesAtivos.remove(uuid);
                    giganteAte.remove(uuid);
                    restaurarEscalaGigante(player);
                    cancel();
                    return;
                }

                Long fim = giganteAte.get(uuid);

                if (fim == null || fim <= agora() || ticks >= DURACAO_PASSOS_GIGANTES_TICKS) {
                    passosGigantesAtivos.remove(uuid);
                    finalizarPassosGigantes(player, true);
                    cancel();
                    return;
                }

                Location centro = player.getLocation();

                desenharPressaoPassosGigantesVisual(centro, RAIO_PRESSAO_PASSOS_GIGANTES, ticks);

                if (ticks % 10 == 0) {
                    aplicarPressaoPassosGigantes(player, centro);
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        passosGigantesAtivos.put(uuid, task);
    }

    private void aplicarPressaoPassosGigantes(Player player, Location centro) {
        for (LivingEntity alvo : inimigosProximos(player, centro, RAIO_PRESSAO_PASSOS_GIGANTES)) {
            aplicarEfeito(alvo, 30, 1, "SLOW", "SLOWNESS");

            Vector velocidade = alvo.getVelocity();
            velocidade.setX(velocidade.getX() * 0.62);
            velocidade.setZ(velocidade.getZ() * 0.62);
            alvo.setVelocity(velocidade);
        }
    }

    private void desenharPressaoPassosGigantesVisual(Location centroOriginal, double raio, int tick) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.08, 0);
        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions cinzaPesado = new Particle.DustOptions(
                Color.fromRGB(105, 100, 95),
                1.25f
        );

        Particle.DustOptions douradoBaixo = new Particle.DustOptions(
                Color.fromRGB(215, 180, 80),
                0.95f
        );

        double rotacao = tick * 0.035;

        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 30) {
            double x = Math.cos(angulo + rotacao) * raio;
            double z = Math.sin(angulo + rotacao) * raio;

            world.spawnParticle(
                    particula,
                    centro.clone().add(x, 0.02, z),
                    1,
                    0,
                    0,
                    0,
                    0,
                    cinzaPesado
            );
        }

        if (tick % 6 == 0) {
            world.spawnParticle(
                    particula,
                    centro,
                    14,
                    raio * 0.32,
                    0.02,
                    raio * 0.32,
                    0.0,
                    douradoBaixo
            );

            world.spawnParticle(
                    Particle.CLOUD,
                    centro,
                    8,
                    raio * 0.35,
                    0.03,
                    raio * 0.35,
                    0.006
            );
        }
    }

    private void lancarFragmentosGigante(Location centroOriginal, double raio) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        int quantidade = 30;

        for (int i = 0; i < quantidade; i++) {
            double angulo = ((Math.PI * 2) / quantidade) * i;
            double distancia = 0.45 + ((i % 7) * (raio / 7.5));

            Location origem = centroOriginal.clone().add(
                    Math.cos(angulo) * distancia,
                    0.15,
                    Math.sin(angulo) * distancia
            );

            BlockData data = obterBlocoVisualDoChao(origem);

            FallingBlock fragmento = world.spawnFallingBlock(origem, data);

            fragmento.setDropItem(false);
            fragmento.setHurtEntities(false);
            fragmento.setGravity(false);
            fragmento.setInvulnerable(true);
            fragmento.setPersistent(false);

            double forcaHorizontal = 0.10 + ((i % 5) * 0.025);
            double forcaVertical = 0.30 + ((i % 6) * 0.035);

            Vector velocidade = new Vector(
                    Math.cos(angulo) * forcaHorizontal,
                    forcaVertical,
                    Math.sin(angulo) * forcaHorizontal
            );

            animarFragmentoFalsoGigante(fragmento, velocidade, 15 + (i % 8));
        }
    }

    private BlockData obterBlocoVisualDoChao(Location origem) {
        for (int i = 0; i <= 5; i++) {
            Block bloco = origem.clone().subtract(0, i, 0).getBlock();
            Material material = bloco.getType();

            if (material != Material.AIR && material.isBlock() && material.isSolid()) {
                return bloco.getBlockData();
            }
        }

        return Material.STONE.createBlockData();
    }

    private void animarFragmentoFalsoGigante(FallingBlock fragmento, Vector velocidadeInicial, int duracaoTicks) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final Vector velocidade = velocidadeInicial.clone();

            @Override
            public void run() {
                if (!fragmento.isValid() || fragmento.isDead()) {
                    cancel();
                    return;
                }

                if (ticks >= duracaoTicks) {
                    fragmento.remove();
                    cancel();
                    return;
                }

                Location proxima = fragmento.getLocation().add(velocidade);
                fragmento.teleport(proxima);

                velocidade.setY(velocidade.getY() - 0.025);
                velocidade.multiply(0.97);

                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void finalizarPassosGigantes(Player player, boolean tocarSomFinal) {
        UUID uuid = player.getUniqueId();

        giganteAte.remove(uuid);
        cancelarRotinaPassosGigantes(uuid);
        restaurarEscalaGigante(player);

        if (!tocarSomFinal || !player.isOnline()) {
            return;
        }

        Location centro = player.getLocation();

        tocarSom(centro, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.65f);
        tocarSom(centro, Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.9f, 0.60f);

        particulas(
                centro.clone().add(0, 1.0, 0),
                Particle.CLOUD,
                35,
                0.75,
                0.8,
                0.75,
                0.025
        );
    }

    private void cancelarRotinaPassosGigantes(UUID uuid) {
        BukkitTask task = passosGigantesAtivos.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }

    private void alterarEscalaGigante(Player player, double escala) {
        org.bukkit.attribute.Attribute atributoEscala = obterAtributoEscala();

        if (atributoEscala == null) {
            return;
        }

        org.bukkit.attribute.AttributeInstance atributo = player.getAttribute(atributoEscala);

        if (atributo == null) {
            return;
        }

        escalaOriginalGigante.putIfAbsent(player.getUniqueId(), atributo.getBaseValue());
        atributo.setBaseValue(escala);
    }

    private void restaurarEscalaGigante(Player player) {
        Double escalaOriginal = escalaOriginalGigante.remove(player.getUniqueId());

        if (escalaOriginal == null) {
            return;
        }

        org.bukkit.attribute.Attribute atributoEscala = obterAtributoEscala();

        if (atributoEscala == null) {
            return;
        }

        org.bukkit.attribute.AttributeInstance atributo = player.getAttribute(atributoEscala);

        if (atributo != null) {
            atributo.setBaseValue(escalaOriginal);
        }
    }

    private org.bukkit.attribute.Attribute obterAtributoEscala() {
        try {
            return org.bukkit.attribute.Attribute.valueOf("SCALE");
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean usarMassacre(Player player) {
        if (!iniciarCooldown(player, MASSACRE, 28.54)) {
            return true;
        }

        Vector direcao = direcaoHorizontal(player);

        tocarSom(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.6f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > 24) {
                    impactoMassacre(player);
                    cancel();
                    return;
                }

                ticks++;

                player.setVelocity(direcao.clone().multiply(0.9).setY(0.08));

                particulas(
                        player.getLocation(),
                        Particle.CRIT,
                        12,
                        1.0,
                        0.2,
                        1.0,
                        0.02
                );

                for (LivingEntity alvo : inimigosProximos(player, player.getLocation(), 3.5)) {
                    puxarPara(alvo, player.getLocation(), 0.95);

                    if (ticks % 4 == 0) {
                        causarDano(player, alvo, 2.0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void impactoMassacre(Player player) {
        Location centro = player.getLocation();

        tocarSom(centro, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.55f);
        desenharCirculo(centro, 5.0, Particle.CLOUD);

        for (LivingEntity alvo : inimigosProximos(player, centro, 5.0)) {
            causarDano(player, alvo, 9.5);
            alvo.setVelocity(new Vector(0, 1.1, 0));
            atordoar(alvo, 10);
        }
    }

    private boolean usarHiperestatica(Player player) {
        if (!iniciarCooldown(player, HIPERESTATICA, 23.78)) {
            return true;
        }

        Location inicio = player.getLocation().add(0, 0.2, 0);
        Location fim = pontoAlvo(player, 15.0);

        tocarSom(inicio, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.6f);
        desenharLinha(inicio, fim, Particle.CRIT);

        for (Player aliado : aliadosProximos(player, fim, 6.0)) {
            aplicarEfeito(aliado, 80, 2, "ABSORPTION");
        }

        for (LivingEntity alvo : inimigosProximos(player, fim, 6.0)) {
            double distancia = distanciaDaLinha(inicio, fim, alvo.getLocation());

            if (distancia > 2.4) {
                continue;
            }

            causarDano(player, alvo, 7.0);

            int stacks = stacksEstaticos.getOrDefault(alvo.getUniqueId(), 0) + 1;
            stacksEstaticos.put(alvo.getUniqueId(), stacks);

            aplicarEfeito(alvo, 120, 0, "GLOWING");

            if (stacks >= 5) {
                stacksEstaticos.remove(alvo.getUniqueId());
                atordoar(alvo, 88);
                tocarSom(alvo.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, 1.3f);
            }

            UUID uuid = alvo.getUniqueId();

            new BukkitRunnable() {
                @Override
                public void run() {
                    stacksEstaticos.remove(uuid);
                }
            }.runTaskLater(plugin, 120L);
        }

        return true;
    }

    private void ativarBatidaDefensiva(Player player) {
        Location centro = player.getLocation();

        tocarSom(centro, Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.2f);

        int afetados = 0;

        for (Player aliado : aliadosProximos(player, centro, 8.0)) {
            if (afetados >= 11) {
                break;
            }

            afetados++;

            aplicarEfeito(aliado, 80, 1, "DAMAGE_RESISTANCE", "RESISTANCE");

            // Representa resistência a controle enquanto não existe um sistema próprio de CC.
            aplicarEfeito(aliado, 80, 0, "SPEED");
        }
    }

    private void ativarAuraDebilitante(Player player) {
        if (!passivaCooldown(player, AURA_DEBILITANTE, 28.54)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        cancelarAuraDebilitante(uuid);

        tocarSom(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.4f);

        BukkitTask task = new BukkitRunnable() {
            private int ticks = 0;
            private final int duracaoTicks = 100;
            private final double raio = 7.0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || ticks >= duracaoTicks) {
                    cancelarAuraDebilitante(uuid);
                    cancel();
                    return;
                }

                Location centro = player.getLocation();

                desenharAuraDebilitanteVisual(centro, raio, ticks);

                if (ticks % 10 == 0) {
                    for (LivingEntity alvo : inimigosProximos(player, centro, raio)) {
                        aplicarReducaoDanoCausado(alvo, 0.40, 40);
                        aplicarEfeito(alvo, 40, 1, "SLOW", "SLOWNESS");
                        aplicarEfeito(alvo, 40, 0, "GLOWING");
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        aurasDebilitantes.put(uuid, task);
    }

    private void ativarEscudoDeForca(Player player) {
        if (!passivaCooldown(player, ESCUDO_DE_FORCA, 47.56)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        cancelarEscudoDeForca(uuid);

        Location centroFixo = player.getLocation().clone().add(0, 0.05, 0);
        double raio = 7.0;

        tocarSom(centroFixo, Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final int duracaoTicks = 160;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= duracaoTicks) {
                    cancelarEscudoDeForca(uuid);
                    cancel();
                    return;
                }

                desenharEscudoDeForcaVisual(centroFixo, raio, ticks);

                if (ticks % 20 == 0) {
                    for (Player aliado : aliadosProximos(player, centroFixo, raio)) {
                        aplicarEfeito(aliado, 60, 1, "DAMAGE_RESISTANCE", "RESISTANCE");
                        aplicarEfeito(aliado, 60, 1, "REGENERATION");
                        aplicarEfeito(aliado, 60, 1, "ABSORPTION");
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        escudosDeForcaAtivos.put(uuid, task);
    }

    private void desenharEscudoDeForcaVisual(Location centroOriginal, double raio, int tick) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone();

        Particle particulaColorida = particulaPoeiraColorida();

        Particle.DustOptions verdeBase = new Particle.DustOptions(
                Color.fromRGB(140, 255, 120),
                1.35f
        );

        Particle.DustOptions azulCupula = new Particle.DustOptions(
                Color.fromRGB(170, 210, 235),
                0.85f
        );

        Particle.DustOptions azulInterno = new Particle.DustOptions(
                Color.fromRGB(190, 230, 245),
                0.65f
        );

        desenharAnelEscudo(world, centro, raio, particulaColorida, verdeBase);
        desenharCupulaEscudo(world, centro, raio, tick, particulaColorida, azulCupula);
        desenharNeblinaEscudo(world, centro, raio, tick, particulaColorida, azulInterno);
    }

    private void desenharAnelEscudo(
            World world,
            Location centro,
            double raio,
            Particle particula,
            Particle.DustOptions cor
    ) {
        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 80) {
            double x = Math.cos(angulo) * raio;
            double z = Math.sin(angulo) * raio;

            Location ponto = centro.clone().add(x, 0.03, z);

            world.spawnParticle(
                    particula,
                    ponto,
                    1,
                    0,
                    0,
                    0,
                    0,
                    cor
            );
        }
    }

    private void desenharCupulaEscudo(
            World world,
            Location centro,
            double raio,
            int tick,
            Particle particula,
            Particle.DustOptions cor
    ) {
        double rotacao = tick * 0.03;

        for (double phi = 0.25; phi <= (Math.PI / 2); phi += Math.PI / 10) {
            double raioCamada = Math.cos(phi) * raio;
            double altura = Math.sin(phi) * raio;

            for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 24) {
                double angulo = theta + rotacao * (0.2 + phi);

                double x = Math.cos(angulo) * raioCamada;
                double z = Math.sin(angulo) * raioCamada;

                Location ponto = centro.clone().add(x, altura, z);

                world.spawnParticle(
                        particula,
                        ponto,
                        1,
                        0,
                        0,
                        0,
                        0,
                        cor
                );
            }
        }
    }

    private void desenharNeblinaEscudo(
            World world,
            Location centro,
            double raio,
            int tick,
            Particle particula,
            Particle.DustOptions cor
    ) {
        if (tick % 2 != 0) {
            return;
        }

        world.spawnParticle(
                particula,
                centro.clone().add(0, 0.25, 0),
                18,
                raio * 0.42,
                0.18,
                raio * 0.42,
                0.01,
                cor
        );

        world.spawnParticle(
                Particle.CLOUD,
                centro.clone().add(0, 0.20, 0),
                10,
                raio * 0.35,
                0.10,
                raio * 0.35,
                0.003
        );
    }

    private void cancelarEscudoDeForca(UUID uuid) {
        BukkitTask task = escudosDeForcaAtivos.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }

    private void iniciarIndicadorResistencia(Player alvo, int nivel, int duracaoTicks) {
        UUID uuid = alvo.getUniqueId();

        removerIndicadorResistencia(uuid);

        String texto = gerarTextoEscudos(nivel);

        TextDisplay indicador = alvo.getWorld().spawn(alvo.getLocation(), TextDisplay.class, display -> {
            display.setText(texto);
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(false);
            display.setPersistent(false);
            display.setInvulnerable(true);
            display.setSilent(true);

            display.setTransformation(
                    new Transformation(
                            new Vector3f(0.0f, 1.05f, 0.0f),
                            new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f),
                            new Vector3f(2.0f, 2.0f, 2.0f) ,
                            new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f)
                    )
            );
        });

        alvo.addPassenger(indicador);
        indicadoresResistencia.put(uuid, indicador);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!alvo.isOnline() || alvo.isDead() || ticks >= duracaoTicks) {
                    removerIndicadorResistencia(uuid);
                    cancel();
                    return;
                }

                TextDisplay atual = indicadoresResistencia.get(uuid);

                if (atual == null || !atual.isValid()) {
                    indicadoresResistencia.remove(uuid);
                    cancel();
                    return;
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private String gerarTextoEscudos(int nivel) {
        int quantidade = Math.max(1, nivel);
        quantidade = Math.min(quantidade, 5);

        StringBuilder texto = new StringBuilder();

        for (int i = 0; i < quantidade; i++) {
            texto.append(ChatColor.DARK_BLUE).append("⛨");
        }

        return texto.toString();
    }

    private void removerIndicadorResistencia(UUID uuid) {
        TextDisplay indicador = indicadoresResistencia.remove(uuid);

        if (indicador == null || !indicador.isValid()) {
            return;
        }

        Entity vehicle = indicador.getVehicle();

        if (vehicle != null) {
            vehicle.removePassenger(indicador);
        }

        indicador.remove();
    }

    private boolean ehTanque(Player player) {
        return classeResolver.obterClasse(player) == ClasseTipo.TANQUE;
    }

    private boolean passivaEquipada(Player player, String id) {
        HabilidadeLoadout loadout = habilidadeService.getLoadout(player.getUniqueId());

        if (loadout == null || loadout.getPassiva() == null) {
            return false;
        }

        return loadout.getPassiva().equalsIgnoreCase(id);
    }

    private boolean iniciarCooldown(Player player, String habilidadeId, double segundos) {
        double cooldownFinal = MODO_TESTE_COOLDOWNS ? COOLDOWN_TESTE_SEGUNDOS : segundos;

        long agora = agora();

        Map<String, Long> cooldownsDoJogador = cooldowns.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new HashMap<>()
        );

        long fim = cooldownsDoJogador.getOrDefault(habilidadeId, 0L);

        if (fim > agora) {
            double restante = (fim - agora) / 1000.0;
            player.sendMessage("§cHabilidade em recarga: §f" + String.format(Locale.US, "%.1f", restante) + "s");
            return false;
        }

        cooldownsDoJogador.put(habilidadeId, agora + (long) (cooldownFinal * 1000.0));

        return true;
    }

    private boolean passivaCooldown(Player player, String passivaId, double segundos) {
        double cooldownFinal = MODO_TESTE_COOLDOWNS ? COOLDOWN_TESTE_SEGUNDOS : segundos;

        Map<String, Long> cooldownsDoJogador = cooldowns.computeIfAbsent(
                player.getUniqueId(),
                id -> new HashMap<>()
        );

        long fim = cooldownsDoJogador.getOrDefault(passivaId, 0L);

        if (fim > agora()) {
            return false;
        }

        cooldownsDoJogador.put(passivaId, agora() + (long) (cooldownFinal * 1000.0));

        return true;
    }

    private boolean estaGigante(Player player) {
        Long fim = giganteAte.get(player.getUniqueId());

        if (fim == null) {
            return false;
        }

        if (fim <= agora()) {
            finalizarPassosGigantes(player, true);
            return false;
        }

        return true;
    }

    private boolean estaImune(Player player) {
        Long fim = imuneAte.get(player.getUniqueId());

        if (fim == null) {
            return false;
        }

        if (fim <= agora()) {
            imuneAte.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private boolean estaAtordoado(Player player) {
        Long fim = stunnedAte.get(player.getUniqueId());

        if (fim == null) {
            return false;
        }

        if (fim <= agora()) {
            stunnedAte.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private void atordoar(LivingEntity alvo, int ticks) {
        aplicarEfeito(alvo, ticks, 9, "SLOW", "SLOWNESS");
        aplicarEfeito(alvo, ticks, 1, "BLINDNESS");

        alvo.setVelocity(new Vector(0, 0, 0));

        if (alvo instanceof Creature creature) {
            creature.setTarget(null);
            creature.setAI(false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (creature.isValid() && !creature.isDead()) {
                        creature.setAI(true);
                    }
                }
            }.runTaskLater(plugin, ticks);
        }

        if (alvo instanceof Player player) {
            stunnedAte.put(player.getUniqueId(), agora() + ticks * 50L);
        }
    }

    private void enraizar(LivingEntity alvo, int ticks) {
        aplicarEfeito(alvo, ticks, 7, "SLOW", "SLOWNESS");

        if (alvo instanceof Player player) {
            stunnedAte.put(player.getUniqueId(), agora() + ticks * 50L);
        }
    }

    private void aplicarReducaoDanoCausado(LivingEntity entidade, double multiplicador, int ticks) {
        UUID uuid = entidade.getUniqueId();
        long fim = agora() + ticks * 50L;

        ReducaoDano atual = reducoesDeDanoCausado.get(uuid);

        if (atual == null || multiplicador < atual.getMultiplicador() || fim > atual.getFim()) {
            reducoesDeDanoCausado.put(uuid, new ReducaoDano(fim, multiplicador));
        }
    }

    private void causarDano(Player caster, LivingEntity alvo, double dano) {
        if (alvo.isDead() || !alvo.isValid()) {
            return;
        }

        danoDeHabilidade.add(caster.getUniqueId());

        try {
            alvo.damage(dano, caster);
        } finally {
            danoDeHabilidade.remove(caster.getUniqueId());
        }
    }

    private void curar(Player player, double quantidade) {
        double novaVida = Math.min(player.getMaxHealth(), player.getHealth() + quantidade);
        player.setHealth(novaVida);
    }

    private void aplicarEfeito(LivingEntity entidade, int ticks, int amplificador, String... nomes) {
        PotionEffectType tipo = efeito(nomes);

        if (tipo == null) {
            return;
        }

        entidade.addPotionEffect(new PotionEffect(
                tipo,
                ticks,
                amplificador,
                false,
                true,
                true
        ));

        if (entidade instanceof Player player && ehEfeitoResistencia(tipo)) {
            int nivel = amplificador + 1;
            iniciarIndicadorResistencia(player, nivel, ticks);
        }
    }

    private boolean ehEfeitoResistencia(PotionEffectType tipo) {
        String nome = tipo.getName();

        return nome.equalsIgnoreCase("DAMAGE_RESISTANCE")
                || nome.equalsIgnoreCase("RESISTANCE");
    }

    private PotionEffectType efeito(String... nomes) {
        for (String nome : nomes) {
            PotionEffectType tipo = PotionEffectType.getByName(nome);

            if (tipo != null) {
                return tipo;
            }
        }

        return null;
    }

    private LivingEntity obterCausador(Entity entidade) {
        if (entidade instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        if (entidade instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }

        return null;
    }

    private LivingEntity primeiroAlvoNaLinha(Player player, double alcance, double raio) {
        Location inicio = player.getEyeLocation();
        Vector direcao = inicio.getDirection().normalize();

        for (double distancia = 0.5; distancia <= alcance; distancia += 0.5) {
            Location ponto = inicio.clone().add(direcao.clone().multiply(distancia));

            for (LivingEntity alvo : inimigosProximos(player, ponto, raio)) {
                return alvo;
            }
        }

        return null;
    }

    private List<LivingEntity> alvosEmCone(Player player, double alcance, double anguloGraus) {
        List<LivingEntity> resultado = new ArrayList<>();

        Vector direcao = direcaoHorizontal(player);
        double anguloRad = Math.toRadians(anguloGraus / 2.0);

        for (LivingEntity alvo : inimigosProximos(player, player.getLocation(), alcance)) {
            Vector ateAlvo = alvo.getLocation().toVector().subtract(player.getLocation().toVector());
            ateAlvo.setY(0);

            if (ateAlvo.lengthSquared() <= 0.01) {
                continue;
            }

            if (direcao.angle(ateAlvo.normalize()) <= anguloRad) {
                resultado.add(alvo);
            }
        }

        return resultado;
    }

    private List<LivingEntity> inimigosProximos(Player caster, Location centro, double raio) {
        List<LivingEntity> resultado = new ArrayList<>();
        World world = centro.getWorld();

        if (world == null) {
            return resultado;
        }

        for (Entity entity : world.getNearbyEntities(centro, raio, raio, raio)) {
            if (!(entity instanceof LivingEntity alvo)) {
                continue;
            }

            if (!alvoValido(caster, alvo)) {
                continue;
            }

            if (alvo.getLocation().distanceSquared(centro) <= raio * raio) {
                resultado.add(alvo);
            }
        }

        return resultado;
    }

    private List<Player> aliadosProximos(Player caster, Location centro, double raio) {
        List<Player> resultado = new ArrayList<>();
        World world = centro.getWorld();

        if (world == null) {
            return resultado;
        }

        for (Entity entity : world.getNearbyEntities(centro, raio, raio, raio)) {
            if (entity instanceof Player player) {
                if (player.getLocation().distanceSquared(centro) <= raio * raio) {
                    resultado.add(player);
                }
            }
        }

        if (!resultado.contains(caster)) {
            resultado.add(caster);
        }

        return resultado;
    }

    private boolean alvoValido(Player caster, LivingEntity alvo) {
        if (alvo.equals(caster)) {
            return false;
        }

        if (alvo.isDead() || !alvo.isValid()) {
            return false;
        }

        return !(alvo instanceof ArmorStand);
    }

    private Location pontoAlvo(Player player, double alcance) {
        Block bloco = player.getTargetBlockExact((int) Math.ceil(alcance));

        if (bloco != null && bloco.getType() != Material.AIR) {
            return bloco.getLocation().add(0.5, 1.0, 0.5);
        }

        Location olho = player.getEyeLocation();

        return olho.clone().add(olho.getDirection().normalize().multiply(alcance));
    }

    private Vector direcaoHorizontal(Player player) {
        Vector direcao = player.getLocation().getDirection();
        direcao.setY(0);

        if (direcao.lengthSquared() <= 0.01) {
            return new Vector(0, 0, 1);
        }

        return direcao.normalize();
    }

    private void puxarPara(Entity entity, Location destino, double forca) {
        Vector vetor = destino.toVector().subtract(entity.getLocation().toVector());

        if (vetor.lengthSquared() <= 0.01) {
            return;
        }

        entity.setVelocity(vetor.normalize().multiply(forca).setY(0.25));
    }

    private void empurrarParaFora(Entity entity, Location centro, double forca) {
        Vector vetor = entity.getLocation().toVector().subtract(centro.toVector());

        if (vetor.lengthSquared() <= 0.01) {
            vetor = new Vector(0, 0, 1);
        }

        entity.setVelocity(vetor.normalize().multiply(forca).setY(0.35));
    }

    private void desenharAuraDebilitanteVisual(Location centroOriginal, double raio, int tick) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.08, 0);

        Particle particulaPoeira = particulaPoeiraColorida();

        Particle.DustOptions roxoForte = new Particle.DustOptions(
                Color.fromRGB(170, 55, 255),
                1.45f
        );

        Particle.DustOptions roxoClaro = new Particle.DustOptions(
                Color.fromRGB(230, 110, 255),
                1.15f
        );

        double rotacao = tick * 0.045;

        desenharCirculoRoxo(world, centro, raio, rotacao, particulaPoeira, roxoForte);
        desenharNuvemRoxaInterna(world, centro, raio, tick, particulaPoeira, roxoClaro);
        desenharOndasRoxas(world, centro, raio, tick, particulaPoeira, roxoClaro);
    }

    private void desenharCirculoRoxo(
            World world,
            Location centro,
            double raio,
            double rotacao,
            Particle particula,
            Particle.DustOptions cor
    ) {
        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 64) {
            double x = Math.cos(angulo + rotacao) * raio;
            double z = Math.sin(angulo + rotacao) * raio;

            Location ponto = centro.clone().add(x, 0.02, z);

            world.spawnParticle(
                    particula,
                    ponto,
                    1,
                    0,
                    0,
                    0,
                    0,
                    cor
            );
        }
    }

    private void desenharNuvemRoxaInterna(
            World world,
            Location centro,
            double raio,
            int tick,
            Particle particula,
            Particle.DustOptions cor
    ) {
        if (tick % 2 != 0) {
            return;
        }

        world.spawnParticle(
                particula,
                centro.clone().add(0, 0.12, 0),
                34,
                raio * 0.42,
                0.03,
                raio * 0.42,
                0.025,
                cor
        );

        world.spawnParticle(
                Particle.CLOUD,
                centro.clone().add(0, 0.10, 0),
                10,
                raio * 0.35,
                0.02,
                raio * 0.35,
                0.01
        );
    }

    private void desenharOndasRoxas(
            World world,
            Location centro,
            double raio,
            int tick,
            Particle particula,
            Particle.DustOptions cor
    ) {
        double tempo = tick * 0.08;

        for (double r = 1.2; r <= raio - 1.0; r += 1.1) {
            double angulo = tempo + (r * 0.85);

            double x = Math.cos(angulo) * r;
            double z = Math.sin(angulo) * r;

            Location ponto = centro.clone().add(x, 0.08, z);

            world.spawnParticle(
                    particula,
                    ponto,
                    2,
                    0.08,
                    0.01,
                    0.08,
                    0,
                    cor
            );
        }
    }

    private Particle particulaPoeiraColorida() {
        try {
            return Particle.valueOf("DUST");
        } catch (IllegalArgumentException ignored) {
            return Particle.valueOf("REDSTONE");
        }
    }

    private void cancelarAuraDebilitante(UUID uuid) {
        BukkitTask task = aurasDebilitantes.remove(uuid);

        if (task != null) {
            task.cancel();
        }
    }

    private Location calcularDestinoInvestida(
            Player player,
            Location inicio,
            Vector direcao,
            double distancia
    ) {
        World world = inicio.getWorld();

        if (world == null) {
            return inicio;
        }

        Location destinoSeguro = inicio.clone();

        for (double d = 0.5; d <= distancia; d += 0.5) {
            Location tentativa = inicio.clone().add(direcao.clone().multiply(d));

            if (tentativa.getBlock().getType().isSolid()
                    || tentativa.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                break;
            }

            destinoSeguro = tentativa;
        }

        return destinoSeguro;
    }

    private void desenharRastroBrancoInvestida(Location posicao) {
        World world = posicao.getWorld();

        if (world == null) {
            return;
        }

        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions branco = new Particle.DustOptions(
                Color.fromRGB(245, 245, 255),
                1.15f
        );

        world.spawnParticle(
                particula,
                posicao.clone().add(0, 0.45, 0),
                10,
                0.35,
                0.20,
                0.35,
                0.01,
                branco
        );

        world.spawnParticle(
                Particle.CLOUD,
                posicao.clone().add(0, 0.35, 0),
                4,
                0.25,
                0.10,
                0.25,
                0.01
        );
    }

    private void impactoInvestidaArdilosa(Player player, Location centro) {
        double raio = 3.6;

        tocarSom(centro, Sound.ENTITY_GENERIC_EXPLODE, 0.45f, 1.65f);

        desenharCirculoRoxoInvestida(centro, raio);

        List<LivingEntity> alvos = inimigosProximos(player, centro, raio);

        if (!alvos.isEmpty()) {
            tocarSom(centro, Sound.BLOCK_CHAIN_HIT, 1.0f, 0.75f);
        }

        for (LivingEntity alvo : alvos) {
            causarDano(player, alvo, 6.0);
            enraizar(alvo, 74);
            iniciarParticulasEnraizado(alvo, 74);
        }
    }

    private void desenharCirculoRoxoInvestida(Location centroOriginal, double raio) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.08, 0);
        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions roxo = new Particle.DustOptions(
                Color.fromRGB(175, 55, 255),
                1.35f
        );

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 12) {
                    cancel();
                    return;
                }

                double raioAtual = raio * (ticks / 12.0);

                for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 32) {
                    double x = Math.cos(angulo) * raioAtual;
                    double z = Math.sin(angulo) * raioAtual;

                    Location ponto = centro.clone().add(x, 0.02, z);

                    world.spawnParticle(
                            particula,
                            ponto,
                            1,
                            0,
                            0,
                            0,
                            0,
                            roxo
                    );
                }

                world.spawnParticle(
                        particula,
                        centro,
                        8,
                        raio * 0.22,
                        0.02,
                        raio * 0.22,
                        0.01,
                        roxo
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void iniciarParticulasEnraizado(LivingEntity alvo, int duracaoTicks) {
        UUID uuid = alvo.getUniqueId();

        new BukkitRunnable() {
            private int ticks = 0;
            private double angulo = 0.0;

            @Override
            public void run() {
                if (alvo.isDead() || !alvo.isValid() || ticks >= duracaoTicks) {
                    cancel();
                    return;
                }

                World world = alvo.getWorld();
                Particle particula = particulaPoeiraColorida();

                Particle.DustOptions amarelo = new Particle.DustOptions(
                        Color.fromRGB(255, 225, 70),
                        1.15f
                );

                angulo += Math.PI / 8.0;

                for (int i = 0; i < 3; i++) {
                    double anguloParticula = angulo + ((Math.PI * 2.0) / 3.0) * i;

                    double x = Math.cos(anguloParticula) * 0.75;
                    double z = Math.sin(anguloParticula) * 0.75;
                    double y = 0.35 + (Math.sin(angulo + i) * 0.12);

                    Location ponto = alvo.getLocation().clone().add(x, y, z);

                    world.spawnParticle(
                            particula,
                            ponto,
                            1,
                            0,
                            0,
                            0,
                            0,
                            amarelo
                    );
                }

                if (ticks % 6 == 0) {
                    world.spawnParticle(
                            particula,
                            alvo.getLocation().clone().add(0, 0.08, 0),
                            6,
                            0.55,
                            0.02,
                            0.55,
                            0.0,
                            amarelo
                    );
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impactoQuebraChao(Player player, Location centro) {
        double raio = 4.0;

        tocarSom(centro, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.55f);
        tocarSom(centro, Sound.ENTITY_GENERIC_EXPLODE, 0.75f, 0.8f);

        desenharImpactoQuebraChaoVisual(centro, raio);
        lancarFragmentosQuebraChao(centro, raio);

        for (LivingEntity alvo : inimigosProximos(player, centro, raio)) {
            causarDano(player, alvo, 7.5);
            alvo.setVelocity(new Vector(0, 1.15, 0));
            atordoar(alvo, 8);
        }
    }

    private void desenharImpactoQuebraChaoVisual(Location centroOriginal, double raio) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.10, 0);

        Particle poeira = particulaPoeiraColorida();
        Particle fumaca = particulaPorNome("LARGE_SMOKE", "SMOKE_LARGE", "CAMPFIRE_COSY_SMOKE", "SMOKE");
        Particle explosao = particulaPorNome("EXPLOSION", "EXPLOSION_NORMAL", "EXPLOSION_LARGE");

        Particle.DustOptions amareloImpacto = new Particle.DustOptions(
                Color.fromRGB(255, 215, 70),
                1.45f
        );

        Particle.DustOptions laranjaImpacto = new Particle.DustOptions(
                Color.fromRGB(255, 135, 35),
                1.25f
        );

        Particle.DustOptions fumacaEscura = new Particle.DustOptions(
                Color.fromRGB(45, 36, 40),
                1.85f
        );

        world.spawnParticle(
                explosao,
                centro.clone().add(0, 0.35, 0),
                2,
                0.25,
                0.15,
                0.25,
                0.0
        );

        world.spawnParticle(
                poeira,
                centro.clone().add(0, 0.22, 0),
                55,
                0.85,
                0.15,
                0.85,
                0.03,
                amareloImpacto
        );

        new BukkitRunnable() {
            private int ticks = 0;
            private final int duracao = 18;

            @Override
            public void run() {
                if (ticks >= duracao) {
                    cancel();
                    return;
                }

                double progresso = ticks / (double) duracao;
                double raioAtual = raio * progresso;

                // Anel de fumaça escura expandindo.
                for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 18) {
                    double x = Math.cos(angulo) * raioAtual;
                    double z = Math.sin(angulo) * raioAtual;

                    Location ponto = centro.clone().add(x, 0.10, z);

                    world.spawnParticle(
                            fumaca,
                            ponto,
                            2,
                            0.20,
                            0.08,
                            0.20,
                            0.018
                    );

                    world.spawnParticle(
                            poeira,
                            ponto,
                            1,
                            0.08,
                            0.02,
                            0.08,
                            0,
                            fumacaEscura
                    );
                }

                // Núcleo dourado do impacto.
                if (ticks <= 8) {
                    world.spawnParticle(
                            poeira,
                            centro.clone().add(0, 0.18, 0),
                            18,
                            0.55,
                            0.08,
                            0.55,
                            0.02,
                            amareloImpacto
                    );

                    world.spawnParticle(
                            poeira,
                            centro.clone().add(0, 0.22, 0),
                            10,
                            0.35,
                            0.12,
                            0.35,
                            0.02,
                            laranjaImpacto
                    );
                }

                // Faíscas radiais saindo do centro.
                if (ticks % 2 == 0 && ticks <= 12) {
                    desenharFaíscasQuebraChao(world, centro, raio, ticks, poeira, amareloImpacto);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void desenharFaíscasQuebraChao(
            World world,
            Location centro,
            double raio,
            int tick,
            Particle particula,
            Particle.DustOptions cor
    ) {
        double distancia = 0.8 + tick * 0.22;

        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 5) {
            double x = Math.cos(angulo) * Math.min(distancia, raio);
            double z = Math.sin(angulo) * Math.min(distancia, raio);

            Location ponto = centro.clone().add(x, 0.16, z);

            world.spawnParticle(
                    particula,
                    ponto,
                    2,
                    0.05,
                    0.02,
                    0.05,
                    0,
                    cor
            );
        }
    }

    private void lancarFragmentosQuebraChao(Location centroOriginal, double raio) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.45, 0);

        Block blocoBase = centroOriginal.clone().subtract(0, 0.2, 0).getBlock();
        Material materialBase = blocoBase.getType();

        if (materialBase == Material.AIR || !materialBase.isBlock()) {
            materialBase = Material.STONE;
        }

        BlockData blocoChao = materialBase.createBlockData();
        BlockData blocoEscuro = Material.DEEPSLATE.createBlockData();

        int quantidade = 18;

        for (int i = 0; i < quantidade; i++) {
            double angulo = ((Math.PI * 2) / quantidade) * i;
            double distanciaInicial = 0.35 + ((i % 4) * 0.18);

            Location spawn = centro.clone().add(
                    Math.cos(angulo) * distanciaInicial,
                    0,
                    Math.sin(angulo) * distanciaInicial
            );

            BlockData data = i % 3 == 0 ? blocoEscuro : blocoChao;

            FallingBlock fragmento = world.spawnFallingBlock(spawn, data);

            fragmento.setDropItem(false);
            fragmento.setHurtEntities(false);
            fragmento.setGravity(true);
            fragmento.setInvulnerable(true);

            double forcaHorizontal = 0.32 + ((i % 5) * 0.045);
            double forcaVertical = 0.38 + ((i % 4) * 0.055);

            fragmento.setVelocity(new Vector(
                    Math.cos(angulo) * forcaHorizontal,
                    forcaVertical,
                    Math.sin(angulo) * forcaHorizontal
            ));

            removerFragmentoDepois(fragmento, 16 + (i % 6));
        }
    }

    private void removerFragmentoDepois(FallingBlock fragmento, int ticks) {
        new BukkitRunnable() {
            private int tempo = 0;

            @Override
            public void run() {
                if (!fragmento.isValid()) {
                    cancel();
                    return;
                }

                if (tempo >= ticks || fragmento.isOnGround()) {
                    fragmento.remove();
                    cancel();
                    return;
                }

                tempo++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private Location ajustarCentroNoChao(Location location) {
        World world = location.getWorld();

        if (world == null) {
            return location;
        }

        Location base = location.clone();

        for (int i = 0; i < 8; i++) {
            Location abaixo = base.clone().subtract(0, 1, 0);

            if (abaixo.getBlock().getType().isSolid()) {
                return new Location(
                        world,
                        base.getBlockX() + 0.5,
                        base.getBlockY(),
                        base.getBlockZ() + 0.5
                );
            }

            base.subtract(0, 1, 0);
        }

        return location;
    }

    private Particle particulaPorNome(String... nomes) {
        for (String nome : nomes) {
            try {
                return Particle.valueOf(nome);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Particle.CLOUD;
    }

    private void desenharCirculo(Location centro, double raio, Particle particle) {
        World world = centro.getWorld();

        if (world == null) {
            return;
        }

        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 16) {
            Location ponto = centro.clone().add(
                    Math.cos(angulo) * raio,
                    0.15,
                    Math.sin(angulo) * raio
            );

            world.spawnParticle(particle, ponto, 1, 0, 0, 0, 0);
        }
    }

    private void desenharLinha(Location inicio, Location fim, Particle particle) {
        World world = inicio.getWorld();

        if (world == null) {
            return;
        }

        Vector direcao = fim.toVector().subtract(inicio.toVector());
        double distancia = direcao.length();

        if (distancia <= 0.01) {
            return;
        }

        direcao.normalize();

        for (double d = 0; d <= distancia; d += 0.45) {
            Location ponto = inicio.clone().add(direcao.clone().multiply(d));
            world.spawnParticle(particle, ponto, 2, 0.05, 0.05, 0.05, 0);
        }
    }

    private void particulas(
            Location location,
            Particle particle,
            int quantidade,
            double offsetX,
            double offsetY,
            double offsetZ,
            double velocidade
    ) {
        World world = location.getWorld();

        if (world == null) {
            return;
        }

        world.spawnParticle(
                particle,
                location,
                quantidade,
                offsetX,
                offsetY,
                offsetZ,
                velocidade
        );
    }

    private void tocarSom(Location location, Sound sound, float volume, float pitch) {
        World world = location.getWorld();

        if (world == null) {
            return;
        }

        world.playSound(location, sound, volume, pitch);
    }

    private double distanciaDaLinha(Location inicio, Location fim, Location ponto) {
        Vector a = inicio.toVector();
        Vector b = fim.toVector();
        Vector p = ponto.toVector();

        Vector ab = b.clone().subtract(a);

        if (ab.lengthSquared() <= 0.01) {
            return ponto.distance(inicio);
        }

        double t = p.clone().subtract(a).dot(ab) / ab.lengthSquared();
        t = Math.max(0, Math.min(1, t));

        Vector maisProximo = a.clone().add(ab.multiply(t));

        return maisProximo.distance(p);
    }

    private void desenharRastroVinganca(Location ponto) {
        World world = ponto.getWorld();

        if (world == null) {
            return;
        }

        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions vermelhoEscuro = new Particle.DustOptions(
                Color.fromRGB(175, 20, 20),
                1.25f
        );

        Particle.DustOptions vermelhoVivo = new Particle.DustOptions(
                Color.fromRGB(255, 45, 45),
                1.05f
        );

        world.spawnParticle(
                particula,
                ponto,
                8,
                0.12,
                0.12,
                0.12,
                0.01,
                vermelhoEscuro
        );

        world.spawnParticle(
                particula,
                ponto,
                3,
                0.05,
                0.05,
                0.05,
                0.0,
                vermelhoVivo
        );

        world.spawnParticle(
                Particle.SMOKE,
                ponto,
                3,
                0.16,
                0.12,
                0.16,
                0.01
        );
    }

    private void impactoVinganca(Player caster, LivingEntity alvoPrincipal) {
        double raioInicial = 8.0;

        aplicarEfeito(alvoPrincipal, 30, 0, "GLOWING");

        tocarSom(alvoPrincipal.getLocation(), Sound.ENTITY_WITHER_HURT, 0.9f, 0.6f);

        new BukkitRunnable() {
            int ticks = 0;
            final int duracao = 24;

            @Override
            public void run() {
                if (!alvoPrincipal.isValid() || alvoPrincipal.isDead()) {
                    cancel();
                    return;
                }

                Location centroAtual = alvoPrincipal.getLocation().clone().add(0, 0.08, 0);

                double progresso = ticks / (double) duracao;
                double raioAtual = raioInicial * (1.0 - progresso);

                desenharCirculoFechandoVinganca(
                        centroAtual,
                        Math.max(raioAtual, 0.35)
                );

                if (ticks >= duracao) {
                    atrairEAtordoarVinganca(caster, alvoPrincipal, raioInicial);
                    cancel();
                    return;
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void atrairEAtordoarVinganca(
            Player caster,
            LivingEntity alvoPrincipal,
            double raio
    ) {
        if (!alvoPrincipal.isValid() || alvoPrincipal.isDead()) {
            return;
        }

        Location centroInicial = alvoPrincipal.getLocation().clone();

        List<LivingEntity> alvos = inimigosProximos(caster, centroInicial, raio);

        if (alvos.isEmpty()) {
            return;
        }

        int duracaoPuxao = 10;
        int duracaoAtordoamento = 80; // 4 segundos

        for (LivingEntity alvo : alvos) {
            alvo.setCollidable(false);
        }

        tocarSom(centroInicial, Sound.BLOCK_CHAIN_HIT, 1.0f, 0.65f);
        tocarSom(centroInicial, Sound.ENTITY_ENDERMAN_TELEPORT, 0.9f, 0.55f);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!alvoPrincipal.isValid() || alvoPrincipal.isDead()) {
                    restaurarColisaoVinganca(alvos);
                    cancel();
                    return;
                }

                Location centroAtual = alvoPrincipal.getLocation().clone().add(0, 0.15, 0);

                if (ticks < duracaoPuxao) {
                    for (LivingEntity alvo : alvos) {
                        if (!alvo.isValid() || alvo.isDead()) {
                            continue;
                        }

                        if (alvo.equals(alvoPrincipal)) {
                            alvo.setVelocity(new Vector(0, 0, 0));
                            continue;
                        }

                        puxarParaVinganca(alvo, centroAtual);
                        desenharCorrenteVinganca(alvo.getLocation().clone().add(0, 1.0, 0), centroAtual);
                    }

                    ticks++;
                    return;
                }

                tocarSom(centroAtual, Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.7f);

                for (LivingEntity alvo : alvos) {
                    if (!alvo.isValid() || alvo.isDead()) {
                        continue;
                    }

                    alvo.setVelocity(new Vector(0, 0, 0));

                    // Evita aquele efeito de passar do centro e ser jogado para fora.
                    if (!alvo.equals(alvoPrincipal)) {
                        aproximarDoCentroVinganca(alvo, centroAtual);
                    }

                    causarDano(caster, alvo, 6.5);
                    atordoar(alvo, duracaoAtordoamento);
                    iniciarParticulasAtordoamento(alvo, duracaoAtordoamento);
                }

                desenharExplosaoFinalVinganca(centroAtual);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        restaurarColisaoVinganca(alvos);
                    }
                }.runTaskLater(plugin, duracaoAtordoamento);

                cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void puxarParaVinganca(LivingEntity alvo, Location centro) {
        Vector vetor = centro.toVector().subtract(alvo.getLocation().toVector());
        vetor.setY(0);

        double distancia = vetor.length();

        if (distancia <= 0.35) {
            alvo.setVelocity(new Vector(0, 0, 0));
            return;
        }

        double forca = Math.min(1.35, 0.35 + distancia * 0.22);

        Vector velocidade = vetor.normalize().multiply(forca);
        velocidade.setY(0.08);

        alvo.setVelocity(velocidade);
    }

    private void aproximarDoCentroVinganca(LivingEntity alvo, Location centro) {
        Location atual = alvo.getLocation();

        double distancia = atual.distance(centro);

        if (distancia <= 0.85) {
            return;
        }

        Vector direcao = centro.toVector().subtract(atual.toVector());
        direcao.setY(0);

        if (direcao.lengthSquared() <= 0.01) {
            return;
        }

        Location novaPosicao = atual.clone().add(direcao.normalize().multiply(distancia - 0.85));
        novaPosicao.setY(atual.getY());
        novaPosicao.setYaw(atual.getYaw());
        novaPosicao.setPitch(atual.getPitch());

        alvo.teleport(novaPosicao);
        alvo.setVelocity(new Vector(0, 0, 0));
    }

    private void restaurarColisaoVinganca(List<LivingEntity> alvos) {
        for (LivingEntity alvo : alvos) {
            if (alvo != null && alvo.isValid() && !alvo.isDead()) {
                alvo.setCollidable(true);
            }
        }
    }

    private void desenharCorrenteVinganca(Location inicio, Location fim) {
        World world = inicio.getWorld();

        if (world == null) {
            return;
        }

        Vector direcao = fim.toVector().subtract(inicio.toVector());
        double distancia = direcao.length();

        if (distancia <= 0.01) {
            return;
        }

        direcao.normalize();

        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions vermelho = new Particle.DustOptions(
                Color.fromRGB(170, 20, 20),
                0.85f
        );

        for (double d = 0; d <= distancia; d += 0.55) {
            Location ponto = inicio.clone().add(direcao.clone().multiply(d));

            world.spawnParticle(
                    particula,
                    ponto,
                    1,
                    0.02,
                    0.02,
                    0.02,
                    0,
                    vermelho
            );

            if (((int) (d * 10)) % 3 == 0) {
                world.spawnParticle(
                        Particle.SMOKE,
                        ponto,
                        1,
                        0.04,
                        0.04,
                        0.04,
                        0.005
                );
            }
        }
    }

    private void desenharExplosaoFinalVinganca(Location centro) {
        World world = centro.getWorld();

        if (world == null) {
            return;
        }

        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions vermelho = new Particle.DustOptions(
                Color.fromRGB(220, 25, 25),
                1.4f
        );

        world.spawnParticle(
                particula,
                centro,
                45,
                1.0,
                0.25,
                1.0,
                0.03,
                vermelho
        );

        world.spawnParticle(
                Particle.SMOKE,
                centro,
                22,
                1.0,
                0.20,
                1.0,
                0.02
        );
    }

    private void desenharCirculoFechandoVinganca(Location centroOriginal, double raio) {
        World world = centroOriginal.getWorld();

        if (world == null) {
            return;
        }

        Location centro = centroOriginal.clone().add(0, 0.03, 0);
        Particle particula = particulaPoeiraColorida();

        Particle.DustOptions vermelhoBorda = new Particle.DustOptions(
                Color.fromRGB(160, 20, 20),
                1.35f
        );

        Particle.DustOptions vermelhoInterno = new Particle.DustOptions(
                Color.fromRGB(255, 50, 50),
                0.95f
        );

        for (double angulo = 0; angulo < Math.PI * 2; angulo += Math.PI / 30) {
            double x = Math.cos(angulo) * raio;
            double z = Math.sin(angulo) * raio;

            Location ponto = centro.clone().add(x, 0.02, z);

            world.spawnParticle(
                    particula,
                    ponto,
                    1,
                    0,
                    0,
                    0,
                    0,
                    vermelhoBorda
            );
        }

        world.spawnParticle(
                particula,
                centro,
                8,
                Math.max(raio * 0.18, 0.15),
                0.02,
                Math.max(raio * 0.18, 0.15),
                0.0,
                vermelhoInterno
        );

        world.spawnParticle(
                Particle.SMOKE,
                centro,
                4,
                Math.max(raio * 0.10, 0.08),
                0.02,
                Math.max(raio * 0.10, 0.08),
                0.01
        );
    }

    private void iniciarParticulasAtordoamento(LivingEntity alvo, int duracaoTicks) {
        new BukkitRunnable() {
            private int ticks = 0;
            private double angulo = 0.0;

            @Override
            public void run() {
                if (!alvo.isValid() || alvo.isDead() || ticks >= duracaoTicks) {
                    cancel();
                    return;
                }

                World world = alvo.getWorld();
                Particle particula = particulaPoeiraColorida();

                Particle.DustOptions amarelo = new Particle.DustOptions(
                        Color.fromRGB(255, 230, 70),
                        1.1f
                );

                angulo += Math.PI / 7.0;

                for (int i = 0; i < 3; i++) {
                    double a = angulo + (((Math.PI * 2) / 3) * i);

                    double x = Math.cos(a) * 0.6;
                    double z = Math.sin(a) * 0.6;
                    double y = 1.15 + Math.sin(a + ticks * 0.08) * 0.12;

                    Location ponto = alvo.getLocation().clone().add(x, y, z);

                    world.spawnParticle(
                            particula,
                            ponto,
                            1,
                            0,
                            0,
                            0,
                            0,
                            amarelo
                    );
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean estaNoChao(Player player) {
        if (player.isOnGround()) {
            return true;
        }

        Location abaixo = player.getLocation().clone().subtract(0, 0.08, 0);

        return abaixo.getBlock().getType().isSolid();
    }

    private long agora() {
        return System.currentTimeMillis();
    }

    private static class ReducaoDano {

        private final long fim;
        private final double multiplicador;

        private ReducaoDano(long fim, double multiplicador) {
            this.fim = fim;
            this.multiplicador = multiplicador;
        }

        public boolean ativo() {
            return System.currentTimeMillis() <= fim;
        }

        public long getFim() {
            return fim;
        }

        public double getMultiplicador() {
            return multiplicador;
        }
    }

    public boolean estaAtordoadoPublic(Player player) {
        return estaAtordoado(player);
    }
}
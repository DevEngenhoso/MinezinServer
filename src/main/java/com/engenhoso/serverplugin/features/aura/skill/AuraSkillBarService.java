package com.engenhoso.serverplugin.features.aura.skill;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class AuraSkillBarService {

    private static final String NICK_DONO_DAS_SKILLS = "Engenhoso";

    private static final int SLOT_AVANCO_SOMBRIO = 0;
    private static final int SLOT_ECO_SINGULARIDADE = 1;

    private static final long COOLDOWN_AVANCO_MS = 1_000L;
    private static final long COOLDOWN_ECO_SINGULARIDADE_MS = 1_000L;

    private static final double DISTANCIA_AVANCO = 10.0;

    private static final int DURACAO_TENTACULOS_TICKS = 20 * 5;
    private static final double RAIO_TENTACULOS = 8.0;
    private static final double RAIO_NUCLEO_TENTACULOS = 1.25;
    private static final double FORCA_PUXAO_TENTACULOS = 0.42;
    private static final double FORCA_EXPLOSAO_TENTACULOS = 3.00;
    private static final double FORCA_VERTICAL_EXPLOSAO_TENTACULOS = 2.00;

    private static final int TEMPO_IMPLOSAO_ECO_TICKS = 20;
    private static final int DURACAO_DOMO_ECO_TICKS = 20 * 10;
    private static final double RAIO_DOMO_ECO = 8.0;
    private static final double DANO_ECO_POR_SEGUNDO = 4.0;
    private static final int SEGUNDO_CONGELAMENTO_TOTAL_ECO = 7;

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private final NamespacedKey skillItemKey;
    private final NamespacedKey ecoProjectileKey;

    private final Map<UUID, EstadoHotbar> estados = new HashMap<>();
    private final Map<UUID, Map<Integer, Long>> cooldowns = new HashMap<>();
    private final Set<BukkitTask> tarefasAtivas = new HashSet<>();
    private final Set<Projectile> projeteisCongelados = new HashSet<>();
    private final Set<UUID> projeteisEcoProcessados = new HashSet<>();
    private final Map<LivingEntity, Boolean> estadoOriginalAi = new HashMap<>();

    private BukkitTask tarefaVisual;

    public AuraSkillBarService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.skillItemKey = new NamespacedKey(plugin, "aura_skill_bar_item");
        this.ecoProjectileKey = new NamespacedKey(plugin, "aura_eco_singularidade_projectile");
    }

    public void iniciarAtualizacaoVisual() {
        if (tarefaVisual != null) {
            return;
        }

        tarefaVisual = new BukkitRunnable() {
            @Override
            public void run() {
                atualizarJogadoresEmModoSkill();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void parar() {
        if (tarefaVisual != null) {
            tarefaVisual.cancel();
            tarefaVisual = null;
        }

        for (BukkitTask tarefa : tarefasAtivas) {
            if (tarefa != null) {
                tarefa.cancel();
            }
        }

        tarefasAtivas.clear();
        projeteisEcoProcessados.clear();
        restaurarProjeteisCongelados();
        restaurarAiDasCriaturas();

        for (Player jogador : Bukkit.getOnlinePlayers()) {
            if (estaEmModoSkill(jogador)) {
                desativarModoSkill(jogador);
            }
        }

        estados.clear();
    }

    public boolean podeAtivarModoSkill(Player jogador) {
        return jogador != null && jogador.getName().equalsIgnoreCase(NICK_DONO_DAS_SKILLS);
    }

    public boolean estaEmModoSkill(Player jogador) {
        return jogador != null && estados.containsKey(jogador.getUniqueId());
    }

    public boolean alternarModoSkill(Player jogador) {
        if (!podeAtivarModoSkill(jogador)) {
            return false;
        }

        if (estaEmModoSkill(jogador)) {
            desativarModoSkill(jogador);
            return true;
        }

        ativarModoSkill(jogador);
        return true;
    }

    public void ativarModoSkill(Player jogador) {
        if (!podeAtivarModoSkill(jogador) || estaEmModoSkill(jogador)) {
            return;
        }

        PlayerInventory inventario = jogador.getInventory();
        ItemStack[] hotbarOriginal = new ItemStack[9];

        for (int i = 0; i < 9; i++) {
            ItemStack item = inventario.getItem(i);
            hotbarOriginal[i] = item == null ? null : item.clone();
        }

        int slotOriginal = inventario.getHeldItemSlot();

        estados.put(jogador.getUniqueId(), new EstadoHotbar(hotbarOriginal, slotOriginal));

        aplicarHotbarDeSkills(jogador);

        inventario.setHeldItemSlot(0);
        jogador.updateInventory();

        jogador.playSound(jogador.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8f, 0.55f);
        enviarBarraDeAcao(jogador, montarTextoSkillBar(jogador));
    }

    public void desativarModoSkill(Player jogador) {
        if (jogador == null) {
            return;
        }

        EstadoHotbar estado = estados.remove(jogador.getUniqueId());

        if (estado == null) {
            return;
        }

        PlayerInventory inventario = jogador.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack item = estado.hotbarOriginal[i];
            inventario.setItem(i, item == null ? null : item.clone());
        }

        inventario.setHeldItemSlot(estado.slotOriginal);
        jogador.updateInventory();

        jogador.playSound(jogador.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.55f, 0.85f);
        enviarBarraDeAcao(jogador, "§7Hotbar normal restaurada.");
    }

    public void executarSkillSelecionada(Player jogador) {
        if (!estaEmModoSkill(jogador)) {
            return;
        }

        int slot = jogador.getInventory().getHeldItemSlot();

        if (slot == SLOT_AVANCO_SOMBRIO) {
            executarAvancoSombrio(jogador);
            return;
        }

        if (slot == SLOT_ECO_SINGULARIDADE) {
            executarEcoDaSingularidade(jogador);
            return;
        }

        enviarBarraDeAcao(jogador, "§8Esta skill ainda não despertou.");
        jogador.playSound(jogador.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.55f, 0.65f);
    }

    public boolean ehItemDaSkillBar(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        Byte valor = meta.getPersistentDataContainer().get(skillItemKey, PersistentDataType.BYTE);
        return valor != null && valor == (byte) 1;
    }

    public boolean ehProjetilEcoDaSingularidade(Entity entidade) {
        if (entidade == null) {
            return false;
        }

        Byte valor = entidade.getPersistentDataContainer().get(ecoProjectileKey, PersistentDataType.BYTE);
        return valor != null && valor == (byte) 1;
    }

    public void aoProjetilEcoDaSingularidadeAtingir(ProjectileHitEvent event) {
        Entity projetil = event.getEntity();

        if (!ehProjetilEcoDaSingularidade(projetil)) {
            return;
        }

        if (!registrarProcessamentoProjetilEco(projetil)) {
            return;
        }

        Player dono = null;

        if (projetil instanceof Projectile projectile && projectile.getShooter() instanceof Player jogador) {
            dono = jogador;
        }

        Location localImpacto = obterLocalImpacto(event);

        projetil.remove();

        iniciarEcoDaSingularidade(dono, localImpacto);
    }

    private void atualizarJogadoresEmModoSkill() {
        for (UUID uuid : new HashSet<>(estados.keySet())) {
            Player jogador = Bukkit.getPlayer(uuid);

            if (jogador == null || !jogador.isOnline()) {
                estados.remove(uuid);
                continue;
            }

            aplicarCooldownVisual(jogador);
            enviarBarraDeAcao(jogador, montarTextoSkillBar(jogador));
        }
    }

    private void aplicarHotbarDeSkills(Player jogador) {
        PlayerInventory inventario = jogador.getInventory();

        inventario.setItem(0, criarItemSkill(
                Material.BLACK_DYE,
                "§5§lAvanço Sombrio",
                "§7Teleporta e desperta tentáculos de tinta.",
                "§8Slot 1"
        ));

        inventario.setItem(1, criarItemSkill(
                Material.ENDER_PEARL,
                "§8§lEco da Singularidade",
                "§7Arremessa um eco instável de buraco negro.",
                "§8Slot 2"
        ));

        inventario.setItem(2, criarItemSkill(
                Material.INK_SAC,
                "§8§lRuptura de Tinta",
                "§7Skill em protótipo.",
                "§8Slot 3"
        ));

        inventario.setItem(3, criarItemSkill(
                Material.CRYING_OBSIDIAN,
                "§8§lPrisão Sombria",
                "§7Skill em protótipo.",
                "§8Slot 4"
        ));

        inventario.setItem(4, criarItemSkill(
                Material.NETHER_STAR,
                "§4§lChamado da Maldra",
                "§7Ainda não é hora dela responder.",
                "§8Slot 5"
        ));

        for (int i = 5; i < 9; i++) {
            inventario.setItem(i, criarItemSkill(
                    Material.GRAY_DYE,
                    "§8§lVazio",
                    "§7Nenhuma skill equipada neste slot.",
                    "§8Slot " + (i + 1)
            ));
        }
    }

    private ItemStack criarItemSkill(Material material, String nome, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(nome);
        meta.setLore(Arrays.asList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(skillItemKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    private void executarAvancoSombrio(Player jogador) {
        if (estaEmCooldown(jogador, SLOT_AVANCO_SOMBRIO)) {
            long segundos = getSegundosRestantesCooldown(jogador, SLOT_AVANCO_SOMBRIO);
            aplicarCooldownVisual(jogador);
            enviarBarraDeAcao(jogador, "§5Avanço Sombrio §cRecarga: §e" + segundos + "s");
            jogador.playSound(jogador.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.55f);
            return;
        }

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

        Location destino = obterDestinoSeguro(mundo, blocoX, blocoZ, origem);

        emitirRastroDoAvanco(mundo, origem, destino);
        tocarSomAvanco(mundo, origem);

        criarArmadilhaDeTentaculosDeTinta(jogador, mundo, origem.clone().add(0, 1.0, 0));

        jogador.teleport(destino);
        jogador.setFallDistance(0);

        tocarSomAvanco(mundo, destino);

        mundo.spawnParticle(
                Particle.SMOKE,
                destino.clone().add(0, 1.0, 0),
                22,
                0.30,
                0.75,
                0.30,
                0.035
        );

        iniciarCooldown(jogador, SLOT_AVANCO_SOMBRIO, COOLDOWN_AVANCO_MS, Material.BLACK_DYE);
        enviarBarraDeAcao(jogador, "§5Avanço Sombrio §7executado. §8A tinta está à espreita.");
    }

    private void executarEcoDaSingularidade(Player jogador) {
        if (estaEmCooldown(jogador, SLOT_ECO_SINGULARIDADE)) {
            long segundos = getSegundosRestantesCooldown(jogador, SLOT_ECO_SINGULARIDADE);
            aplicarCooldownVisual(jogador);
            enviarBarraDeAcao(jogador, "§8Eco da Singularidade §cRecarga: §e" + segundos + "s");
            jogador.playSound(jogador.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.45f);
            return;
        }

        Snowball projetil = jogador.launchProjectile(Snowball.class);

        projetil.setItem(criarItemVisualDoEco());
        projetil.setShooter(jogador);
        projetil.setGravity(true);

        projetil.getPersistentDataContainer().set(
                ecoProjectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        monitorarProjetilEco(jogador, projetil);

        jogador.getWorld().playSound(
                jogador.getLocation(),
                Sound.ENTITY_WITHER_SHOOT,
                1.3f,
                0.55f
        );

        iniciarCooldown(jogador, SLOT_ECO_SINGULARIDADE, COOLDOWN_ECO_SINGULARIDADE_MS, Material.ENDER_PEARL);
        enviarBarraDeAcao(jogador, "§8Eco da Singularidade §7arremessado.");
    }

    private void monitorarProjetilEco(Player dono, Snowball projetil) {
        BukkitTask tarefa = new BukkitRunnable() {
            private int tempo = 0;
            private Location ultimaLocalizacao = projetil.getLocation().clone();

            @Override
            public void run() {
                if (projetil.isValid() && !projetil.isDead()) {
                    ultimaLocalizacao = projetil.getLocation().clone();

                    if (tempo >= 20 * 12) {
                        if (registrarProcessamentoProjetilEco(projetil)) {
                            projetil.remove();
                            iniciarEcoDaSingularidade(dono, ultimaLocalizacao);
                        }

                        this.cancel();
                        return;
                    }

                    tempo++;
                    return;
                }

                if (registrarProcessamentoProjetilEco(projetil)) {
                    iniciarEcoDaSingularidade(dono, ultimaLocalizacao);
                }

                this.cancel();
            }
        }.runTaskTimer(plugin, 1L, 1L);

        tarefasAtivas.add(tarefa);
    }

    private boolean registrarProcessamentoProjetilEco(Entity projetil) {
        if (projetil == null) {
            return false;
        }

        UUID uuid = projetil.getUniqueId();

        if (projeteisEcoProcessados.contains(uuid)) {
            return false;
        }

        projeteisEcoProcessados.add(uuid);
        return true;
    }

    private ItemStack criarItemVisualDoEco() {
        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§8Eco da Singularidade");
            item.setItemMeta(meta);
        }

        return item;
    }

    private Location obterDestinoSeguro(World mundo, int blocoX, int blocoZ, Location origem) {
        Location blocoMaisAlto = mundo.getHighestBlockAt(blocoX, blocoZ).getLocation();

        return new Location(
                mundo,
                blocoX + 0.5,
                blocoMaisAlto.getY() + 1.0,
                blocoZ + 0.5,
                origem.getYaw(),
                origem.getPitch()
        );
    }

    private void criarArmadilhaDeTentaculosDeTinta(Player dono, World mundo, Location centro) {
        mundo.playSound(centro, Sound.ENTITY_WITHER_AMBIENT, 1.25f, 0.42f);

        Set<UUID> entidadesCapturadas = new HashSet<>();

        BukkitTask tarefa = new BukkitRunnable() {
            private int tempo = 0;
            private double giro = random.nextDouble() * Math.PI * 2.0;

            @Override
            public void run() {
                if (tempo >= DURACAO_TENTACULOS_TICKS) {
                    explodirTentaculos(dono, mundo, centro, entidadesCapturadas);
                    this.cancel();
                    return;
                }

                emitirNucleoDeTinta(mundo, centro, tempo);

                int indice = 0;
                boolean encontrouAlvo = false;

                for (Entity entidade : mundo.getNearbyEntities(
                        centro,
                        RAIO_TENTACULOS,
                        RAIO_TENTACULOS,
                        RAIO_TENTACULOS
                )) {
                    if (!(entidade instanceof LivingEntity criatura)) {
                        continue;
                    }

                    if (!podeAfetarEntidade(criatura, dono)) {
                        continue;
                    }

                    Location pontoCorpo = obterPontoCentralDoCorpo(criatura);

                    if (!pontoCorpo.getWorld().equals(mundo)) {
                        continue;
                    }

                    double distancia = pontoCorpo.distance(centro);

                    if (distancia > RAIO_TENTACULOS) {
                        continue;
                    }

                    encontrouAlvo = true;
                    entidadesCapturadas.add(criatura.getUniqueId());

                    emitirTentaculoMirado(mundo, centro, criatura, giro, tempo, indice);
                    puxarEntidadeParaNucleo(criatura, centro, distancia);

                    indice++;
                }

                if (!encontrouAlvo) {
                    emitirArmadilhaEmEspera(mundo, centro, giro, tempo);
                }

                giro += Math.PI / 11.0;
                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        tarefasAtivas.add(tarefa);
    }

    private void emitirNucleoDeTinta(World mundo, Location centro, int tempo) {
        double pulso = 0.42 + Math.sin(tempo * 0.22) * 0.14;

        mundo.spawnParticle(
                Particle.SMOKE,
                centro,
                22,
                pulso,
                pulso,
                pulso,
                0.028
        );

        if (tempo % 6 == 0) {
            mundo.spawnParticle(
                    Particle.SOUL,
                    centro,
                    5,
                    0.24,
                    0.24,
                    0.24,
                    0.012
            );
        }
    }

    private void emitirArmadilhaEmEspera(World mundo, Location centro, double giro, int tempo) {
        int pontos = 18;
        double raio = 1.15 + Math.sin(tempo * 0.08) * 0.18;

        for (int i = 0; i < pontos; i++) {
            double anguloAtual = giro + i * ((Math.PI * 2.0) / pontos);
            double x = Math.cos(anguloAtual) * raio;
            double z = Math.sin(anguloAtual) * raio;

            Location ponto = centro.clone().add(x, -0.55, z);
            Vector direcao = centro.toVector().subtract(ponto.toVector());

            if (direcao.lengthSquared() > 0) {
                direcao.normalize();
            }

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    0,
                    direcao.getX(),
                    0.05,
                    direcao.getZ(),
                    0.035
            );
        }
    }

    private void emitirTentaculoMirado(
            World mundo,
            Location centro,
            LivingEntity alvo,
            double giro,
            int tempo,
            int indice
    ) {
        Location pontoCorpo = obterPontoCentralDoCorpo(alvo);

        Vector paraAlvo = pontoCorpo.toVector().subtract(centro.toVector());

        if (paraAlvo.lengthSquared() == 0) {
            paraAlvo = new Vector(
                    random.nextDouble() - 0.5,
                    0,
                    random.nextDouble() - 0.5
            );
        }

        paraAlvo.setY(0);

        if (paraAlvo.lengthSquared() == 0) {
            paraAlvo = new Vector(1, 0, 0);
        }

        paraAlvo.normalize();

        double distanciaAlvo = Math.max(1.1, pontoCorpo.distance(centro));
        double recuoRaiz = Math.min(RAIO_TENTACULOS, distanciaAlvo + 1.65);

        Location raiz = centro.clone().add(paraAlvo.clone().multiply(recuoRaiz));
        raiz.setY(Math.min(pontoCorpo.getY(), centro.getY()) - 0.45);

        Vector caminho = pontoCorpo.toVector().subtract(raiz.toVector());

        if (caminho.lengthSquared() == 0) {
            return;
        }

        Vector direcao = caminho.clone().normalize();
        Vector lateral = new Vector(-direcao.getZ(), 0, direcao.getX());

        if (lateral.lengthSquared() == 0) {
            lateral = new Vector(1, 0, 0);
        }

        lateral.normalize();

        double comprimento = caminho.length();
        int pontos = 14;

        for (int p = 0; p < pontos; p++) {
            double progresso = p / (double) (pontos - 1);
            double distancia = comprimento * progresso;
            double intensidadeAgarre = Math.sin(progresso * Math.PI);
            double ondulacao = Math.sin((progresso * Math.PI * 3.0) + giro + indice) * 0.35 * intensidadeAgarre;
            double elevacao = Math.sin((progresso * Math.PI) + tempo * 0.15 + indice) * 0.22;

            Location ponto = raiz.clone()
                    .add(direcao.clone().multiply(distancia))
                    .add(lateral.clone().multiply(ondulacao))
                    .add(0, elevacao, 0);

            Vector direcaoPuxao = centro.toVector().subtract(ponto.toVector());

            if (direcaoPuxao.lengthSquared() > 0) {
                direcaoPuxao.normalize();
            }

            mundo.spawnParticle(
                    Particle.SMOKE,
                    ponto,
                    4,
                    0.055,
                    0.055,
                    0.055,
                    0.004
            );

            if (p >= pontos - 4) {
                mundo.spawnParticle(
                        Particle.SMOKE,
                        ponto,
                        0,
                        direcaoPuxao.getX(),
                        direcaoPuxao.getY(),
                        direcaoPuxao.getZ(),
                        0.095
                );
            }

            if (p == pontos - 1 && tempo % 4 == 0) {
                mundo.spawnParticle(
                        Particle.SOUL,
                        ponto,
                        2,
                        0.10,
                        0.10,
                        0.10,
                        0.01
                );
            }
        }
    }

    private void puxarEntidadeParaNucleo(LivingEntity criatura, Location centro, double distancia) {
        if (distancia <= RAIO_NUCLEO_TENTACULOS) {
            prenderNoNucleo(criatura, centro);
            return;
        }

        Vector direcaoParaCentro = centro.toVector().subtract(obterPontoCentralDoCorpo(criatura).toVector());

        if (direcaoParaCentro.lengthSquared() == 0) {
            return;
        }

        direcaoParaCentro.normalize();

        double intensidade = 1.0 - (distancia / RAIO_TENTACULOS);
        double forca = FORCA_PUXAO_TENTACULOS + (intensidade * 0.30);

        Vector velocidade = direcaoParaCentro.multiply(forca);
        velocidade.setY(limitar(velocidade.getY(), -0.05, 0.26));

        criatura.setVelocity(velocidade);
        criatura.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                14,
                3,
                true,
                false,
                true
        ));
    }

    private void prenderNoNucleo(LivingEntity criatura, Location centro) {
        Vector direcaoParaCentro = centro.toVector().subtract(obterPontoCentralDoCorpo(criatura).toVector());

        if (direcaoParaCentro.lengthSquared() > 0) {
            direcaoParaCentro.normalize().multiply(0.10);
        }

        direcaoParaCentro.setY(0.02);
        criatura.setVelocity(direcaoParaCentro);

        criatura.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                25,
                10,
                true,
                false,
                true
        ));
    }

    private void explodirTentaculos(Player dono, World mundo, Location centro, Set<UUID> entidadesCapturadas) {
        mundo.playSound(centro, Sound.ENTITY_GENERIC_EXPLODE, 1.85f, 0.62f);

        mundo.spawnParticle(
                Particle.SMOKE,
                centro,
                130,
                0.85,
                0.85,
                0.85,
                0.105
        );

        for (int i = 0; i < 100; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = random.nextDouble() * Math.PI;
            double distancia = 0.55 + random.nextDouble() * RAIO_TENTACULOS;

            double x = Math.cos(theta) * Math.sin(phi) * distancia;
            double y = Math.cos(phi) * distancia;
            double z = Math.sin(theta) * Math.sin(phi) * distancia;

            Location ponto = centro.clone().add(x, y, z);
            Vector direcao = ponto.toVector().subtract(centro.toVector());

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
                    0.24
            );
        }

        for (UUID uuid : entidadesCapturadas) {
            Entity entidade = Bukkit.getEntity(uuid);

            if (!(entidade instanceof LivingEntity criatura)) {
                continue;
            }

            if (!podeAfetarEntidade(criatura, dono)) {
                continue;
            }

            if (criatura.getWorld() != mundo || criatura.getLocation().distanceSquared(centro) > Math.pow(RAIO_TENTACULOS + 3.0, 2)) {
                continue;
            }

            Vector direcaoParaFora = obterPontoCentralDoCorpo(criatura).toVector().subtract(centro.toVector());

            if (direcaoParaFora.lengthSquared() == 0) {
                direcaoParaFora = new Vector(
                        random.nextDouble() - 0.5,
                        0.2,
                        random.nextDouble() - 0.5
                );
            }

            direcaoParaFora.normalize();

            double distancia = Math.max(0.0, obterPontoCentralDoCorpo(criatura).distance(centro));
            double proximidade = 1.0 - Math.min(1.0, distancia / RAIO_TENTACULOS);

            Vector velocidade = direcaoParaFora.multiply(FORCA_EXPLOSAO_TENTACULOS + proximidade * 0.95);
            velocidade.setY(FORCA_VERTICAL_EXPLOSAO_TENTACULOS + proximidade * 0.35);

            criatura.setVelocity(velocidade);
            criatura.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    private void iniciarEcoDaSingularidade(Player dono, Location impacto) {
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
                if (tempo >= TEMPO_IMPLOSAO_ECO_TICKS) {
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

                    criarDomoEco(dono, mundo, centro);

                    this.cancel();
                    return;
                }

                emitirImplosaoEco(mundo, centro, tempo);

                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        tarefasAtivas.add(tarefa);
    }

    private void criarDomoEco(Player dono, World mundo, Location centro) {
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
                if (tempo >= DURACAO_DOMO_ECO_TICKS) {
                    finalizarDomoEco(mundo, centro);
                    this.cancel();
                    return;
                }

                emitirBordaDoDomoEco(mundo, centro, tempo);
                emitirNeveSombriaEco(mundo, centro);
                aplicarDistorcaoDoTempoEco(dono, mundo, centro, tempo);

                if (tempo % 20 == 0) {
                    aplicarDanoNoDomoEco(dono, mundo, centro);
                    emitirPulsoDoDomoEco(mundo, centro);
                }

                tempo++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        tarefasAtivas.add(tarefa);
    }

    private void emitirImplosaoEco(World mundo, Location centro, int tempo) {
        double progresso = tempo / (double) TEMPO_IMPLOSAO_ECO_TICKS;
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

    private void emitirBordaDoDomoEco(World mundo, Location centro, int tempo) {
        if (tempo % 2 != 0) {
            return;
        }

        double fase = tempo * 0.08;
        int camadas = 9;
        int pontosPorAnel = 40;

        for (int camada = 0; camada < camadas; camada++) {
            double progressoVertical = -1.0 + ((2.0 * camada) / (camadas - 1.0));
            double y = progressoVertical * RAIO_DOMO_ECO;
            double raioAnel = Math.sqrt(Math.max(0.0, (RAIO_DOMO_ECO * RAIO_DOMO_ECO) - (y * y)));
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

    private void emitirNeveSombriaEco(World mundo, Location centro) {
        int particulas = 38;

        for (int i = 0; i < particulas; i++) {
            double x = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO_ECO;
            double y = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO_ECO;
            double z = (random.nextDouble() * 2.0 - 1.0) * RAIO_DOMO_ECO;

            if ((x * x) + (y * y) + (z * z) > RAIO_DOMO_ECO * RAIO_DOMO_ECO) {
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

    private void aplicarDistorcaoDoTempoEco(Player dono, World mundo, Location centro, int tempo) {
        int segundoAtual = tempo / 20;
        double fatorVelocidade = calcularFatorVelocidadePorTickEco(segundoAtual);
        boolean congelamentoTotal = segundoAtual >= SEGUNDO_CONGELAMENTO_TOTAL_ECO;

        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_DOMO_ECO,
                RAIO_DOMO_ECO,
                RAIO_DOMO_ECO
        )) {
            if (!estaDentroDoDomo(entidade.getLocation(), centro, RAIO_DOMO_ECO)) {
                continue;
            }

            if (entidade instanceof LivingEntity criatura) {
                if (!podeAfetarEntidade(criatura, dono)) {
                    continue;
                }

                aplicarLentidaoEmCriaturaEco(criatura, segundoAtual, fatorVelocidade, congelamentoTotal);
                continue;
            }

            if (entidade instanceof Projectile projectile) {
                aplicarLentidaoEmProjetilEco(projectile, fatorVelocidade, congelamentoTotal);
            }
        }
    }

    private void aplicarLentidaoEmCriaturaEco(
            LivingEntity criatura,
            int segundoAtual,
            double fatorVelocidade,
            boolean congelamentoTotal
    ) {
        if (criatura.isDead()) {
            return;
        }

        int amplificadorLentidao = calcularAmplificadorLentidaoEco(segundoAtual);

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

    private void aplicarLentidaoEmProjetilEco(
            Projectile projectile,
            double fatorVelocidade,
            boolean congelamentoTotal
    ) {
        if (projectile.isDead() || !projectile.isValid()) {
            return;
        }

        if (ehProjetilEcoDaSingularidade(projectile)) {
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

    private void aplicarDanoNoDomoEco(Player dono, World mundo, Location centro) {
        for (Entity entidade : mundo.getNearbyEntities(
                centro,
                RAIO_DOMO_ECO,
                RAIO_DOMO_ECO,
                RAIO_DOMO_ECO
        )) {
            if (!(entidade instanceof LivingEntity criatura)) {
                continue;
            }

            if (!podeAfetarEntidade(criatura, dono)) {
                continue;
            }

            if (!estaDentroDoDomo(criatura.getLocation(), centro, RAIO_DOMO_ECO)) {
                continue;
            }

            if (dono != null && dono.isOnline()) {
                criatura.damage(DANO_ECO_POR_SEGUNDO, dono);
            } else {
                criatura.damage(DANO_ECO_POR_SEGUNDO);
            }
        }
    }

    private void emitirPulsoDoDomoEco(World mundo, Location centro) {
        mundo.playSound(
                centro,
                Sound.ENTITY_WITHER_AMBIENT,
                1.1f,
                0.25f
        );

        int pontos = 96;

        for (int i = 0; i < pontos; i++) {
            double angulo = i * ((Math.PI * 2.0) / pontos);

            double x = Math.cos(angulo) * RAIO_DOMO_ECO;
            double z = Math.sin(angulo) * RAIO_DOMO_ECO;

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

    private void finalizarDomoEco(World mundo, Location centro) {
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
                RAIO_DOMO_ECO * 0.35,
                RAIO_DOMO_ECO * 0.35,
                RAIO_DOMO_ECO * 0.35,
                0.08
        );

        restaurarProjeteisCongelados();
        restaurarAiDasCriaturas();
    }

    private Location obterLocalImpacto(ProjectileHitEvent event) {
        if (event.getHitEntity() != null) {
            Entity entidade = event.getHitEntity();

            return entidade.getLocation().clone().add(
                    0,
                    Math.max(0.7, entidade.getHeight() * 0.5),
                    0
            );
        }

        if (event.getHitBlock() != null && event.getHitBlockFace() != null) {
            BlockFace face = event.getHitBlockFace();
            Vector direcaoFace = face.getDirection();

            return event.getHitBlock()
                    .getLocation()
                    .clone()
                    .add(0.5, 0.5, 0.5)
                    .add(direcaoFace.multiply(0.65));
        }

        return event.getEntity().getLocation().clone();
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

            if (random.nextDouble() < 0.55) {
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
        }

        mundo.spawnParticle(
                Particle.SMOKE,
                inicio,
                24,
                0.35,
                0.55,
                0.35,
                0.018
        );

        mundo.spawnParticle(
                Particle.SMOKE,
                fim,
                26,
                0.38,
                0.65,
                0.38,
                0.02
        );
    }

    private void tocarSomAvanco(World mundo, Location local) {
        float pitch = 0.65f + (random.nextFloat() * 0.32f);

        mundo.playSound(
                local,
                Sound.ENTITY_WITHER_AMBIENT,
                1.2f,
                pitch
        );
    }

    private void iniciarCooldown(Player jogador, int slotSkill, long cooldownMs, Material materialVisual) {
        cooldowns
                .computeIfAbsent(jogador.getUniqueId(), uuid -> new HashMap<>())
                .put(slotSkill, System.currentTimeMillis() + cooldownMs);

        int ticks = (int) Math.ceil(cooldownMs / 50.0);
        jogador.setCooldown(materialVisual, ticks);
    }

    private boolean estaEmCooldown(Player jogador, int slotSkill) {
        long fim = getFimCooldown(jogador, slotSkill);
        return System.currentTimeMillis() < fim;
    }

    private long getSegundosRestantesCooldown(Player jogador, int slotSkill) {
        long restante = getFimCooldown(jogador, slotSkill) - System.currentTimeMillis();

        if (restante <= 0) {
            return 0;
        }

        return (long) Math.ceil(restante / 1000.0);
    }

    private long getFimCooldown(Player jogador, int slotSkill) {
        Map<Integer, Long> cooldownJogador = cooldowns.get(jogador.getUniqueId());

        if (cooldownJogador == null) {
            return 0L;
        }

        return cooldownJogador.getOrDefault(slotSkill, 0L);
    }

    private void aplicarCooldownVisual(Player jogador) {
        long segundosAvanco = getSegundosRestantesCooldown(jogador, SLOT_AVANCO_SOMBRIO);
        long segundosEco = getSegundosRestantesCooldown(jogador, SLOT_ECO_SINGULARIDADE);

        if (segundosAvanco > 0) {
            jogador.setCooldown(Material.BLACK_DYE, (int) Math.ceil(segundosAvanco * 20.0));
        }

        if (segundosEco > 0) {
            jogador.setCooldown(Material.ENDER_PEARL, (int) Math.ceil(segundosEco * 20.0));
        }
    }

    private String montarTextoSkillBar(Player jogador) {
        long cdAvanco = getSegundosRestantesCooldown(jogador, SLOT_AVANCO_SOMBRIO);
        long cdEco = getSegundosRestantesCooldown(jogador, SLOT_ECO_SINGULARIDADE);

        String avanco = cdAvanco > 0
                ? "§8[§51 Avanço §c" + cdAvanco + "s§8]"
                : "§8[§51 Avanço§8]";

        String eco = cdEco > 0
                ? "§8[§82 Eco §c" + cdEco + "s§8]"
                : "§8[§82 Eco§8]";

        return avanco + " "
                + eco + " "
                + "§8[§73 Tinta§8] "
                + "§8[§74 Prisão§8] "
                + "§8[§45 Maldra§8]";
    }

    private void enviarBarraDeAcao(Player jogador, String mensagem) {
        jogador.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(mensagem));
    }

    private Location obterPontoCentralDoCorpo(LivingEntity criatura) {
        return criatura.getLocation().clone().add(0, Math.max(0.65, criatura.getHeight() * 0.5), 0);
    }

    private boolean podeAfetarEntidade(LivingEntity criatura, Player dono) {
        if (criatura == null || criatura.isDead()) {
            return false;
        }

        if (dono == null) {
            return true;
        }

        if (!(criatura instanceof Player jogador)) {
            return true;
        }

        return !jogador.getUniqueId().equals(dono.getUniqueId());
    }

    private boolean estaDentroDoDomo(Location local, Location centro, double raio) {
        if (local == null || centro == null) {
            return false;
        }

        if (local.getWorld() == null || centro.getWorld() == null) {
            return false;
        }

        if (!local.getWorld().equals(centro.getWorld())) {
            return false;
        }

        return local.distanceSquared(centro) <= raio * raio;
    }

    private double calcularFatorVelocidadePorTickEco(int segundoAtual) {
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

    private int calcularAmplificadorLentidaoEco(int segundoAtual) {
        if (segundoAtual >= SEGUNDO_CONGELAMENTO_TOTAL_ECO) {
            return 255;
        }

        return Math.min(7, 1 + segundoAtual);
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

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private static class EstadoHotbar {
        private final ItemStack[] hotbarOriginal;
        private final int slotOriginal;

        private EstadoHotbar(ItemStack[] hotbarOriginal, int slotOriginal) {
            this.hotbarOriginal = hotbarOriginal;
            this.slotOriginal = slotOriginal;
        }
    }
}
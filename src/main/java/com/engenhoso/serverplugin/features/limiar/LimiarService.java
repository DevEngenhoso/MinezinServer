package com.engenhoso.serverplugin.features.limiar;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import com.engenhoso.serverplugin.features.players.PlayerProfile;
import com.engenhoso.serverplugin.features.players.PlayerProfileService;
import com.engenhoso.serverplugin.shared.hologram.HologramButton;
import com.engenhoso.serverplugin.shared.hologram.HologramInterface;
import com.engenhoso.serverplugin.shared.hologram.HologramInterfaceService;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LimiarService {

    public static final String LIMIAR_WORLD_NAME = "world_limiar";
    public static final String SURVIVAL_WORLD_NAME = "world_survival";

    private static final String CONFIG_LIMIAR_SPAWN = "limiar.spawn";
    private static final String CONFIG_SURVIVAL_SPAWN = "limiar.survivalSpawn";
    private static final String CONFIG_MANNEQUIN_SKINS = "limiar.mannequin-skins";

    private static final double DISTANCIA_MAXIMA_INTERACAO = 4.0;
    private static final double DISTANCIA_MAXIMA_INTERACAO_QUADRADO = DISTANCIA_MAXIMA_INTERACAO * DISTANCIA_MAXIMA_INTERACAO;

    private static final int DISTANCIA_MIRA_LEGENDA = 4;
    private static final long COOLDOWN_INTERACAO_MS = 700L;

    private final JavaPlugin plugin;
    private final PlayerProfileService playerProfileService;
    private final HologramInterfaceService hologramInterfaceService;

    private final NamespacedKey keyTotemTipo;

    private final Map<UUID, Long> cooldownInteracaoPorJogador = new ConcurrentHashMap<>();

    private BukkitTask miraTask;

    public LimiarService(
            JavaPlugin plugin,
            PlayerProfileService playerProfileService,
            HologramInterfaceService hologramInterfaceService
    ) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
        this.hologramInterfaceService = hologramInterfaceService;
        this.keyTotemTipo = new NamespacedKey(plugin, "limiar_totem_tipo");

        garantirConfigPadrao();
    }

    public World criarOuCarregarLimiar(CommandSender sender) {
        World mundoCarregado = Bukkit.getWorld(LIMIAR_WORLD_NAME);

        if (mundoCarregado != null) {
            sender.sendMessage("§eO mundo §f" + LIMIAR_WORLD_NAME + " §ejá estava carregado.");
            return mundoCarregado;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), LIMIAR_WORLD_NAME);
        boolean mundoJaExistia = worldFolder.exists();

        sender.sendMessage("§7Criando/carregando mundo §f" + LIMIAR_WORLD_NAME + "§7...");

        WorldCreator creator = new WorldCreator(LIMIAR_WORLD_NAME);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);

        World world = Bukkit.createWorld(creator);

        if (world == null) {
            sender.sendMessage("§cNão foi possível criar/carregar o mundo do Limiar.");
            throw new IllegalStateException("Falha ao criar/carregar mundo " + LIMIAR_WORLD_NAME);
        }

        configurarLimiar(world);

        if (!mundoJaExistia) {
            criarBaseInicial(world);
            salvarLimiarSpawn(new Location(world, 0.5, 81.0, 0.5, 0.0f, 0.0f));
            sender.sendMessage("§aMundo §f" + LIMIAR_WORLD_NAME + " §acriado com base inicial.");
        } else {
            sender.sendMessage("§aMundo §f" + LIMIAR_WORLD_NAME + " §acarregado com sucesso.");
        }

        plugin.getLogger().info("[Limiar] Mundo carregado: " + LIMIAR_WORLD_NAME);
        return world;
    }

    public World criarOuCarregarSurvival(CommandSender sender) {
        World mundoCarregado = Bukkit.getWorld(SURVIVAL_WORLD_NAME);

        if (mundoCarregado != null) {
            sender.sendMessage("§eO mundo §f" + SURVIVAL_WORLD_NAME + " §ejá estava carregado.");
            return mundoCarregado;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), SURVIVAL_WORLD_NAME);
        boolean mundoJaExistia = worldFolder.exists();

        sender.sendMessage("§7Criando/carregando mundo §f" + SURVIVAL_WORLD_NAME + "§7...");

        WorldCreator creator = new WorldCreator(SURVIVAL_WORLD_NAME);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.generateStructures(true);

        World world = Bukkit.createWorld(creator);

        if (world == null) {
            sender.sendMessage("§cNão foi possível criar/carregar o mundo survival.");
            throw new IllegalStateException("Falha ao criar/carregar mundo " + SURVIVAL_WORLD_NAME);
        }

        configurarSurvival(world);

        if (!mundoJaExistia) {
            Location spawn = world.getSpawnLocation().clone().add(0.5, 1.0, 0.5);
            salvarSurvivalSpawn(spawn);
            sender.sendMessage("§aMundo §f" + SURVIVAL_WORLD_NAME + " §acriado com sucesso.");
        } else {
            sender.sendMessage("§aMundo §f" + SURVIVAL_WORLD_NAME + " §acarregado com sucesso.");
        }

        plugin.getLogger().info("[Limiar] Survival carregado: " + SURVIVAL_WORLD_NAME);
        return world;
    }

    public void teleportarAdminParaLimiar(Player player) {
        World world = Bukkit.getWorld(LIMIAR_WORLD_NAME);

        if (world == null) {
            world = criarOuCarregarLimiar(player);
        }

        player.teleport(getLimiarSpawn(world));
        player.setGameMode(GameMode.CREATIVE);

        player.sendMessage("§aVocê foi enviado para o §f" + LIMIAR_WORLD_NAME + "§a.");
        player.sendMessage("§7Use §f/mz limiar spawntotem <tipo> §7para criar os totens.");
    }

    public void teleportarAdminParaSurvival(Player player) {
        World world = Bukkit.getWorld(SURVIVAL_WORLD_NAME);

        if (world == null) {
            world = criarOuCarregarSurvival(player);
        }

        player.teleport(getSurvivalSpawn(world));
        player.setGameMode(GameMode.CREATIVE);

        player.sendMessage("§aVocê foi enviado para o §f" + SURVIVAL_WORLD_NAME + "§a.");
    }

    public void enviarJogadorParaLimiar(Player player) {
        World world = Bukkit.getWorld(LIMIAR_WORLD_NAME);

        if (world == null) {
            world = criarOuCarregarLimiar(player);
        }

        PlayerProfile profile = playerProfileService.getOrLoad(player);

        player.teleport(getLimiarSpawn(world));
        player.setGameMode(GameMode.ADVENTURE);
        player.setFallDistance(0);

        if (profile.hasClasse()) {
            player.sendTitle(
                    "§5§lLimiar da Vigília",
                    "§7Use o portal para atravessar",
                    20,
                    70,
                    30
            );

            player.sendMessage("§8§m--------------------------------");
            player.sendMessage("§5§lLimiar da Vigília");
            player.sendMessage("§7Você retornou ao Limiar.");
            player.sendMessage("§7Use o totem do Survival para atravessar ao mundo dos vivos.");
            player.sendMessage("§8§m--------------------------------");
        } else {
            player.sendTitle(
                    "§5§lLimiar da Vigília",
                    "§7Escolha o destino da sua alma",
                    20,
                    80,
                    30
            );

            player.sendMessage("§8§m--------------------------------");
            player.sendMessage("§5§lLimiar da Vigília");
            player.sendMessage("§7Você ainda não possui uma classe.");
            player.sendMessage("§7Interaja com um manequim para ouvir o chamado de um caminho.");
            player.sendMessage("§8§m--------------------------------");
        }

        plugin.getLogger().info("[Limiar] Jogador enviado ao Limiar: " + player.getName());
    }

    public void enviarJogadorParaSurvival(Player player) {
        PlayerProfile profile = playerProfileService.getOrLoad(player);

        if (!profile.hasClasse()) {
            efeitoBloqueado(player);
            player.sendMessage("§cUma força impede sua passagem.");
            player.sendMessage("§7Escolha uma classe antes de atravessar para o Survival.");
            return;
        }

        World world = Bukkit.getWorld(SURVIVAL_WORLD_NAME);

        if (world == null) {
            world = criarOuCarregarSurvival(player);
        }

        player.teleport(getSurvivalSpawn(world));
        player.setGameMode(GameMode.SURVIVAL);
        player.setFallDistance(0);

        player.sendTitle(
                "§2§lMundo dos Vivos",
                "§7Sua jornada começa agora",
                20,
                80,
                30
        );

        player.sendMessage("§aVocê atravessou para o Survival.");
        plugin.getLogger().info("[Limiar] Jogador atravessou para o Survival: " + player.getName());
    }

    public void spawnarTotem(Player player, LimiarTotemTipo tipo) {
        if (!player.getWorld().getName().equals(LIMIAR_WORLD_NAME)) {
            player.sendMessage("§cVocê precisa estar no mundo §f" + LIMIAR_WORLD_NAME + "§c.");
            player.sendMessage("§7Use §f/mz limiar tp §7antes de spawnar os totens.");
            return;
        }

        Location spawnLocation = centralizarNoBloco(player.getLocation());

        Mannequin mannequin = player.getWorld().spawn(spawnLocation, Mannequin.class);

        configurarMannequinBase(mannequin, tipo);
        aplicarSkin(mannequin, tipo);
        aplicarEquipamento(mannequin, tipo);

        player.sendMessage("§aTotem §f" + tipo.getNomeExibicao() + " §aspawnado em:");
        player.sendMessage("§7" + formatarLocation(spawnLocation));
        player.sendMessage("§8Ele nasceu centralizado no bloco, olhando para a direção que você estava olhando.");

        plugin.getLogger().info("[Limiar] Totem spawnado: " + tipo.getId() + " em " + formatarLocation(spawnLocation));
    }

    public void removerTotemMirado(Player player) {
        Entity target = player.getTargetEntity(8, false);

        if (target == null || !isTotem(target)) {
            player.sendMessage("§cMire em um totem do Limiar para removê-lo.");
            return;
        }

        Optional<LimiarTotemTipo> optionalTipo = getTipoTotem(target);
        String nome = optionalTipo.map(LimiarTotemTipo::getNomeExibicao).orElse("desconhecido");

        target.remove();
        hologramInterfaceService.close(player);

        player.sendMessage("§aTotem removido: §f" + nome + "§a.");
    }

    public void limparTotens(CommandSender sender) {
        World world = Bukkit.getWorld(LIMIAR_WORLD_NAME);

        if (world == null) {
            sender.sendMessage("§cO mundo do Limiar não está carregado.");
            return;
        }

        int removidos = 0;

        for (Mannequin mannequin : world.getEntitiesByClass(Mannequin.class)) {
            if (isTotem(mannequin)) {
                mannequin.remove();
                removidos++;
            }
        }

        hologramInterfaceService.closeAll();

        sender.sendMessage("§aTotens removidos: §f" + removidos + "§a.");
    }

    public void listarTotens(CommandSender sender) {
        World world = Bukkit.getWorld(LIMIAR_WORLD_NAME);

        if (world == null) {
            sender.sendMessage("§cO mundo do Limiar não está carregado.");
            return;
        }

        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lTotens do Limiar");

        int total = 0;

        for (Mannequin mannequin : world.getEntitiesByClass(Mannequin.class)) {
            Optional<LimiarTotemTipo> optionalTipo = getTipoTotem(mannequin);

            if (optionalTipo.isEmpty()) {
                continue;
            }

            total++;

            sender.sendMessage("§7- §f" + optionalTipo.get().getNomeExibicao()
                    + " §8em §7" + formatarLocation(mannequin.getLocation()));
        }

        if (total == 0) {
            sender.sendMessage("§8Nenhum totem encontrado.");
        }

        sender.sendMessage("§8§m--------------------------------");
    }

    public boolean tratarInteracaoTotem(Player player, Entity entity) {
        Optional<LimiarTotemTipo> optionalTipo = getTipoTotem(entity);

        if (optionalTipo.isEmpty()) {
            return false;
        }

        if (!estaPertoDoTotem(player, entity)) {
            player.sendActionBar(legacy("§8Aproxime-se do totem para interagir."));
            return true;
        }

        if (estaEmCooldownDeInteracao(player)) {
            return true;
        }

        registrarCooldownDeInteracao(player);

        LimiarTotemTipo tipo = optionalTipo.get();

        if (tipo.isTotemSurvival()) {
            enviarJogadorParaSurvival(player);
            return true;
        }

        PlayerProfile profile = playerProfileService.getOrLoad(player);

        if (profile.hasClasse()) {
            efeitoEscolhaNegada(player);
            player.sendMessage("§8Nada acontece.");
            player.sendMessage("§7Sua alma já foi selada por outro caminho.");
            player.sendMessage("§7Classe atual: §f" + profile.getClasse());
            return true;
        }

        abrirInterfaceClasse(player, entity, tipo);
        return true;
    }

    public boolean isTotem(Entity entity) {
        if (entity == null) {
            return false;
        }

        PersistentDataContainer data = entity.getPersistentDataContainer();

        return data.has(keyTotemTipo, PersistentDataType.STRING);
    }

    public void iniciarTarefaDeMira() {
        pararTarefaDeMira();

        miraTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                mostrarLegendaSeMirando(player);
            }
        }, 10L, 5L);
    }

    public void pararTarefaDeMira() {
        if (miraTask != null) {
            miraTask.cancel();
            miraTask = null;
        }
    }

    public void salvarLimiarSpawnAtual(Player player) {
        if (!player.getWorld().getName().equals(LIMIAR_WORLD_NAME)) {
            player.sendMessage("§cVocê precisa estar no mundo §f" + LIMIAR_WORLD_NAME + "§c.");
            return;
        }

        salvarLimiarSpawn(player.getLocation());
        player.sendMessage("§aSpawn do Limiar definido na sua posição atual.");
    }

    public void salvarSurvivalSpawnAtual(Player player) {
        if (!player.getWorld().getName().equals(SURVIVAL_WORLD_NAME)) {
            player.sendMessage("§cVocê precisa estar no mundo §f" + SURVIVAL_WORLD_NAME + "§c.");
            return;
        }

        salvarSurvivalSpawn(player.getLocation());
        player.sendMessage("§aSpawn do Survival definido na sua posição atual.");
    }

    public void enviarInfo(CommandSender sender) {
        World limiar = Bukkit.getWorld(LIMIAR_WORLD_NAME);
        World survival = Bukkit.getWorld(SURVIVAL_WORLD_NAME);

        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§6§lLimiar da Vigília");
        sender.sendMessage("§7Mundo Limiar: §f" + LIMIAR_WORLD_NAME + " " + formatarStatusMundo(limiar));
        sender.sendMessage("§7Mundo Survival: §f" + SURVIVAL_WORLD_NAME + " " + formatarStatusMundo(survival));
        sender.sendMessage("§7Spawn Limiar: §f" + formatarLocationConfig(CONFIG_LIMIAR_SPAWN));
        sender.sendMessage("§7Spawn Survival: §f" + formatarLocationConfig(CONFIG_SURVIVAL_SPAWN));
        sender.sendMessage("§8§m--------------------------------");

        sender.sendMessage("§6§lSkins configuradas");

        for (LimiarTotemTipo tipo : LimiarTotemTipo.values()) {
            String url = getSkinUrl(tipo);
            sender.sendMessage("§7" + tipo.getNomeExibicao() + ": §f" + (url.isBlank() ? "§8sem skin custom" : "configurada"));
        }

        sender.sendMessage("§8§m--------------------------------");
    }

    private void abrirInterfaceClasse(Player player, Entity entity, LimiarTotemTipo tipo) {
        HologramInterface.Builder builder = HologramInterface.builder("limiar-classe-" + tipo.getId())
                .title(getTituloClasse(tipo))
                .subtitle("")
                .blankLine();

        for (String linha : getDescricaoClasse(tipo)) {
            if (linha.isBlank()) {
                builder.blankLine();
            } else {
                builder.line(linha);
            }
        }

        HologramInterface hologramInterface = builder
                .button(HologramButton.of(
                        "cancelar",
                        "§c§l[ RECUAR ]",
                        1.65f,
                        0.85f,
                        true,
                        context -> cancelarEscolhaClasse(context.getPlayer())
                ))
                .button(HologramButton.of(
                        "confirmar",
                        "§a§l[ SELAR DESTINO ]",
                        2.25f,
                        0.85f,
                        false,
                        context -> confirmarEscolhaClasse(context.getPlayer(), tipo)
                ))
                .distanceFromAnchor(2.05)
                .verticalOffset(1.85)
                .lineSpacing(0.19)
                .lineWidth(520)
                .durationTicks(20L * 45L)
                .build();

        hologramInterfaceService.open(player, entity, hologramInterface);

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 0.8f);
        player.sendActionBar(legacy("§eA interface se abriu diante do totem."));
    }

    private void confirmarEscolhaClasse(Player player, LimiarTotemTipo tipo) {
        Optional<ClasseTipo> optionalClasse = tipo.getClasseTipo();

        if (optionalClasse.isEmpty()) {
            player.sendMessage("§cEste chamado não possui uma classe válida.");
            return;
        }

        PlayerProfile profile = playerProfileService.getOrLoad(player);

        if (profile.hasClasse()) {
            efeitoEscolhaNegada(player);
            player.sendMessage("§8Nada acontece.");
            player.sendMessage("§7Sua alma já foi selada por outro caminho.");
            player.sendMessage("§7Classe atual: §f" + profile.getClasse());
            return;
        }

        ClasseTipo classeTipo = optionalClasse.get();

        boolean definiuClasse = playerProfileService.definirClasse(player, classeTipo.name());

        if (!definiuClasse) {
            efeitoEscolhaNegada(player);
            player.sendMessage("§cNão foi possível definir sua classe.");
            return;
        }

        hologramInterfaceService.close(player);

        efeitoEscolhaSucesso(player, tipo);

        player.sendTitle(
                "§6§lDestino Selado",
                "§f" + tipo.getNomeExibicao(),
                20,
                90,
                30
        );

        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("§6§lDestino selado");
        player.sendMessage("§7Sua classe foi definida como §f" + tipo.getNomeExibicao() + "§7.");
        player.sendMessage("§7Agora use o §fTotem do Survival §7para atravessar.");
        player.sendMessage("§8§m--------------------------------");

        plugin.getLogger().info("[Limiar] Classe escolhida por " + player.getName() + ": " + classeTipo.name());
    }

    private void cancelarEscolhaClasse(Player player) {
        player.sendMessage("§7Você afasta a mão do totem. O chamado se cala.");
        player.playSound(player.getLocation(), Sound.BLOCK_CANDLE_EXTINGUISH, 0.7f, 0.8f);
    }

    private String getTituloClasse(LimiarTotemTipo tipo) {
        return switch (tipo) {
            case TANQUE -> "§b§l[=] §6§lTANQUE §b§l[=]";
            case GUERREIRO -> "§b§l[/] §6§lGUERREIRO §b§l[/]";
            case ATIRADOR -> "§b§l[>] §6§lATIRADOR §b§l[<]";
            case MAGO -> "§b§l[*] §6§lMAGO §b§l[*]";
            case SACERDOTE -> "§b§l[+] §6§lSACERDOTE §b§l[+]";
            case SURVIVAL -> "§b§l[~] §2§lSURVIVAL §b§l[~]";
        };
    }

    private String[] getDescricaoClasse(LimiarTotemTipo tipo) {
        return switch (tipo) {
            case SACERDOTE -> new String[]{
                    "§7Uma luz fria atravessa o Limiar,",
                    "§7silenciosa como uma prece esquecida.",
                    "",
                    "§7Este é o caminho daqueles que sustentam",
                    "§7a vida onde a morte já estendeu a mão.",
                    "§7O Sacerdote protege, cura e fortalece",
                    "§7aqueles que ainda têm uma jornada a cumprir.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Cura e suporte.",
                    "§f- Proteção de aliados.",
                    "§f- Bênçãos e fortalecimento.",
                    "§f- Utilidade em grupos.",
                    "§f- Ideal para manter a equipe viva.",
                    "",
                    "§7Ao escolher este caminho, sua alma será",
                    "§7selada pela §ffé§7."
            };

            case MAGO -> new String[]{
                    "§7A realidade ao seu redor se dobra",
                    "§7em silêncio.",
                    "",
                    "§7Este é o caminho daqueles que não lutam",
                    "§7apenas com as mãos, mas com as forças",
                    "§7que sustentam o mundo.",
                    "§7O Mago transforma conhecimento em",
                    "§7destruição, controle e poder arcano.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Dano mágico à distância.",
                    "§f- Controle de área.",
                    "§f- Explosões e efeitos especiais.",
                    "§f- Alto potencial ofensivo.",
                    "§f- Mais frágil em combate direto.",
                    "",
                    "§7Ao escolher este caminho, sua alma será",
                    "§7selada pelo §darcano§7."
            };

            case ATIRADOR -> new String[]{
                    "§7O ar parece parar por um instante,",
                    "§7como se o mundo aguardasse o disparo.",
                    "",
                    "§7Este é o caminho daqueles que vencem",
                    "§7antes que o inimigo se aproxime.",
                    "§7O Atirador observa, calcula e atinge",
                    "§7onde a armadura falha.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Alto dano à distância.",
                    "§f- Precisão com arcos e bestas.",
                    "§f- Controle de posicionamento.",
                    "§f- Ataques seguros fora do alcance inimigo.",
                    "§f- Ideal para eliminar alvos antes",
                    "§f  do confronto direto.",
                    "",
                    "§7Ao escolher este caminho, sua alma será",
                    "§7selada pela §aprecisão§7."
            };

            case GUERREIRO -> new String[]{
                    "§7Uma lâmina invisível corta o silêncio",
                    "§7diante de você.",
                    "",
                    "§7Este é o caminho daqueles que resolvem",
                    "§7o destino no alcance do aço.",
                    "§7O Guerreiro avança sem hesitar,",
                    "§7quebra defesas e transforma cada",
                    "§7confronto em uma sentença.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Alto dano corpo a corpo.",
                    "§f- Pressão constante em combate.",
                    "§f- Investidas e golpes pesados.",
                    "§f- Boa resistência ofensiva.",
                    "§f- Ideal para duelos e combate direto.",
                    "",
                    "§7Ao escolher este caminho, sua alma será",
                    "§7selada pela §clâmina§7."
            };

            case TANQUE -> new String[]{
                    "§7Você sente o peso de uma muralha antiga",
                    "§7repousar sobre seus ombros.",
                    "",
                    "§7Este é o caminho daqueles que permanecem",
                    "§7quando todos os outros recuam.",
                    "§7O Tanque não busca a glória do golpe final,",
                    "§7mas a honra de impedir que seus aliados caiam.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Alta resistência.",
                    "§f- Defesa e mitigação de dano.",
                    "§f- Controle de inimigos.",
                    "§f- Proteção de aliados.",
                    "§f- Ideal para ficar na linha de frente.",
                    "",
                    "§7Ao escolher este caminho, sua alma será",
                    "§7selada pela §bresistência§7."
            };

            case SURVIVAL -> new String[]{
                    "§7O mundo dos vivos chama do outro lado.",
                    "",
                    "§7A travessia não exige promessa,",
                    "§7apenas coragem.",
                    "",
                    "§e§lCapacidades:",
                    "§f- Entrada no mundo de sobrevivência.",
                    "§f- Acesso à progressão principal.",
                    "§f- Exploração, coleta e combate.",
                    "",
                    "§7Somente uma alma já selada pode atravessar."
            };
        };
    }

    private boolean estaPertoDoTotem(Player player, Entity entity) {
        if (player.getWorld() != entity.getWorld()) {
            return false;
        }

        return player.getLocation().distanceSquared(entity.getLocation()) <= DISTANCIA_MAXIMA_INTERACAO_QUADRADO;
    }

    private boolean estaEmCooldownDeInteracao(Player player) {
        Long ultimoUso = cooldownInteracaoPorJogador.get(player.getUniqueId());

        if (ultimoUso == null) {
            return false;
        }

        return System.currentTimeMillis() - ultimoUso < COOLDOWN_INTERACAO_MS;
    }

    private void registrarCooldownDeInteracao(Player player) {
        cooldownInteracaoPorJogador.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void configurarMannequinBase(Mannequin mannequin, LimiarTotemTipo tipo) {
        mannequin.setPersistent(true);
        mannequin.setInvulnerable(true);
        mannequin.setGravity(false);
        mannequin.setSilent(true);
        mannequin.setAI(false);
        mannequin.setCanPickupItems(false);
        mannequin.setRemoveWhenFarAway(false);
        mannequin.setCollidable(false);
        mannequin.setImmovable(true);
        mannequin.setMainHand(MainHand.RIGHT);

        mannequin.customName(legacy("§6" + tipo.getNomeExibicao()));
        mannequin.setCustomNameVisible(false);

        mannequin.setDescription(legacy(getDescricaoTotem(tipo)));

        mannequin.getPersistentDataContainer().set(
                keyTotemTipo,
                PersistentDataType.STRING,
                tipo.getId()
        );
    }

    private void aplicarSkin(Mannequin mannequin, LimiarTotemTipo tipo) {
        String skinUrl = getSkinUrl(tipo);

        if (skinUrl.isBlank()) {
            return;
        }

        try {
            com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.getServer().createProfile(
                    UUID.randomUUID(),
                    gerarNomeProfile(tipo)
            );

            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(skinUrl), getSkinModel(tipo));
            profile.setTextures(textures);

            mannequin.setProfile(ResolvableProfile.resolvableProfile(profile));
        } catch (MalformedURLException exception) {
            plugin.getLogger().warning("[Limiar] URL de skin inválida para " + tipo.getId() + ": " + skinUrl);
        } catch (Exception exception) {
            plugin.getLogger().warning("[Limiar] Não foi possível aplicar skin do totem " + tipo.getId() + ": " + exception.getMessage());
        }
    }

    private void aplicarEquipamento(Mannequin mannequin, LimiarTotemTipo tipo) {
        EntityEquipment equipment = mannequin.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack air = new ItemStack(Material.AIR);

        equipment.setHelmet(air);
        equipment.setChestplate(air);
        equipment.setLeggings(air);
        equipment.setBoots(air);
        equipment.setItemInMainHand(air);
        equipment.setItemInOffHand(air);

        switch (tipo) {
            case TANQUE -> {
                equipment.setItemInMainHand(criarItemVisual(Material.IRON_SWORD, "§7Espada de Guarda"));
                equipment.setItemInOffHand(criarItemVisual(Material.SHIELD, "§fEscudo do Tanque"));
            }

            case GUERREIRO -> equipment.setItemInMainHand(
                    criarItemVisual(Material.IRON_SWORD, "§cLâmina do Guerreiro")
            );

            case ATIRADOR -> equipment.setItemInMainHand(
                    criarItemVisual(Material.BOW, "§aArco do Atirador")
            );

            case MAGO -> equipment.setItemInMainHand(
                    criarItemVisual(Material.BLAZE_ROD, "§5Cajado do Mago")
            );

            case SACERDOTE -> equipment.setItemInMainHand(
                    criarItemVisual(Material.TOTEM_OF_UNDYING, "§eSímbolo do Sacerdote")
            );

            case SURVIVAL -> equipment.setItemInMainHand(
                    criarItemVisual(Material.TOTEM_OF_UNDYING, "§2Totem do Mundo dos Vivos")
            );
        }
    }

    private ItemStack criarItemVisual(Material material, String nome) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(legacy(nome));
            item.setItemMeta(meta);
        }

        return item;
    }

    private void mostrarLegendaSeMirando(Player player) {
        Entity target = player.getTargetEntity(DISTANCIA_MIRA_LEGENDA, false);

        if (target == null) {
            return;
        }

        Optional<LimiarTotemTipo> optionalTipo = getTipoTotem(target);

        if (optionalTipo.isEmpty()) {
            return;
        }

        LimiarTotemTipo tipo = optionalTipo.get();
        PlayerProfile profile = playerProfileService.getOrLoad(player);

        if (tipo.isTotemSurvival()) {
            if (profile.hasClasse()) {
                player.sendActionBar(legacy("§2§l" + tipo.getNomeExibicao() + " §8» §7Clique para atravessar"));
            } else {
                player.sendActionBar(legacy("§8§l" + tipo.getNomeExibicao() + " §8» §7Escolha uma classe primeiro"));
            }

            return;
        }

        if (profile.hasClasse()) {
            player.sendActionBar(legacy("§8§l" + tipo.getNomeExibicao() + " §8» §7Sua alma já escolheu"));
        } else {
            player.sendActionBar(legacy("§6§l" + tipo.getNomeExibicao() + " §8» §7Clique para ouvir este caminho"));
        }
    }

    private Optional<LimiarTotemTipo> getTipoTotem(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        String tipoId = entity.getPersistentDataContainer().get(keyTotemTipo, PersistentDataType.STRING);

        if (tipoId == null || tipoId.isBlank()) {
            return Optional.empty();
        }

        return LimiarTotemTipo.fromString(tipoId);
    }

    private void efeitoEscolhaSucesso(Player player, LimiarTotemTipo tipo) {
        Location location = player.getLocation().add(0.0, 1.0, 0.0);

        player.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                location,
                90,
                0.5,
                0.8,
                0.5,
                0.15
        );

        player.getWorld().spawnParticle(
                Particle.ENCHANT,
                location,
                60,
                0.6,
                0.9,
                0.6,
                0.1
        );

        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        player.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
    }

    private void efeitoEscolhaNegada(Player player) {
        Location location = player.getLocation().add(0.0, 1.0, 0.0);

        player.getWorld().spawnParticle(
                Particle.SMOKE,
                location,
                35,
                0.35,
                0.45,
                0.35,
                0.02
        );

        player.getWorld().spawnParticle(
                Particle.ASH,
                location,
                25,
                0.35,
                0.45,
                0.35,
                0.02
        );

        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 0.6f);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.7f);
    }

    private void efeitoBloqueado(Player player) {
        Location location = player.getLocation().add(0.0, 1.0, 0.0);

        player.getWorld().spawnParticle(
                Particle.SMOKE,
                location,
                45,
                0.5,
                0.7,
                0.5,
                0.03
        );

        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.9f, 0.6f);
    }

    private void configurarLimiar(World world) {
        world.setSpawnLocation(0, 81, 0);

        world.setTime(18000);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.SPAWN_PATROLS, false);
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
        world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
        world.setGameRule(GameRules.KEEP_INVENTORY, true);
        world.setGameRule(GameRules.MOB_GRIEFING, false);
        world.setGameRule(GameRules.RESPAWN_RADIUS, 0);
    }

    private void configurarSurvival(World world) {
        world.setGameRule(GameRules.RESPAWN_RADIUS, 0);
    }

    private void criarBaseInicial(World world) {
        int y = 80;

        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                world.getBlockAt(x, y, z).setType(Material.POLISHED_DEEPSLATE);
            }
        }

        world.getBlockAt(0, y + 1, 0).setType(Material.SOUL_LANTERN);
    }

    private Location centralizarNoBloco(Location origem) {
        World world = origem.getWorld();

        if (world == null) {
            throw new IllegalStateException("Não é possível centralizar uma localização sem mundo.");
        }

        return new Location(
                world,
                origem.getBlockX() + 0.5,
                origem.getBlockY(),
                origem.getBlockZ() + 0.5,
                origem.getYaw(),
                0.0f
        );
    }

    private Location getLimiarSpawn(World fallbackWorld) {
        return carregarLocation(CONFIG_LIMIAR_SPAWN)
                .orElse(new Location(fallbackWorld, 0.5, 81.0, 0.5, 0.0f, 0.0f));
    }

    private Location getSurvivalSpawn(World fallbackWorld) {
        return carregarLocation(CONFIG_SURVIVAL_SPAWN)
                .orElse(fallbackWorld.getSpawnLocation().clone().add(0.5, 1.0, 0.5));
    }

    private void salvarLimiarSpawn(Location location) {
        salvarLocation(CONFIG_LIMIAR_SPAWN, location);
    }

    private void salvarSurvivalSpawn(Location location) {
        salvarLocation(CONFIG_SURVIVAL_SPAWN, location);
    }

    private void salvarLocation(String path, Location location) {
        FileConfiguration config = plugin.getConfig();

        config.set(path + ".world", location.getWorld() == null ? null : location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        plugin.saveConfig();
    }

    private Optional<Location> carregarLocation(String path) {
        FileConfiguration config = plugin.getConfig();

        if (!config.contains(path + ".world")) {
            return Optional.empty();
        }

        String worldName = config.getString(path + ".world");

        if (worldName == null || worldName.isBlank()) {
            return Optional.empty();
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return Optional.empty();
        }

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return Optional.of(new Location(world, x, y, z, yaw, pitch));
    }

    private String getSkinUrl(LimiarTotemTipo tipo) {
        return plugin.getConfig().getString(CONFIG_MANNEQUIN_SKINS + "." + tipo.getId() + ".url", tipo.getSkinUrlPadrao());
    }

    private PlayerTextures.SkinModel getSkinModel(LimiarTotemTipo tipo) {
        String valor = plugin.getConfig().getString(CONFIG_MANNEQUIN_SKINS + "." + tipo.getId() + ".model", "CLASSIC");

        if (valor == null || valor.isBlank()) {
            return PlayerTextures.SkinModel.CLASSIC;
        }

        try {
            return PlayerTextures.SkinModel.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("[Limiar] Skin model inválido para " + tipo.getId() + ": " + valor + ". Usando CLASSIC.");
            return PlayerTextures.SkinModel.CLASSIC;
        }
    }

    private String gerarNomeProfile(LimiarTotemTipo tipo) {
        return switch (tipo) {
            case TANQUE -> "MZ_Tanque";
            case GUERREIRO -> "MZ_Guerreiro";
            case ATIRADOR -> "MZ_Atirador";
            case MAGO -> "MZ_Mago";
            case SACERDOTE -> "MZ_Sacerdote";
            case SURVIVAL -> "MZ_Survival";
        };
    }

    private String getDescricaoTotem(LimiarTotemTipo tipo) {
        return switch (tipo) {
            case TANQUE -> "§7Caminho da resistência";
            case GUERREIRO -> "§7Caminho da lâmina";
            case ATIRADOR -> "§7Caminho da precisão";
            case MAGO -> "§7Caminho do arcano";
            case SACERDOTE -> "§7Caminho da fé";
            case SURVIVAL -> "§7Travessia para o mundo dos vivos";
        };
    }

    private String formatarStatusMundo(World world) {
        return world == null ? "§cNão carregado" : "§aCarregado";
    }

    private String formatarLocationConfig(String path) {
        Optional<Location> optionalLocation = carregarLocation(path);
        return optionalLocation.map(this::formatarLocation).orElse("§8não definido");
    }

    private String formatarLocation(Location location) {
        if (location.getWorld() == null) {
            return "mundo inválido";
        }

        return location.getWorld().getName()
                + ", "
                + String.format("%.2f", location.getX())
                + ", "
                + String.format("%.2f", location.getY())
                + ", "
                + String.format("%.2f", location.getZ())
                + ", yaw "
                + String.format("%.2f", location.getYaw());
    }

    private void garantirConfigPadrao() {
        FileConfiguration config = plugin.getConfig();

        for (LimiarTotemTipo tipo : LimiarTotemTipo.values()) {
            String basePath = CONFIG_MANNEQUIN_SKINS + "." + tipo.getId();

            config.addDefault(basePath + ".url", tipo.getSkinUrlPadrao());
            config.addDefault(basePath + ".model", "CLASSIC");
        }

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    private Component legacy(String texto) {
        return LegacyComponentSerializer.legacySection().deserialize(texto);
    }
}
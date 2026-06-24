package com.engenhoso.serverplugin.features.classes.habilidades.skillbar;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import com.engenhoso.serverplugin.features.classes.habilidades.ClasseResolver;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeDefinicao;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeLoadout;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeRegistry;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeService;
import com.engenhoso.serverplugin.features.classes.habilidades.TipoHabilidade;
import com.engenhoso.serverplugin.features.classes.habilidades.execucao.HabilidadeExecutorService;
import com.engenhoso.serverplugin.features.limiar.LimiarService;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HabilidadeSkillBarService {

    private static final int SLOT_COMUM_1 = 0;
    private static final int SLOT_COMUM_2 = 1;
    private static final int SLOT_COMUM_3 = 2;
    private static final int SLOT_COMUM_4 = 3;
    private static final int SLOT_ULTIMATE = 4;
    private static final int SLOT_PASSIVA = 8;

    private final JavaPlugin plugin;
    private final HabilidadeRegistry registry;
    private final HabilidadeService habilidadeService;
    private final ClasseResolver classeResolver;
    private final HabilidadeExecutorService executorService;

    private final NamespacedKey keySkillBar;
    private final NamespacedKey keyHabilidadeId;

    private final Map<UUID, EstadoHotbar> estados = new HashMap<>();

    public HabilidadeSkillBarService(
            JavaPlugin plugin,
            HabilidadeRegistry registry,
            HabilidadeService habilidadeService,
            ClasseResolver classeResolver,
            HabilidadeExecutorService executorService
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.habilidadeService = habilidadeService;
        this.classeResolver = classeResolver;
        this.executorService = executorService;

        this.keySkillBar = new NamespacedKey(plugin, "habilidade_skill_bar");
        this.keyHabilidadeId = new NamespacedKey(plugin, "habilidade_id");
    }

    public boolean podeAtivarModoSkill(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        if (estaNoLimiar(player)) {
            return false;
        }

        ClasseTipo classe = classeResolver.obterClasse(player);

        return classe != null;
    }

    public boolean estaEmModoSkill(Player player) {
        return estados.containsKey(player.getUniqueId());
    }

    public boolean estaNoLimiar(Player player) {
        return player.getWorld().getName().equalsIgnoreCase(LimiarService.LIMIAR_WORLD_NAME);
    }

    public void alternarModoSkill(Player player) {
        if (estaEmModoSkill(player)) {
            desativarModoSkill(player);
            return;
        }

        ativarModoSkill(player);
    }

    public void ativarModoSkill(Player player) {
        if (estaEmModoSkill(player)) {
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (estaNoLimiar(player)) {
            player.sendMessage(ChatColor.DARK_PURPLE + "O Limiar silencia suas habilidades.");
            return;
        }

        ClasseTipo classe = classeResolver.obterClasse(player);

        if (classe == null) {
            player.sendMessage(ChatColor.RED + "Você precisa escolher uma classe antes de usar habilidades.");
            return;
        }

        HabilidadeLoadout loadout = habilidadeService.getLoadout(player.getUniqueId());

        if (!possuiAlgumaHabilidadeEquipada(loadout)) {
            player.sendMessage(ChatColor.RED + "Você ainda não equipou habilidades. Use /habilidades.");
            return;
        }

        PlayerInventory inventory = player.getInventory();

        ItemStack[] hotbarOriginal = new ItemStack[9];

        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            hotbarOriginal[i] = item == null ? null : item.clone();
        }

        int slotOriginal = inventory.getHeldItemSlot();

        estados.put(
                player.getUniqueId(),
                new EstadoHotbar(hotbarOriginal, slotOriginal)
        );

        aplicarHotbarDeHabilidades(player, loadout, classe);

        inventory.setHeldItemSlot(SLOT_COMUM_1);
        player.updateInventory();

        player.sendMessage(ChatColor.DARK_PURPLE + "Hotbar de habilidades ativada.");
    }

    public void desativarModoSkill(Player player) {
        EstadoHotbar estado = estados.remove(player.getUniqueId());

        if (estado == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, clonar(estado.getHotbarOriginal()[i]));
        }

        inventory.setHeldItemSlot(estado.getSlotOriginal());
        player.updateInventory();
    }

    public void executarSkillSelecionada(Player player) {
        EstadoHotbar estado = estados.get(player.getUniqueId());

        if (estado == null) {
            return;
        }

        if (estaNoLimiar(player)) {
            desativarModoSkill(player);
            player.sendMessage(ChatColor.DARK_PURPLE + "O Limiar desfaz sua tentativa de usar habilidades.");
            return;
        }

        int slot = player.getInventory().getHeldItemSlot();

        if (slot == SLOT_PASSIVA) {
            player.sendMessage(ChatColor.YELLOW + "Passivas funcionam automaticamente.");
            return;
        }

        String habilidadeId = estado.getHabilidadePorSlot(slot);

        if (habilidadeId == null || habilidadeId.isBlank()) {
            player.sendMessage(ChatColor.RED + "Nenhuma habilidade equipada nesse slot.");
            return;
        }

        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            player.sendMessage(ChatColor.RED + "Habilidade inválida.");
            return;
        }

        ClasseTipo classeAtual = classeResolver.obterClasse(player);

        if (classeAtual == null) {
            player.sendMessage(ChatColor.RED + "Você precisa escolher uma classe antes de usar habilidades.");
            return;
        }

        if (habilidade.getClasse() != classeAtual) {
            player.sendMessage(ChatColor.RED + "Essa habilidade não pertence à sua classe atual.");
            return;
        }

        if (habilidade.getTipo() == TipoHabilidade.PASSIVA) {
            player.sendMessage(ChatColor.YELLOW + "Passivas funcionam automaticamente.");
            return;
        }

        executorService.executar(player, habilidade);
    }

    public boolean ehItemDaSkillBar(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        Byte marcado = item.getItemMeta()
                .getPersistentDataContainer()
                .get(keySkillBar, PersistentDataType.BYTE);

        return marcado != null && marcado == (byte) 1;
    }

    public void limparItensDaSkillBar(Player player) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (ehItemDaSkillBar(item)) {
                inventory.setItem(i, null);
            }
        }

        ItemStack[] armaduras = inventory.getArmorContents();

        for (int i = 0; i < armaduras.length; i++) {
            if (ehItemDaSkillBar(armaduras[i])) {
                armaduras[i] = null;
            }
        }

        inventory.setArmorContents(armaduras);

        if (ehItemDaSkillBar(inventory.getItemInOffHand())) {
            inventory.setItemInOffHand(null);
        }

        player.updateInventory();
    }

    public void aoDeslogar(Player player) {
        if (estaEmModoSkill(player)) {
            desativarModoSkill(player);
        }

        limparItensDaSkillBar(player);
    }

    public void aoMorrer(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EstadoHotbar estado = estados.remove(player.getUniqueId());

        event.getDrops().removeIf(this::ehItemDaSkillBar);

        if (estado == null) {
            limparItensDaSkillBar(player);
            return;
        }

        if (event.getKeepInventory()) {
            PlayerInventory inventory = player.getInventory();

            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, clonar(estado.getHotbarOriginal()[i]));
            }

            inventory.setHeldItemSlot(estado.getSlotOriginal());
            limparItensDaSkillBar(player);
            player.updateInventory();
            return;
        }

        for (ItemStack item : estado.getHotbarOriginal()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            event.getDrops().add(item.clone());
        }
    }

    public void parar() {
        List<UUID> jogadores = new ArrayList<>(estados.keySet());

        for (UUID uuid : jogadores) {
            Player player = plugin.getServer().getPlayer(uuid);

            if (player != null) {
                desativarModoSkill(player);
                limparItensDaSkillBar(player);
            } else {
                estados.remove(uuid);
            }
        }
    }

    private void aplicarHotbarDeHabilidades(
            Player player,
            HabilidadeLoadout loadout,
            ClasseTipo classe
    ) {
        PlayerInventory inventory = player.getInventory();
        EstadoHotbar estado = estados.get(player.getUniqueId());

        if (estado == null) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, criarFundo());
            estado.definirHabilidadeNoSlot(i, "");
        }

        colocarComum(
                player,
                inventory,
                estado,
                SLOT_COMUM_1,
                loadout.getComum(0),
                "Skill 1",
                classe
        );

        colocarComum(
                player,
                inventory,
                estado,
                SLOT_COMUM_2,
                loadout.getComum(1),
                "Skill 2",
                classe
        );

        colocarComum(
                player,
                inventory,
                estado,
                SLOT_COMUM_3,
                loadout.getComum(2),
                "Skill 3",
                classe
        );

        colocarComum(
                player,
                inventory,
                estado,
                SLOT_COMUM_4,
                loadout.getComum(3),
                "Skill 4",
                classe
        );

        colocarUltimate(
                player,
                inventory,
                estado,
                SLOT_ULTIMATE,
                loadout.getUltimate(),
                classe
        );

        colocarPassiva(
                player,
                inventory,
                estado,
                SLOT_PASSIVA,
                loadout.getPassiva(),
                classe
        );

        player.updateInventory();
    }

    private void colocarComum(
            Player player,
            PlayerInventory inventory,
            EstadoHotbar estado,
            int slot,
            String habilidadeId,
            String titulo,
            ClasseTipo classe
    ) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            inventory.setItem(slot, criarVazio(titulo, "Nenhuma habilidade comum equipada."));
            return;
        }

        if (habilidade.getClasse() != classe) {
            inventory.setItem(slot, criarVazio(titulo, "Habilidade não pertence à sua classe atual."));
            return;
        }

        if (habilidade.getTipo() != TipoHabilidade.COMUM) {
            inventory.setItem(slot, criarVazio(titulo, "Tipo inválido para esse slot."));
            return;
        }

        inventory.setItem(slot, criarItemHabilidade(player, habilidade, titulo));
        estado.definirHabilidadeNoSlot(slot, habilidade.getId());
    }

    private void colocarUltimate(
            Player player,
            PlayerInventory inventory,
            EstadoHotbar estado,
            int slot,
            String habilidadeId,
            ClasseTipo classe
    ) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            inventory.setItem(slot, criarVazio("Ultimate", "Nenhuma ultimate equipada."));
            return;
        }

        if (habilidade.getClasse() != classe) {
            inventory.setItem(slot, criarVazio("Ultimate", "Habilidade não pertence à sua classe atual."));
            return;
        }

        if (habilidade.getTipo() != TipoHabilidade.ULTIMATE) {
            inventory.setItem(slot, criarVazio("Ultimate", "Tipo inválido para esse slot."));
            return;
        }

        inventory.setItem(slot, criarItemHabilidade(player, habilidade, "Ultimate"));
        estado.definirHabilidadeNoSlot(slot, habilidade.getId());
    }

    private void colocarPassiva(
            Player player,
            PlayerInventory inventory,
            EstadoHotbar estado,
            int slot,
            String habilidadeId,
            ClasseTipo classe
    ) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            inventory.setItem(slot, criarVazio("Passiva", "Nenhuma passiva equipada."));
            return;
        }

        if (habilidade.getClasse() != classe) {
            inventory.setItem(slot, criarVazio("Passiva", "Habilidade não pertence à sua classe atual."));
            return;
        }

        if (habilidade.getTipo() != TipoHabilidade.PASSIVA) {
            inventory.setItem(slot, criarVazio("Passiva", "Tipo inválido para esse slot."));
            return;
        }

        inventory.setItem(slot, criarItemHabilidade(player, habilidade, "Passiva"));
        estado.definirHabilidadeNoSlot(slot, habilidade.getId());
    }

    private ItemStack criarItemHabilidade(
            Player player,
            HabilidadeDefinicao habilidade,
            String tituloSlot
    ) {
        ItemStack item = new ItemStack(habilidade.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        int nivel = habilidadeService.getNivel(player.getUniqueId(), habilidade);

        meta.setDisplayName(
                ChatColor.GOLD + tituloSlot + ": "
                        + ChatColor.YELLOW + habilidade.getNome()
        );

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + formatarTipo(habilidade.getTipo()));
        lore.add(ChatColor.GRAY + "Nível: " + ChatColor.WHITE + nivel + "/" + habilidade.getNivelMaximo());
        lore.add("");

        for (String linha : habilidade.getDescricao()) {
            lore.add(ChatColor.DARK_GRAY + linha);
        }

        lore.add("");

        if (habilidade.getTipo() == TipoHabilidade.PASSIVA) {
            lore.add(ChatColor.YELLOW + "Passiva automática.");
        } else {
            lore.add(ChatColor.YELLOW + "Clique direito para usar.");
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(
                keySkillBar,
                PersistentDataType.BYTE,
                (byte) 1
        );

        meta.getPersistentDataContainer().set(
                keyHabilidadeId,
                PersistentDataType.STRING,
                habilidade.getId()
        );

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack criarVazio(String tituloSlot, String motivo) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.DARK_GRAY + tituloSlot + ": vazio");

        meta.setLore(Arrays.asList(
                ChatColor.GRAY + motivo,
                ChatColor.YELLOW + "Use /habilidades para equipar."
        ));

        meta.getPersistentDataContainer().set(
                keySkillBar,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack criarFundo() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.DARK_GRAY + "Barra de habilidades");

        meta.getPersistentDataContainer().set(
                keySkillBar,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private boolean possuiAlgumaHabilidadeEquipada(HabilidadeLoadout loadout) {
        if (loadout == null) {
            return false;
        }

        String passiva = loadout.getPassiva();
        String ultimate = loadout.getUltimate();

        if (passiva != null && !passiva.isBlank()) {
            return true;
        }

        if (ultimate != null && !ultimate.isBlank()) {
            return true;
        }

        for (int i = 0; i < 4; i++) {
            String comum = loadout.getComum(i);

            if (comum != null && !comum.isBlank()) {
                return true;
            }
        }

        return false;
    }

    private String formatarTipo(TipoHabilidade tipo) {
        if (tipo == TipoHabilidade.PASSIVA) {
            return "Passiva";
        }

        if (tipo == TipoHabilidade.ULTIMATE) {
            return "Ultimate";
        }

        return "Comum";
    }

    private ItemStack clonar(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
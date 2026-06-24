package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HabilidadeMenu {

    private static final int TAMANHO = 54;

    private static final int SLOT_VOLTAR = 45;
    private static final int SLOT_PASSIVA = 46;
    private static final int SLOT_COMUM_1 = 47;
    private static final int SLOT_COMUM_2 = 48;
    private static final int SLOT_COMUM_3 = 49;
    private static final int SLOT_COMUM_4 = 50;
    private static final int SLOT_ULTIMATE = 51;
    private static final int SLOT_LIMPAR = 52;
    private static final int SLOT_SALVAR = 53;

    private static final int[] SLOTS_HABILIDADES = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final HabilidadeRegistry registry;
    private final HabilidadeService habilidadeService;
    private final ClasseResolver classeResolver;

    private final Map<UUID, String> selecionadas = new HashMap<>();
    private final Map<UUID, HabilidadeLoadout> rascunhos = new HashMap<>();

    public HabilidadeMenu(
            HabilidadeRegistry registry,
            HabilidadeService habilidadeService,
            ClasseResolver classeResolver
    ) {
        this.registry = registry;
        this.habilidadeService = habilidadeService;
        this.classeResolver = classeResolver;
    }

    public void abrir(Player player) {
        abrir(player, FiltroHabilidade.TODAS);
    }

    public void abrir(Player player, FiltroHabilidade filtro) {
        ClasseTipo classe = classeResolver.obterClasse(player);

        if (classe == null) {
            player.sendMessage(ChatColor.RED + "Voce precisa escolher uma classe antes de montar habilidades.");
            return;
        }

        UUID uuid = player.getUniqueId();

        rascunhos.putIfAbsent(uuid, habilidadeService.getLoadout(uuid));

        Inventory inv = Bukkit.createInventory(
                new HabilidadeMenuHolder(classe, filtro),
                TAMANHO,
                ChatColor.DARK_PURPLE + "Habilidades - " + classe.name()
        );

        preencherFundo(inv);
        colocarCabecalho(inv, player, classe);
        colocarFiltros(inv, filtro);
        colocarHabilidades(inv, player, classe, filtro);
        colocarLoadout(inv, player);

        player.openInventory(inv);
    }

    public void selecionarHabilidade(Player player, String habilidadeId) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            return;
        }

        if (!habilidadeService.isDesbloqueada(player.getUniqueId(), habilidade)) {
            player.sendMessage(ChatColor.RED + "Essa habilidade ainda esta bloqueada.");
            return;
        }

        selecionadas.put(player.getUniqueId(), habilidadeId);
        player.sendMessage(ChatColor.YELLOW + "Selecionado: " + ChatColor.GOLD + habilidade.getNome());
    }

    public void equiparSelecionada(Player player, int slotLoadout) {
        UUID uuid = player.getUniqueId();
        HabilidadeLoadout rascunho = rascunhos.get(uuid);

        if (rascunho == null) {
            rascunho = habilidadeService.getLoadout(uuid);
            rascunhos.put(uuid, rascunho);
        }

        String selecionadaId = selecionadas.getOrDefault(uuid, "");

        if (selecionadaId.isEmpty()) {
            removerSlot(player, slotLoadout, rascunho);
            abrir(player, getFiltroAtual(player));
            return;
        }

        HabilidadeDefinicao habilidade = registry.get(selecionadaId);

        if (habilidade == null) {
            return;
        }

        TipoHabilidade esperado = getTipoEsperado(slotLoadout);

        if (esperado == null) {
            return;
        }

        if (habilidade.getTipo() != esperado) {
            player.sendMessage(ChatColor.RED + "Essa habilidade nao pode ser equipada nesse espaco.");
            return;
        }

        if (esperado == TipoHabilidade.PASSIVA) {
            rascunho.setPassiva(habilidade.getId());
        } else if (esperado == TipoHabilidade.ULTIMATE) {
            rascunho.setUltimate(habilidade.getId());
        } else {
            int index = slotLoadout - SLOT_COMUM_1;
            rascunho.removerComumDuplicada(habilidade.getId());
            rascunho.setComum(index, habilidade.getId());
        }

        player.sendMessage(ChatColor.GREEN + habilidade.getNome() + " equipada.");
        abrir(player, getFiltroAtual(player));
    }

    public void limparRascunho(Player player) {
        HabilidadeLoadout rascunho = rascunhos.get(player.getUniqueId());

        if (rascunho == null) {
            rascunho = habilidadeService.getLoadout(player.getUniqueId());
            rascunhos.put(player.getUniqueId(), rascunho);
        }

        rascunho.limpar();
        selecionadas.remove(player.getUniqueId());

        player.sendMessage(ChatColor.YELLOW + "Loadout limpo. Clique em salvar para confirmar.");
        abrir(player, getFiltroAtual(player));
    }

    public void salvar(Player player) {
        UUID uuid = player.getUniqueId();
        HabilidadeLoadout rascunho = rascunhos.get(uuid);

        if (rascunho == null) {
            return;
        }

        habilidadeService.setLoadout(uuid, rascunho);
        selecionadas.remove(uuid);
        rascunhos.remove(uuid);

        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Habilidades salvas com sucesso.");
    }

    public void descartar(Player player) {
        selecionadas.remove(player.getUniqueId());
        rascunhos.remove(player.getUniqueId());
        player.closeInventory();
    }

    public HabilidadeRegistry getRegistry() {
        return registry;
    }

    public HabilidadeService getHabilidadeService() {
        return habilidadeService;
    }

    private void colocarCabecalho(Inventory inv, Player player, ClasseTipo classe) {
        inv.setItem(3, criarItem(
                Material.BOOK,
                ChatColor.GOLD + "Menu de Habilidades",
                Arrays.asList(
                        ChatColor.GRAY + "Classe atual: " + ChatColor.WHITE + classe.name(),
                        "",
                        ChatColor.YELLOW + "Clique em uma habilidade",
                        ChatColor.YELLOW + "e depois clique no slot desejado."
                ),
                false
        ));

        inv.setItem(4, criarItem(
                Material.EXPERIENCE_BOTTLE,
                ChatColor.AQUA + "Progressao",
                Arrays.asList(
                        ChatColor.GRAY + "Habilidades comuns: " + ChatColor.WHITE + "5 niveis",
                        ChatColor.GRAY + "Passivas: " + ChatColor.WHITE + "3 niveis",
                        ChatColor.GRAY + "Ultimates: " + ChatColor.WHITE + "3 niveis"
                ),
                false
        ));

        String selecionadaId = selecionadas.getOrDefault(player.getUniqueId(), "");
        HabilidadeDefinicao selecionada = registry.get(selecionadaId);

        if (selecionada == null) {
            inv.setItem(5, criarItem(
                    Material.GRAY_DYE,
                    ChatColor.GRAY + "Nenhuma habilidade selecionada",
                    Arrays.asList(
                            ChatColor.DARK_GRAY + "Selecione uma habilidade acima."
                    ),
                    false
            ));
        } else {
            inv.setItem(5, criarItem(
                    selecionada.getMaterial(),
                    ChatColor.YELLOW + "Selecionada: " + ChatColor.GOLD + selecionada.getNome(),
                    Arrays.asList(
                            ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + formatarTipo(selecionada.getTipo()),
                            ChatColor.GRAY + "Agora clique em um slot do loadout."
                    ),
                    true
            ));
        }
    }

    private void colocarFiltros(Inventory inv, FiltroHabilidade filtroAtual) {
        inv.setItem(37, criarFiltro(Material.COMPASS, "Todas", FiltroHabilidade.TODAS, filtroAtual));
        inv.setItem(38, criarFiltro(Material.SHIELD, "Passivas", FiltroHabilidade.PASSIVAS, filtroAtual));
        inv.setItem(39, criarFiltro(Material.IRON_SWORD, "Comuns", FiltroHabilidade.COMUNS, filtroAtual));
        inv.setItem(40, criarFiltro(Material.NETHER_STAR, "Ultimates", FiltroHabilidade.ULTIMATES, filtroAtual));
        inv.setItem(41, criarFiltro(Material.EXPERIENCE_BOTTLE, "Upaveis", FiltroHabilidade.UPAVEIS, filtroAtual));
        inv.setItem(42, criarFiltro(Material.BARRIER, "Bloqueadas", FiltroHabilidade.BLOQUEADAS, filtroAtual));
    }

    private void colocarHabilidades(Inventory inv, Player player, ClasseTipo classe, FiltroHabilidade filtro) {
        List<HabilidadeDefinicao> habilidades = registry.getPorClasse(classe);
        List<HabilidadeDefinicao> filtradas = new ArrayList<>();

        for (HabilidadeDefinicao habilidade : habilidades) {
            if (passaNoFiltro(player, habilidade, filtro)) {
                filtradas.add(habilidade);
            }
        }

        for (int i = 0; i < SLOTS_HABILIDADES.length; i++) {
            if (i >= filtradas.size()) {
                continue;
            }

            HabilidadeDefinicao habilidade = filtradas.get(i);
            inv.setItem(SLOTS_HABILIDADES[i], criarItemHabilidade(player, habilidade));
        }
    }

    private void colocarLoadout(Inventory inv, Player player) {
        UUID uuid = player.getUniqueId();
        HabilidadeLoadout rascunho = rascunhos.get(uuid);

        if (rascunho == null) {
            rascunho = habilidadeService.getLoadout(uuid);
            rascunhos.put(uuid, rascunho);
        }

        inv.setItem(SLOT_VOLTAR, criarItem(
                Material.ARROW,
                ChatColor.RED + "Voltar / Fechar",
                Collections.singletonList(ChatColor.GRAY + "Descarta alteracoes nao salvas."),
                false
        ));

        inv.setItem(SLOT_PASSIVA, criarSlotLoadout(
                "Passiva",
                rascunho.getPassiva(),
                TipoHabilidade.PASSIVA
        ));

        inv.setItem(SLOT_COMUM_1, criarSlotLoadout("Skill 1", rascunho.getComum(0), TipoHabilidade.COMUM));
        inv.setItem(SLOT_COMUM_2, criarSlotLoadout("Skill 2", rascunho.getComum(1), TipoHabilidade.COMUM));
        inv.setItem(SLOT_COMUM_3, criarSlotLoadout("Skill 3", rascunho.getComum(2), TipoHabilidade.COMUM));
        inv.setItem(SLOT_COMUM_4, criarSlotLoadout("Skill 4", rascunho.getComum(3), TipoHabilidade.COMUM));

        inv.setItem(SLOT_ULTIMATE, criarSlotLoadout(
                "Ultimate",
                rascunho.getUltimate(),
                TipoHabilidade.ULTIMATE
        ));

        inv.setItem(SLOT_LIMPAR, criarItem(
                Material.REDSTONE_BLOCK,
                ChatColor.RED + "Limpar loadout",
                Collections.singletonList(ChatColor.GRAY + "Remove todas as habilidades selecionadas."),
                false
        ));

        inv.setItem(SLOT_SALVAR, criarItem(
                Material.EMERALD_BLOCK,
                ChatColor.GREEN + "Salvar build",
                Arrays.asList(
                        ChatColor.GRAY + "Confirma as habilidades equipadas.",
                        ChatColor.YELLOW + "Use isso antes de sair."
                ),
                false
        ));
    }

    private ItemStack criarItemHabilidade(Player player, HabilidadeDefinicao habilidade) {
        UUID uuid = player.getUniqueId();
        boolean desbloqueada = habilidadeService.isDesbloqueada(uuid, habilidade);
        int nivel = habilidadeService.getNivel(uuid, habilidade);
        boolean selecionada = habilidade.getId().equalsIgnoreCase(selecionadas.getOrDefault(uuid, ""));

        Material material = desbloqueada ? habilidade.getMaterial() : Material.BARRIER;

        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + formatarTipo(habilidade.getTipo()));

        if (desbloqueada) {
            lore.add(ChatColor.GRAY + "Nivel: " + ChatColor.GREEN + nivel + "/" + habilidade.getNivelMaximo());
        } else {
            lore.add(ChatColor.RED + "Bloqueada");
        }

        lore.add("");

        for (String linha : habilidade.getDescricao()) {
            lore.add(ChatColor.DARK_GRAY + linha);
        }

        lore.add("");

        if (desbloqueada) {
            lore.add(ChatColor.YELLOW + "Clique esquerdo para selecionar.");

            if (habilidadeService.podeUpar(uuid, habilidade)) {
                lore.add(ChatColor.AQUA + "Pode ser upada.");
            } else {
                lore.add(ChatColor.GREEN + "Nivel maximo ou sem upgrade disponivel.");
            }
        } else {
            lore.add(ChatColor.RED + "Voce ainda nao desbloqueou essa habilidade.");
        }

        return criarItem(
                material,
                (desbloqueada ? ChatColor.GOLD : ChatColor.RED) + habilidade.getNome(),
                lore,
                selecionada
        );
    }

    private ItemStack criarSlotLoadout(String nomeSlot, String habilidadeId, TipoHabilidade tipo) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            Material material;

            if (tipo == TipoHabilidade.PASSIVA) {
                material = Material.GRAY_DYE;
            } else if (tipo == TipoHabilidade.ULTIMATE) {
                material = Material.NETHER_STAR;
            } else {
                material = Material.LIGHT_GRAY_DYE;
            }

            return criarItem(
                    material,
                    ChatColor.GRAY + nomeSlot + ": " + ChatColor.DARK_GRAY + "Vazio",
                    Arrays.asList(
                            ChatColor.GRAY + "Tipo aceito: " + ChatColor.WHITE + formatarTipo(tipo),
                            "",
                            ChatColor.YELLOW + "Selecione uma habilidade",
                            ChatColor.YELLOW + "e clique aqui para equipar."
                    ),
                    false
            );
        }

        return criarItem(
                habilidade.getMaterial(),
                ChatColor.GREEN + nomeSlot + ": " + ChatColor.GOLD + habilidade.getNome(),
                Arrays.asList(
                        ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + formatarTipo(habilidade.getTipo()),
                        "",
                        ChatColor.YELLOW + "Selecione outra habilidade para trocar.",
                        ChatColor.RED + "Clique sem habilidade selecionada para remover."
                ),
                true
        );
    }

    private ItemStack criarFiltro(Material material, String nome, FiltroHabilidade filtro, FiltroHabilidade atual) {
        boolean ativo = filtro == atual;

        return criarItem(
                material,
                (ativo ? ChatColor.GREEN : ChatColor.YELLOW) + nome,
                Collections.singletonList(ativo
                        ? ChatColor.GREEN + "Filtro ativo."
                        : ChatColor.GRAY + "Clique para filtrar."),
                ativo
        );
    }

    private ItemStack criarItem(Material material, String nome, List<String> lore, boolean brilho) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(nome);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (brilho) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void preencherFundo(Inventory inv) {
        ItemStack fundo = criarItem(
                Material.BLACK_STAINED_GLASS_PANE,
                " ",
                Collections.emptyList(),
                false
        );

        for (int i = 0; i < TAMANHO; i++) {
            inv.setItem(i, fundo);
        }
    }

    private boolean passaNoFiltro(Player player, HabilidadeDefinicao habilidade, FiltroHabilidade filtro) {
        UUID uuid = player.getUniqueId();
        boolean desbloqueada = habilidadeService.isDesbloqueada(uuid, habilidade);

        switch (filtro) {
            case PASSIVAS:
                return habilidade.getTipo() == TipoHabilidade.PASSIVA;
            case COMUNS:
                return habilidade.getTipo() == TipoHabilidade.COMUM;
            case ULTIMATES:
                return habilidade.getTipo() == TipoHabilidade.ULTIMATE;
            case UPAVEIS:
                return desbloqueada && habilidadeService.podeUpar(uuid, habilidade);
            case BLOQUEADAS:
                return !desbloqueada;
            case TODAS:
            default:
                return true;
        }
    }

    private TipoHabilidade getTipoEsperado(int slot) {
        if (slot == SLOT_PASSIVA) {
            return TipoHabilidade.PASSIVA;
        }

        if (slot >= SLOT_COMUM_1 && slot <= SLOT_COMUM_4) {
            return TipoHabilidade.COMUM;
        }

        if (slot == SLOT_ULTIMATE) {
            return TipoHabilidade.ULTIMATE;
        }

        return null;
    }

    private void removerSlot(Player player, int slotLoadout, HabilidadeLoadout rascunho) {
        if (slotLoadout == SLOT_PASSIVA) {
            rascunho.setPassiva("");
            player.sendMessage(ChatColor.YELLOW + "Passiva removida.");
            return;
        }

        if (slotLoadout >= SLOT_COMUM_1 && slotLoadout <= SLOT_COMUM_4) {
            int index = slotLoadout - SLOT_COMUM_1;
            rascunho.setComum(index, "");
            player.sendMessage(ChatColor.YELLOW + "Habilidade comum removida.");
            return;
        }

        if (slotLoadout == SLOT_ULTIMATE) {
            rascunho.setUltimate("");
            player.sendMessage(ChatColor.YELLOW + "Ultimate removida.");
        }
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

    private FiltroHabilidade getFiltroAtual(Player player) {
        if (player.getOpenInventory() == null) {
            return FiltroHabilidade.TODAS;
        }

        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof HabilidadeMenuHolder)) {
            return FiltroHabilidade.TODAS;
        }

        HabilidadeMenuHolder holder = (HabilidadeMenuHolder) player.getOpenInventory().getTopInventory().getHolder();
        return holder.getFiltro();
    }
}
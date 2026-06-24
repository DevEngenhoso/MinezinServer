package com.engenhoso.serverplugin.features.classes.habilidades.skillbar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public class HabilidadeSkillBarListener implements Listener {

    private final JavaPlugin plugin;
    private final HabilidadeSkillBarService skillBarService;

    public HabilidadeSkillBarListener(JavaPlugin plugin, HabilidadeSkillBarService skillBarService) {
        this.plugin = plugin;
        this.skillBarService = skillBarService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoUsarAtalhoDaSkillBar(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (skillBarService.estaNoLimiar(player)) {
            event.setCancelled(true);
            skillBarService.desativarModoSkill(player);
            skillBarService.limparItensDaSkillBar(player);
            player.sendMessage("§5O Limiar silencia suas habilidades.");
            return;
        }

        if (!skillBarService.podeAtivarModoSkill(player)) {
            return;
        }

        event.setCancelled(true);
        skillBarService.alternarModoSkill(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoClicarComSkillSelecionada(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (!skillBarService.estaEmModoSkill(player)) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        skillBarService.executarSkillSelecionada(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoDroparItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (skillBarService.ehItemDaSkillBar(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getItemDrop().remove();
            skillBarService.limparItensDaSkillBar(player);
            return;
        }

        if (skillBarService.estaEmModoSkill(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoClicarNoInventario(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (skillBarService.ehItemDaSkillBar(event.getCurrentItem())
                || skillBarService.ehItemDaSkillBar(event.getCursor())) {
            event.setCancelled(true);
            skillBarService.limparItensDaSkillBar(player);
            return;
        }

        if (!skillBarService.estaEmModoSkill(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoArrastarNoInventario(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (skillBarService.ehItemDaSkillBar(event.getOldCursor())) {
            event.setCancelled(true);
            skillBarService.limparItensDaSkillBar(player);
            return;
        }

        if (!skillBarService.estaEmModoSkill(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoSair(PlayerQuitEvent event) {
        skillBarService.aoDeslogar(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoSerExpulso(PlayerKickEvent event) {
        skillBarService.aoDeslogar(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (!player.isOnline()) {
                        return;
                    }

                    skillBarService.limparItensDaSkillBar(player);
                },
                1L
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoMorrer(PlayerDeathEvent event) {
        skillBarService.aoMorrer(event);
    }
}
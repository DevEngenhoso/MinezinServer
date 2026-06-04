package com.engenhoso.serverplugin.features.aura.skill;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AuraSkillBarListener implements Listener {

    private final AuraSkillBarService auraSkillBarService;

    public AuraSkillBarListener(AuraSkillBarService auraSkillBarService) {
        this.auraSkillBarService = auraSkillBarService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoApertarF(PlayerSwapHandItemsEvent event) {
        Player jogador = event.getPlayer();

        if (!auraSkillBarService.podeAtivarModoSkill(jogador)) {
            return;
        }

        event.setCancelled(true);
        auraSkillBarService.alternarModoSkill(jogador);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoClicarComSkillSelecionada(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player jogador = event.getPlayer();

        if (!auraSkillBarService.estaEmModoSkill(jogador)) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        auraSkillBarService.executarSkillSelecionada(jogador);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoDroparItem(PlayerDropItemEvent event) {
        if (!auraSkillBarService.estaEmModoSkill(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoClicarNoInventario(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player jogador)) {
            return;
        }

        if (!auraSkillBarService.estaEmModoSkill(jogador)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoArrastarNoInventario(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player jogador)) {
            return;
        }

        if (!auraSkillBarService.estaEmModoSkill(jogador)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        if (!auraSkillBarService.estaEmModoSkill(event.getPlayer())) {
            return;
        }

        auraSkillBarService.desativarModoSkill(event.getPlayer());
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent event) {
        Player jogador = event.getEntity();

        if (!auraSkillBarService.estaEmModoSkill(jogador)) {
            return;
        }

        event.getDrops().removeIf(auraSkillBarService::ehItemDaSkillBar);
        auraSkillBarService.desativarModoSkill(jogador);
    }
}

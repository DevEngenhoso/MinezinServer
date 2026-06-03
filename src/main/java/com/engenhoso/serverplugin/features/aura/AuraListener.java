package com.engenhoso.serverplugin.features.aura;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AuraListener implements Listener {

    private final AuraService auraService;

    public AuraListener(AuraService auraService) {
        this.auraService = auraService;
    }

    @EventHandler
    public void aoClicarComAuraCarregada(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!auraService.podeExecutarAvancoSombrio(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        auraService.executarAvancoSombrio(event.getPlayer());
    }
}
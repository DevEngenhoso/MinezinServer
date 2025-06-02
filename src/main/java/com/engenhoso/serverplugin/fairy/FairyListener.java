package com.engenhoso.serverplugin.fairy;

import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class FairyListener implements Listener {
    @EventHandler
    public void aoClicarNaFada(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Allay allay)) return;

        Player jogador = e.getPlayer();
        Fairy fada = FairyManager.getFada(jogador);

        if (fada == null) return;
        if (!fada.getEntidade().getUniqueId().equals(allay.getUniqueId())) return;

        // Garante que a flor não seja removida
        ItemStack flor = allay.getInventory().getItem(0);
        if (flor != null && flor.getType().isItem()) {
            allay.getInventory().setItem(0, flor.clone());
        }

        jogador.openInventory(fada.getInventario().getInventario());
    }
}
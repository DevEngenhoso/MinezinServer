package com.engenhoso.serverplugin.features.aura.ultimate;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class BlackHoleUltimateCraftListener implements Listener {

    private static final String NICK_DONO_DA_AURA = "Engenhoso";

    private final BlackHoleUltimateItem blackHoleUltimateItem;

    public BlackHoleUltimateCraftListener(BlackHoleUltimateItem blackHoleUltimateItem) {
        this.blackHoleUltimateItem = blackHoleUltimateItem;
    }

    @EventHandler
    public void aoPrepararCraft(PrepareItemCraftEvent event) {
        if (contemItemUltimate(event.getInventory().getMatrix())) {
            event.getInventory().setResult(null);
            return;
        }

        ItemStack resultado = event.getInventory().getResult();

        if (!blackHoleUltimateItem.ehItemUltimate(resultado)) {
            return;
        }

        Player jogador = obterJogador(event);

        if (jogador == null || !ehDonoDaAura(jogador)) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void aoCraftar(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player jogador)) {
            return;
        }

        if (event.getInventory() instanceof CraftingInventory craftingInventory) {
            if (contemItemUltimate(craftingInventory.getMatrix())) {
                event.setCancelled(true);
                jogador.sendMessage("§cA Singularidade Sombria não pode ser usada como ingrediente.");
                return;
            }
        }

        ItemStack resultado = event.getCurrentItem();

        if (!blackHoleUltimateItem.ehItemUltimate(resultado)) {
            return;
        }

        if (!ehDonoDaAura(jogador)) {
            event.setCancelled(true);
            jogador.sendMessage("§cApenas Engenhoso consegue craftar este item.");
        }
    }

    private boolean contemItemUltimate(ItemStack[] itens) {
        if (itens == null) {
            return false;
        }

        for (ItemStack item : itens) {
            if (blackHoleUltimateItem.ehItemUltimate(item)) {
                return true;
            }
        }

        return false;
    }

    private Player obterJogador(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer() instanceof Player jogador) {
            return jogador;
        }

        return null;
    }

    private boolean ehDonoDaAura(Player jogador) {
        return jogador.getName().equalsIgnoreCase(NICK_DONO_DA_AURA);
    }
}
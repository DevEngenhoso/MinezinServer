package com.engenhoso.serverplugin.modules.fairy.listeners;

import com.engenhoso.serverplugin.modules.fairy.core.FairyManager;
import com.engenhoso.serverplugin.modules.fairy.core.FairyReactionManager;
import com.engenhoso.serverplugin.modules.fairy.core.FairySituation;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FairyTalkListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void aoRetornarOverworld(PlayerChangedWorldEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (jogador.getWorld().getEnvironment() == World.Environment.NORMAL) {
            FairyReactionManager.reagir(FairySituation.PLAYER_RETURN_OVERWORLD, jogador);
        }
    }

    @EventHandler
    public void aoMatarCriatura(EntityDeathEvent e) {
        if (!(e.getEntity().getKiller() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_KILL_MOB, jogador);
    }

    @EventHandler
    public void aoEntrarDuranteTempestade(PlayerJoinEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (jogador.getWorld().hasStorm()) {
            FairyReactionManager.reagir(FairySituation.TIME_STORM, jogador);
        }
    }

    @EventHandler
    public void aoMinerar(BlockBreakEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;

        Material tipo = e.getBlock().getType();
        FairySituation situacao = null;

        switch (tipo) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON           -> situacao = FairySituation.MINERA_FERRO;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD           -> situacao = FairySituation.MINERA_OURO;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, DIAMOND      -> situacao = FairySituation.MINERA_DIAMANTE;
            case ANCIENT_DEBRIS, NETHERITE_SCRAP, NETHERITE_INGOT -> situacao = FairySituation.MINERA_NETHERITE;
            case AMETHYST_BLOCK, BUDDING_AMETHYST, AMETHYST_SHARD -> situacao = FairySituation.MINERA_AMETISTA;
        }

        if (situacao != null) {
            FairyReactionManager.reagir(situacao, jogador);
        }
    }

    @EventHandler
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getSlot() >= 5 && event.getSlot() <= 8) {
            ItemStack newItem = event.getCursor();

            if (newItem != null && newItem.getType().toString().matches(".*_(HELMET|CHESTPLATE|LEGGINGS|BOOTS)")) {
                FairyReactionManager.reagir(FairySituation.PLAYER_EQUIP_ARMOR, player);
            }
        }
    }
}

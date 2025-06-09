package com.engenhoso.serverplugin.modules.fairy.listeners;

import com.engenhoso.serverplugin.modules.fairy.core.FairyManager;
import com.engenhoso.serverplugin.modules.fairy.core.FairyReactionManager;
import com.engenhoso.serverplugin.modules.fairy.core.FairySituation;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
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

    @EventHandler
    public void aoCraftarItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;

        FairyReactionManager.reagir(FairySituation.PLAYER_CRAFT_ITEM, jogador);
    }

    @EventHandler
    public void aoJogarItem(PlayerDropItemEvent event) {
        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;

        FairyReactionManager.reagir(FairySituation.PLAYER_DROP_ITEM, jogador);
    }

    @EventHandler
    public void aoComerItem(PlayerItemConsumeEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_EAT_ITEM, jogador);
    }

    @EventHandler
    public void aoTrocarItemDeMao(PlayerSwapHandItemsEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_SWAP_ITEM, jogador);
    }

    @EventHandler
    public void aoDormir(PlayerBedEnterEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_SLEEP, jogador);
    }

    @EventHandler
    public void aoUsarBigorna(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block bloco = event.getClickedBlock();
        if (bloco == null || bloco.getType() != Material.ANVIL) return;

        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_ANVIL, jogador);
    }

    @EventHandler
    public void aoUsarMesaDeEncantamento(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block bloco = event.getClickedBlock();
        if (bloco == null || bloco.getType() != Material.ENCHANTING_TABLE) return;

        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_ENCHANTING, jogador);
    }

    @EventHandler
    public void aoUsarFornalha(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block bloco = event.getClickedBlock();
        if (bloco == null || bloco.getType() != Material.FURNACE) return;

        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_FURNACE, jogador);
    }

    @EventHandler
    public void aoDomarAnimal(EntityTameEvent event) {
        Player jogador = ((Player) event.getOwner());
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_TAME_ANIMAL, jogador);
    }

    @EventHandler
    public void aoMontarEmEntidade(EntityMountEvent event) {
        Player jogador = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_MOUNT_ENTITY, jogador);
    }

    @EventHandler
    public void aoUsarElytra(EntityToggleGlideEvent event) {
        Player jogador = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_ELYTRA, jogador);
    }

    @EventHandler
    public void aoRenascer(PlayerRespawnEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_RESPAWN, jogador);
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent event) {
        Player jogador = event.getEntity();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_DIES, jogador);
    }

    @EventHandler
    public void aoCompletarConquista(PlayerAdvancementDoneEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_ADVANCEMENT, jogador);
    }

    @EventHandler
    public void aoUsarTotem(EntityResurrectEvent event) {
        Player jogador = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_TOTEM, jogador);
    }

    @EventHandler
    public void aoEntrarNoCarrinho(VehicleEnterEvent event) {
        Player jogador = event.getEntered() instanceof Player ? (Player) event.getEntered() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_ENTER_MINECART, jogador);
    }

    @EventHandler
    public void aoUsarEscudo(EntityDamageByEntityEvent event) {
        Player jogador = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_SHIELD, jogador);
    }

    @EventHandler
    public void aoQuebrarItem(PlayerItemBreakEvent event) {
        Player jogador = event.getPlayer();
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_BREAK_ITEM, jogador);
    }

    @EventHandler
    public void aoLevarDanoDeFogo(EntityDamageEvent event) {
        Player jogador = event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FIRE ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_TAKE_FIRE_DAMAGE, jogador);
    }

    @EventHandler
    public void aoLevarDanoDeQueda(EntityDamageEvent event) {
        Player jogador = event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_TAKE_FALL_DAMAGE, jogador);
    }

    @EventHandler
    public void aoSerAtacado(EntityDamageByEntityEvent event) {
        Player jogador = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_UNDER_ATTACK, jogador);
    }

    @EventHandler
    public void aoEnfrentarBoss(EntityDamageByEntityEvent event) {
        Player jogador = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        if (jogador == null || !FairyManager.temFada(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_FIGHTS_BOSS, jogador);
    }
}

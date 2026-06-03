package com.engenhoso.serverplugin.features.aura.ultimate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlackHoleUltimateListener implements Listener {

    private final BlackHoleUltimateService blackHoleUltimateService;
    private final BlackHoleUltimateItem blackHoleUltimateItem;

    public BlackHoleUltimateListener(
            BlackHoleUltimateService blackHoleUltimateService,
            BlackHoleUltimateItem blackHoleUltimateItem
    ) {
        this.blackHoleUltimateService = blackHoleUltimateService;
        this.blackHoleUltimateItem = blackHoleUltimateItem;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoClicarComSingularidade(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (!blackHoleUltimateItem.ehItemUltimate(item)) {
            return;
        }

        event.setCancelled(true);
        blackHoleUltimateService.tentarArremessar(event.getPlayer());
    }

    @EventHandler
    public void aoProjetilAtingir(ProjectileHitEvent event) {
        Entity projetil = event.getEntity();

        if (!blackHoleUltimateItem.ehProjetilUltimate(projetil)) {
            return;
        }

        Player dono = null;

        if (projetil instanceof Projectile projectile && projectile.getShooter() instanceof Player jogador) {
            dono = jogador;
        }

        Location localImpacto = obterLocalImpacto(event);

        projetil.remove();

        blackHoleUltimateService.iniciarImplosaoEDomo(dono, localImpacto);
    }

    @EventHandler
    public void aoProjetilCausarDano(EntityDamageByEntityEvent event) {
        if (!blackHoleUltimateItem.ehProjetilUltimate(event.getDamager())) {
            return;
        }

        event.setCancelled(true);
    }

    private Location obterLocalImpacto(ProjectileHitEvent event) {
        if (event.getHitEntity() != null) {
            Entity entidade = event.getHitEntity();

            return entidade.getLocation().clone().add(
                    0,
                    Math.max(0.7, entidade.getHeight() * 0.5),
                    0
            );
        }

        if (event.getHitBlock() != null && event.getHitBlockFace() != null) {
            BlockFace face = event.getHitBlockFace();
            Vector direcaoFace = face.getDirection();

            return event.getHitBlock()
                    .getLocation()
                    .clone()
                    .add(0.5, 0.5, 0.5)
                    .add(direcaoFace.multiply(0.65));
        }

        return event.getEntity().getLocation().clone();
    }
}
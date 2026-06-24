package com.engenhoso.serverplugin.features.limiar;

import com.engenhoso.serverplugin.shared.hologram.HologramInterfaceService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LimiarListener implements Listener {

    private final JavaPlugin plugin;
    private final LimiarService limiarService;
    private final HologramInterfaceService hologramInterfaceService;

    private final Set<UUID> interacoesTratadasNesteTick = new HashSet<>();

    public LimiarListener(
            JavaPlugin plugin,
            LimiarService limiarService,
            HologramInterfaceService hologramInterfaceService
    ) {
        this.plugin = plugin;
        this.limiarService = limiarService;
        this.hologramInterfaceService = hologramInterfaceService;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            limiarService.enviarJogadorParaLimiar(player);
        }, 20L);
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        hologramInterfaceService.close(event.getPlayer());
    }

    @EventHandler
    public void aoClicarComMira(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (interacoesTratadasNesteTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        boolean tratado = hologramInterfaceService.handleRayClick(player);

        if (tratado) {
            marcarInteracaoTratada(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoInteragirComEntidadePrecisamente(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (interacoesTratadasNesteTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        Entity entity = event.getRightClicked();

        boolean interfaceTratada = hologramInterfaceService.handleInteractionAt(event);

        if (interfaceTratada) {
            marcarInteracaoTratada(player);
            event.setCancelled(true);
            return;
        }

        if (hologramInterfaceService.isHologramInterfaceEntity(entity)) {
            boolean rayTratado = hologramInterfaceService.handleRayClick(player);

            if (rayTratado) {
                marcarInteracaoTratada(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void aoInteragirComEntidade(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (interacoesTratadasNesteTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        Entity entity = event.getRightClicked();

        boolean interfaceTratada = hologramInterfaceService.handleInteraction(player, entity);

        if (interfaceTratada) {
            marcarInteracaoTratada(player);
            event.setCancelled(true);
            return;
        }

        if (hologramInterfaceService.isHologramInterfaceEntity(entity)) {
            boolean rayTratado = hologramInterfaceService.handleRayClick(player);

            if (rayTratado) {
                marcarInteracaoTratada(player);
                event.setCancelled(true);
            }

            return;
        }

        if (!(entity instanceof Mannequin)) {
            return;
        }

        boolean tratado = limiarService.tratarInteracaoTotem(player, entity);

        if (tratado) {
            marcarInteracaoTratada(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoReceberDano(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (limiarService.isTotem(entity) || hologramInterfaceService.isHologramInterfaceEntity(entity)) {
            event.setCancelled(true);
        }
    }

    private void marcarInteracaoTratada(Player player) {
        interacoesTratadasNesteTick.add(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () ->
                interacoesTratadasNesteTick.remove(player.getUniqueId())
        );
    }
}
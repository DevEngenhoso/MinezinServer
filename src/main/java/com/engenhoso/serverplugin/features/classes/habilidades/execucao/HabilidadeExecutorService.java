package com.engenhoso.serverplugin.features.classes.habilidades.execucao;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import com.engenhoso.serverplugin.features.classes.habilidades.ClasseResolver;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeDefinicao;
import com.engenhoso.serverplugin.features.classes.habilidades.HabilidadeService;
import com.engenhoso.serverplugin.features.classes.habilidades.TipoHabilidade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class HabilidadeExecutorService implements Listener {

    private final TankSkillService tankSkillService;

    public HabilidadeExecutorService(
            JavaPlugin plugin,
            HabilidadeService habilidadeService,
            ClasseResolver classeResolver
    ) {
        this.tankSkillService = new TankSkillService(plugin, habilidadeService, classeResolver);
    }

    public boolean executar(Player player, HabilidadeDefinicao habilidade) {
        if (habilidade == null) {
            return false;
        }

        if (habilidade.getTipo() == TipoHabilidade.PASSIVA) {
            player.sendMessage("§ePassivas funcionam automaticamente.");
            return true;
        }

        if (habilidade.getClasse() == ClasseTipo.TANQUE) {
            return tankSkillService.executar(player, habilidade.getId());
        }

        player.sendMessage("§cEssa habilidade ainda não foi implementada.");
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoCausarDano(EntityDamageByEntityEvent event) {
        tankSkillService.aoCausarDano(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoReceberDano(EntityDamageEvent event) {
        tankSkillService.aoReceberDano(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoMover(PlayerMoveEvent event) {
        tankSkillService.aoMover(event);
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        tankSkillService.limpar(event.getPlayer());
    }

    @EventHandler
    public void aoInteragir(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tankSkillService.estaAtordoadoPublic(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoTrocarSlot(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (tankSkillService.estaAtordoadoPublic(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoTrocarMao(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (tankSkillService.estaAtordoadoPublic(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void aoDano(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (tankSkillService.estaAtordoadoPublic(player)) {
                event.setCancelled(true);
            }
        }
    }
}
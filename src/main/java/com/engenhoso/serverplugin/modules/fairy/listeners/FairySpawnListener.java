package com.engenhoso.serverplugin.modules.fairy.listeners;

import com.engenhoso.serverplugin.MinezinServer;
import com.engenhoso.serverplugin.modules.fairy.core.Fairy;
import com.engenhoso.serverplugin.modules.fairy.core.FairyManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;


public class FairySpawnListener implements Listener {
    private final MinezinServer plugin;

    public FairySpawnListener(MinezinServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent e) {
        plugin.getDeathCountModule().mostrarSidebar(e.getPlayer());
        Fairy fada = FairyManager.criarOuSubstituirFada(e.getPlayer());
        fada.getInventario().carregarInventario(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent e) {
        Player jogador = e.getPlayer();
        Fairy fada = FairyManager.getFada(jogador);
        if (fada != null) {
            fada.getInventario().salvarInventario(jogador.getUniqueId());

            if (fada.getEntidade() != null && !fada.getEntidade().isDead()) {
                fada.getEntidade().remove();
            }

            FairyManager.removerFada(jogador);
        }
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent e) {
        Player jogador = e.getEntity();
        Fairy fada = FairyManager.getFada(jogador);
        if (fada != null && fada.getEntidade() != null && !fada.getEntidade().isDead()) {
            fada.getEntidade().remove();
        }
        FairyManager.removerFada(jogador);
    }

    @EventHandler
    public void aoRenascer(PlayerRespawnEvent e) {
        Player jogador = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Fairy fada = FairyManager.criarOuSubstituirFada(jogador);
            fada.getInventario().carregarInventario(jogador.getUniqueId());
        }, 20L);
    }

    @EventHandler
    public void aoTrocarMundo(PlayerChangedWorldEvent e) {
        Player jogador = e.getPlayer();
        World mundoAtual = jogador.getWorld();

        if (mundoAtual.getEnvironment() == World.Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Fairy fada = FairyManager.criarOuSubstituirFada(jogador);
                    fada.getInventario().carregarInventario(jogador.getUniqueId());
                }
            }.runTaskLater(plugin, 100L);
        }
    }

    @EventHandler
    public void aoVoltarParaOverworld(PlayerChangedWorldEvent event) {
        Player jogador = event.getPlayer();

        if (jogador.getWorld().getEnvironment() == World.Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    FairyManager.removerFada(jogador); // <--- adicionado
                    FairyManager.criarOuSubstituirFada(jogador);
                    jogador.sendMessage("§d[✧ Fada] §fAhhh, chão firme... estou de volta!");
                }
            }.runTaskLater(MinezinServer.getInstance(), 10L);
        }
    }

    @EventHandler
    public void aoEntrarEmPortal(PlayerPortalEvent event) {
        Player jogador = event.getPlayer();

        if (!FairyManager.temFada(jogador)) return;

        Fairy fada = FairyManager.getFada(jogador);
        if (fada != null && fada.getEntidade() != null && fada.getEntidade().isValid()) {
            fada.getEntidade().remove(); // Remove do mundo
        }

        FairyManager.removerFada(jogador);
        jogador.sendMessage("§d[✧ Fada] §fPortais e eu não nos damos muito bem... Adeus por agora!");
    }
}

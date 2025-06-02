package com.engenhoso.serverplugin.listeners;

import com.engenhoso.serverplugin.MinezinServer;
import com.engenhoso.serverplugin.fairy.Fairy;
import com.engenhoso.serverplugin.fairy.FairyManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final MinezinServer plugin;

    public PlayerListener(MinezinServer plugin) {
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
                fada.getEntidade().remove(); // Garante que a fada seja removida do mundo
            }

            FairyManager.removerFada(jogador);
        }
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent e) {
        Player jogador = e.getEntity();
        Fairy fada = FairyManager.getFada(jogador);
        if (fada != null && fada.getEntidade() != null && !fada.getEntidade().isDead()) {
            fada.getEntidade().remove(); // Remove a fada do mundo
        }
        FairyManager.removerFada(jogador);
    }

    @EventHandler
    public void aoRenascer(PlayerRespawnEvent e) {
        Player jogador = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Fairy fada = FairyManager.criarOuSubstituirFada(jogador);
            fada.getInventario().carregarInventario(jogador.getUniqueId());
        }, 20L); // 1 segundo após renascer
    }

    @EventHandler
    public void aoTrocarMundo(PlayerChangedWorldEvent e) {
        Player jogador = e.getPlayer();
        World mundoAtual = jogador.getWorld();

        // Se voltou para o overworld
        if (mundoAtual.getEnvironment() == World.Environment.NORMAL) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Fairy fada = FairyManager.criarOuSubstituirFada(jogador);
                    fada.getInventario().carregarInventario(jogador.getUniqueId());
                }
            }.runTaskLater(plugin, 100L); // 5 segundos depois de voltar
        }
    }
}

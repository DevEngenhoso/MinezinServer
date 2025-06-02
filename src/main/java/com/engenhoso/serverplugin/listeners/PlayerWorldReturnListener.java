package com.engenhoso.serverplugin.listeners;

import com.engenhoso.serverplugin.MinezinServer;
import com.engenhoso.serverplugin.fairy.FairyManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerWorldReturnListener implements Listener {

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
}

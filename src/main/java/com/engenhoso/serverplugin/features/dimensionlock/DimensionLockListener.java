package com.engenhoso.serverplugin.features.dimensionlock;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DimensionLockListener implements Listener {

    private final DimensionLockService dimensionLockService;

    public DimensionLockListener(DimensionLockService dimensionLockService) {
        this.dimensionLockService = dimensionLockService;
    }

    @EventHandler
    public void aoUsarPortal(PlayerPortalEvent event) {
        World.Environment destino = identificarDestino(event);

        if (destino == null) {
            return;
        }

        if (!dimensionLockService.estaTravado(destino)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(dimensionLockService.getMensagemBloqueio(destino));
    }

    @EventHandler
    public void aoTrocarDeMundo(PlayerChangedWorldEvent event) {
        verificarMundoAtual(event.getPlayer());
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        verificarMundoAtual(event.getPlayer());
    }

    private void verificarMundoAtual(Player jogador) {
        World.Environment ambienteAtual = jogador.getWorld().getEnvironment();

        if (!dimensionLockService.estaTravado(ambienteAtual)) {
            return;
        }

        dimensionLockService.teleportarParaOverworld(jogador);
        jogador.sendMessage(dimensionLockService.getMensagemBloqueio(ambienteAtual));
    }

    private World.Environment identificarDestino(PlayerPortalEvent event) {
        if (event.getTo() != null) {
            World.Environment environment = event.getTo().getWorld().getEnvironment();

            if (environment == World.Environment.NETHER || environment == World.Environment.THE_END) {
                return environment;
            }

            return null;
        }

        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            return World.Environment.NETHER;
        }

        if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return World.Environment.THE_END;
        }

        return null;
    }
}
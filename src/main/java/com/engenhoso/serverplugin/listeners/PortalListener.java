package com.engenhoso.serverplugin.listeners;

import com.engenhoso.serverplugin.fairy.Fairy;
import com.engenhoso.serverplugin.fairy.FairyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalListener implements Listener {

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

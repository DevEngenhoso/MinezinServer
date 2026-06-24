package com.engenhoso.serverplugin.shared.hologram;

import org.bukkit.entity.Player;

public class HologramInterfaceContext {

    private final HologramInterfaceService service;
    private final Player player;
    private final String interfaceId;
    private final String buttonId;

    private boolean closed;

    public HologramInterfaceContext(
            HologramInterfaceService service,
            Player player,
            String interfaceId,
            String buttonId
    ) {
        this.service = service;
        this.player = player;
        this.interfaceId = interfaceId;
        this.buttonId = buttonId;
        this.closed = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void close() {
        service.close(player);
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
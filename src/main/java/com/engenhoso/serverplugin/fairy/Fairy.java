package com.engenhoso.serverplugin.fairy;

import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Fairy {
    private final Player dono;
    private final Allay entidade;
    private final FairyInventory inventario;
    private final Map<String, Long> cooldowns;

    public Fairy(Player dono, Allay entidade) {
        this.dono = dono;
        this.entidade = entidade;
        this.inventario = new FairyInventory();
        this.cooldowns = new HashMap<>();
    }

    public Player getDono() {
        return dono;
    }

    public Allay getEntidade() {
        return entidade;
    }

    public FairyInventory getInventario() {
        return inventario;
    }

    public boolean podeUsar(String tipo) {
        return !cooldowns.containsKey(tipo) || cooldowns.get(tipo) < System.currentTimeMillis();
    }

    public void aplicarCooldown(String tipo, long segundos) {
        cooldowns.put(tipo, System.currentTimeMillis() + (segundos * 1000));
    }
}

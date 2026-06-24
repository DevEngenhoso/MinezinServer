package com.engenhoso.serverplugin.features.classes.habilidades.skillbar;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EstadoHotbar {

    private final ItemStack[] hotbarOriginal;
    private final int slotOriginal;
    private final Map<Integer, String> habilidadesPorSlot = new HashMap<>();

    public EstadoHotbar(ItemStack[] hotbarOriginal, int slotOriginal) {
        this.hotbarOriginal = hotbarOriginal;
        this.slotOriginal = slotOriginal;
    }

    public ItemStack[] getHotbarOriginal() {
        return hotbarOriginal;
    }

    public int getSlotOriginal() {
        return slotOriginal;
    }

    public void definirHabilidadeNoSlot(int slot, String habilidadeId) {
        habilidadesPorSlot.put(slot, habilidadeId);
    }

    public String getHabilidadePorSlot(int slot) {
        return habilidadesPorSlot.getOrDefault(slot, "");
    }
}
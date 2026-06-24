package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class HabilidadeMenuHolder implements InventoryHolder {

    private final ClasseTipo classe;
    private final FiltroHabilidade filtro;

    public HabilidadeMenuHolder(ClasseTipo classe, FiltroHabilidade filtro) {
        this.classe = classe;
        this.filtro = filtro;
    }

    public ClasseTipo getClasse() {
        return classe;
    }

    public FiltroHabilidade getFiltro() {
        return filtro;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
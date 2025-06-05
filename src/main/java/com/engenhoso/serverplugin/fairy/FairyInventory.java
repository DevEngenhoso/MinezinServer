package com.engenhoso.serverplugin.fairy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FairyInventory {
    private final Inventory inventario;
    private String nomeFada;

    public FairyInventory() {
        this.inventario = Bukkit.createInventory(null, 9, "Inventário da Fada");
        this.nomeFada = FairyNames.gerarNomeAleatorio(); // nome inicial padrão
    }

    public Inventory getInventario() {
        return inventario;
    }

    public String getNomeFada() {
        return nomeFada;
    }

    public void setNomeFada(String nome) {
        this.nomeFada = nome;
    }

    public void salvarInventario(UUID uuid) {
        File dir = new File("plugins/MinezinServer/fadas");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("inventario", inventario.getContents());
        config.set("nome", nomeFada);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void carregarInventario(UUID uuid) {
        File file = new File("plugins/MinezinServer/fadas", uuid.toString() + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> list = config.getList("inventario");
        if (list != null) {
            ItemStack[] contents = list.toArray(new ItemStack[0]);
            inventario.setContents(contents);
        }

        this.nomeFada = config.getString("nome", FairyNames.gerarNomeAleatorio());
    }
}

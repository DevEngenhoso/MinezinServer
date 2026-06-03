package com.engenhoso.serverplugin.features.aura.ultimate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class BlackHoleUltimateItem {

    private static final String NOME_ITEM =
            ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Singularidade Sombria";

    private final NamespacedKey itemKey;
    private final NamespacedKey projectileKey;

    public BlackHoleUltimateItem(JavaPlugin plugin) {
        this.itemKey = new NamespacedKey(plugin, "black_hole_ultimate_item");
        this.projectileKey = new NamespacedKey(plugin, "black_hole_ultimate_projectile");
    }

    public ItemStack criarItem() {
        ItemStack item = new ItemStack(Material.SNOWBALL, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(NOME_ITEM);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Uma bola de neve impossível.",
                ChatColor.DARK_GRAY + "Dobra o espaço-tempo ao atingir o mundo.",
                "",
                ChatColor.LIGHT_PURPLE + "Ultimate da Aura",
                ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "30 segundos"
        ));

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack criarItemVisualDoProjetil() {
        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Núcleo da Singularidade");
            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean ehItemUltimate(ItemStack item) {
        if (item == null) {
            return false;
        }

        if (item.getType() != Material.SNOWBALL) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        Byte valor = meta.getPersistentDataContainer().get(
                itemKey,
                PersistentDataType.BYTE
        );

        return valor != null && valor == (byte) 1;
    }

    public void marcarProjetil(Entity entidade) {
        entidade.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    public boolean ehProjetilUltimate(Entity entidade) {
        if (entidade == null) {
            return false;
        }

        Byte valor = entidade.getPersistentDataContainer().get(
                projectileKey,
                PersistentDataType.BYTE
        );

        return valor != null && valor == (byte) 1;
    }
}
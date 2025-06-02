package com.engenhoso.serverplugin.fairy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class FairyManager {
    private static final Map<UUID, Fairy> fadas = new HashMap<>();
    private static JavaPlugin plugin;

    private static final List<Material> FLORES = List.of(
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY
    );

    public static void init(JavaPlugin p) {
        plugin = p;
        Bukkit.getOnlinePlayers().forEach(FairyManager::criarOuSubstituirFada);
    }

    public static Fairy criarOuSubstituirFada(Player player) {
        removerFada(player);

        if (!player.getLocation().getChunk().isLoaded()) {
            player.getLocation().getChunk().load();
        }

        Allay allay = player.getWorld().spawn(player.getLocation(), Allay.class);

        Fairy fada = new Fairy(player, allay);
        fada.getInventario().carregarInventario(player.getUniqueId());

        allay.setCustomName("§d" + fada.getInventario().getNomeFada());
        allay.setCustomNameVisible(true);
        allay.setInvulnerable(true);
        allay.setSilent(true);
        allay.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                Integer.MAX_VALUE,
                254,
                false,
                false,
                false
        ));
        allay.setTarget(player);

        Material flor = FLORES.get(new Random().nextInt(FLORES.size()));
        allay.getEquipment().setItemInMainHand(new ItemStack(flor));

        fadas.put(player.getUniqueId(), fada);
        return fada;
    }

    public static Fairy getFada(Player player) {
        return fadas.get(player.getUniqueId());
    }

    public static boolean temFada(Player player) {
        return fadas.containsKey(player.getUniqueId());
    }

    public static void removerFada(Player player) {
        Fairy fada = fadas.remove(player.getUniqueId());

        if (fada != null && fada.getEntidade() != null && fada.getEntidade().isValid()) {
            fada.getEntidade().remove();
        }
    }
}

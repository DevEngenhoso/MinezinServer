package com.engenhoso.serverplugin.features.aura.ultimate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class BlackHoleUltimateRecipe {

    private final BlackHoleUltimateItem blackHoleUltimateItem;
    private final NamespacedKey receitaKey;

    public BlackHoleUltimateRecipe(JavaPlugin plugin, BlackHoleUltimateItem blackHoleUltimateItem) {
        this.blackHoleUltimateItem = blackHoleUltimateItem;
        this.receitaKey = new NamespacedKey(plugin, "singularidade_sombria");
    }

    public void registrar() {
        remover();

        ShapedRecipe receita = new ShapedRecipe(
                receitaKey,
                blackHoleUltimateItem.criarItem()
        );

        receita.shape(
                "CSC",
                "SBS",
                "CSC"
        );

        receita.setIngredient('C', Material.WITHER_SKELETON_SKULL);
        receita.setIngredient('S', Material.NETHER_STAR);
        receita.setIngredient('B', Material.SNOWBALL);

        Bukkit.addRecipe(receita);
    }

    public void remover() {
        Bukkit.removeRecipe(receitaKey);
    }
}
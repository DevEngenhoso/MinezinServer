package com.engenhoso.serverplugin.features.classes.habilidades;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HabilidadeService {

    private final JavaPlugin plugin;
    private final HabilidadeRegistry registry;
    private final File arquivo;
    private final FileConfiguration config;

    public HabilidadeService(JavaPlugin plugin, HabilidadeRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
        this.arquivo = new File(plugin.getDataFolder(), "habilidades.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = YamlConfiguration.loadConfiguration(arquivo);
    }

    public boolean isDesbloqueada(UUID uuid, HabilidadeDefinicao habilidade) {
        if (habilidade == null) {
            return false;
        }

        if (habilidade.isDesbloqueadaPorPadrao()) {
            return true;
        }

        return config.getBoolean(path(uuid) + ".unlocked." + habilidade.getId(), false);
    }

    public int getNivel(UUID uuid, HabilidadeDefinicao habilidade) {
        if (!isDesbloqueada(uuid, habilidade)) {
            return 0;
        }

        int nivel = config.getInt(path(uuid) + ".levels." + habilidade.getId(), 1);

        if (nivel < 1) {
            nivel = 1;
        }

        if (nivel > habilidade.getNivelMaximo()) {
            nivel = habilidade.getNivelMaximo();
        }

        return nivel;
    }

    public boolean podeUpar(UUID uuid, HabilidadeDefinicao habilidade) {
        if (!isDesbloqueada(uuid, habilidade)) {
            return false;
        }

        return getNivel(uuid, habilidade) < habilidade.getNivelMaximo();
    }

    public boolean upar(UUID uuid, String habilidadeId) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            return false;
        }

        if (!isDesbloqueada(uuid, habilidade)) {
            return false;
        }

        int nivelAtual = getNivel(uuid, habilidade);

        if (nivelAtual >= habilidade.getNivelMaximo()) {
            return false;
        }

        config.set(path(uuid) + ".levels." + habilidade.getId(), nivelAtual + 1);
        salvar();
        return true;
    }

    public void desbloquear(UUID uuid, String habilidadeId) {
        HabilidadeDefinicao habilidade = registry.get(habilidadeId);

        if (habilidade == null) {
            return;
        }

        config.set(path(uuid) + ".unlocked." + habilidade.getId(), true);

        if (config.getInt(path(uuid) + ".levels." + habilidade.getId(), 0) <= 0) {
            config.set(path(uuid) + ".levels." + habilidade.getId(), 1);
        }

        salvar();
    }

    public HabilidadeLoadout getLoadout(UUID uuid) {
        HabilidadeLoadout loadout = new HabilidadeLoadout();

        String base = path(uuid) + ".loadout";

        loadout.setPassiva(config.getString(base + ".passiva", ""));
        loadout.setUltimate(config.getString(base + ".ultimate", ""));

        List<String> comuns = config.getStringList(base + ".comuns");

        for (int i = 0; i < 4; i++) {
            String habilidadeId = "";

            if (i < comuns.size()) {
                habilidadeId = comuns.get(i);
            }

            loadout.setComum(i, habilidadeId);
        }

        return loadout;
    }

    public void setLoadout(UUID uuid, HabilidadeLoadout loadout) {
        String base = path(uuid) + ".loadout";

        config.set(base + ".passiva", loadout.getPassiva());
        config.set(base + ".ultimate", loadout.getUltimate());

        List<String> comuns = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            comuns.add(loadout.getComum(i));
        }

        config.set(base + ".comuns", comuns);

        salvar();
    }

    public String getPassivaEquipada(Player player) {
        return getLoadout(player.getUniqueId()).getPassiva();
    }

    public String getHabilidadeComumEquipada(Player player, int index) {
        return getLoadout(player.getUniqueId()).getComum(index);
    }

    public String getUltimateEquipada(Player player) {
        return getLoadout(player.getUniqueId()).getUltimate();
    }

    public void salvar() {
        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().severe("Nao foi possivel salvar habilidades.yml");
            e.printStackTrace();
        }
    }

    private String path(UUID uuid) {
        return "players." + uuid.toString();
    }
}
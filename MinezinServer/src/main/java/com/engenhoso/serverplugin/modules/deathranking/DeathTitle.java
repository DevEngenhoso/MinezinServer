package com.engenhoso.serverplugin.modules.deathranking;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DeathTitle implements Listener {

    private final Sound[] sonsAssustadores = {
            Sound.ENTITY_WITHER_DEATH,
            Sound.ENTITY_WITHER_SPAWN,
            Sound.ENTITY_ENDER_DRAGON_DEATH,
            Sound.ENTITY_ENDER_DRAGON_GROWL,
            Sound.ENTITY_SKELETON_DEATH,
            Sound.ENTITY_PHANTOM_DEATH,
            Sound.ENTITY_EVOKER_PREPARE_ATTACK,
            Sound.AMBIENT_CAVE,
            Sound.ENTITY_WARDEN_ANGRY,
            Sound.ENTITY_WARDEN_DEATH,
            Sound.ENTITY_WARDEN_SONIC_CHARGE
    };

    private final Particle[] particulasAssustadoras = {
            Particle.SCULK_SOUL,
            Particle.SOUL
    };

    private final Random random = new Random();
    private final JavaPlugin plugin;

    // Construtor
    public DeathTitle(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent e) {
        Player morto = e.getEntity();
        String nome = morto.getName();

        Sound somAleatorio = sonsAssustadores[random.nextInt(sonsAssustadores.length)];

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§cF no chat", "§7" + nome + " morreu.", 10, 60, 10);
            p.playSound(p.getLocation(), somAleatorio, 0.7f, 1.0f);
        }

        new BukkitRunnable() {
            int tempo = 0;

            @Override
            public void run() {
                if (tempo > 200) { // 200 ticks = 10 segundos
                    this.cancel();
                    return;
                }

                Particle particula = particulasAssustadoras[random.nextInt(particulasAssustadoras.length)];

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spawnParticle(particula, p.getLocation().add(2, 2, 2), 2, 2, 2, 2, 0.01);
                }

                tempo++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}

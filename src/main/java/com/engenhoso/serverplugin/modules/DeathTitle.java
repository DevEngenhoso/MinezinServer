package com.engenhoso.serverplugin.modules;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

    private final Map<UUID, Long> ultimasMensagensDeMorte = new HashMap<>();

    public DeathTitle(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoMorrer(PlayerDeathEvent e) {
        Player morto = e.getEntity();
        String nome = morto.getName();

        // Cancela a mensagem padrão do Minecraft para evitar duplicidade.
        e.setDeathMessage(null);

        if (podeEnviarMensagem(morto)) {
            Bukkit.broadcastMessage("§7" + nome + " morreu");
        }

        Sound somAleatorio = sonsAssustadores[random.nextInt(sonsAssustadores.length)];

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§cF no chat", "§7" + nome + " morreu.", 10, 60, 10);
            p.playSound(p.getLocation(), somAleatorio, 0.7f, 1.0f);
        }

        new BukkitRunnable() {
            int tempo = 0;

            @Override
            public void run() {
                if (tempo > 200) {
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

    private boolean podeEnviarMensagem(Player jogador) {
        long agora = System.currentTimeMillis();
        long ultima = ultimasMensagensDeMorte.getOrDefault(jogador.getUniqueId(), 0L);

        if (agora - ultima < 1000) {
            return false;
        }

        ultimasMensagensDeMorte.put(jogador.getUniqueId(), agora);
        return true;
    }
}
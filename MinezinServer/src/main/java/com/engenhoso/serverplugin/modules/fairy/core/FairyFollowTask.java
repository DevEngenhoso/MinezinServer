package com.engenhoso.serverplugin.modules.fairy.core;

import org.bukkit.*;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class FairyFollowTask extends BukkitRunnable {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void run() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            if (!FairyManager.temFada(jogador)) continue;

            Fairy fada = FairyManager.getFada(jogador);
            Allay entidade = fada.getEntidade();

            if (entidade == null || entidade.isDead()) continue;

            // Evita comparação de distância entre mundos diferentes
            if (!entidade.getWorld().equals(jogador.getWorld())) continue;

            // Seguir jogador
            if (jogador.getLocation().distance(entidade.getLocation()) > 15) {
                entidade.teleport(jogador.getLocation().add(0, 1, 0));
            }

            // Cura automática
            if (jogador.getHealth() <= 6.0) {
                UUID id = jogador.getUniqueId();
                long agora = System.currentTimeMillis();
                long ultimo = cooldowns.getOrDefault(id, 0L);

                if ((agora - ultimo) >= 20 * 60 * 1000) { // 20 minutos
                    // Lançar poção de regeneração II (10s)
                    SplashPotion pocao = jogador.getWorld().spawn(jogador.getLocation().add(0, 2, 0), SplashPotion.class);
                    ItemStack item = new ItemStack(Material.SPLASH_POTION);
                    PotionMeta meta = (PotionMeta) item.getItemMeta();
                    meta.setDisplayName("Poção da Fada");
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2), true); // 10s, nível 2
                    item.setItemMeta(meta);
                    pocao.setItem(item);
                    pocao.setVelocity(jogador.getLocation().getDirection().multiply(0.1));

                    // Efeitos mágicos
                    Location loc = jogador.getLocation().add(0, 1, 0);
                    World mundo = jogador.getWorld();

                    mundo.spawnParticle(Particle.END_ROD, loc, 30, 0.6, 0.8, 0.6, 0.05);
                    mundo.spawnParticle(Particle.GLOW, loc, 40, 0.5, 1, 0.5, 0.02);
                    mundo.spawnParticle(Particle.PORTAL, loc, 60, 0.8, 1.0, 0.8, 0.1);

                    mundo.playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1.2f);
                    mundo.playSound(loc, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 1.5f);

                    FairyReactionManager.reagir(FairySituation.PLAYER_TAKE_DAMAGE, jogador);

                    cooldowns.put(id, agora);
                }
            }

            if (jogador.getFireTicks() > 0 && fada.podeUsar("fogo")) {
                ItemStack splash = encontrarSplashComEfeito(fada, PotionEffectType.FIRE_RESISTANCE);
                if (splash != null) {

                    if (jogador.getLocation().distance(entidade.getLocation()) > 2) {
                        entidade.teleport(jogador.getLocation().add(0, 1, 0));
                    }

                    ThrownPotion arremessada = jogador.getWorld().spawn(fada.getEntidade().getLocation(), ThrownPotion.class);
                    arremessada.setItem(splash);
                    arremessada.setVelocity(jogador.getLocation().toVector().subtract(fada.getEntidade().getLocation().toVector()).normalize().multiply(0.5));

                    FairyReactionManager.reagir(FairySituation.PLAYER_TAKE_DAMAGE, jogador);
                    fada.getInventario().getInventario().removeItem(splash);
                    fada.aplicarCooldown("fogo", 600);
                }
            }
        }
    }

    private ItemStack encontrarSplashComEfeito(Fairy fada, PotionEffectType efeitoDesejado) {
        for (ItemStack item : fada.getInventario().getInventario().getContents()) {
            if (item == null || item.getType() != Material.SPLASH_POTION) continue;
            if (!(item.getItemMeta() instanceof PotionMeta meta)) continue;

            // Checa efeitos customizados (poções feitas via /give ou brew)
            if (meta.getCustomEffects().stream().anyMatch(e -> e.getType() == efeitoDesejado)) {
                return item;
            }

            // Checa o tipo base da poção (obsoleto, removido)
            PotionData data = meta.getBasePotionData();
            if (data.getType() == PotionType.FIRE_RESISTANCE) {
                return item;
            }
        }
        return null;
    }

}

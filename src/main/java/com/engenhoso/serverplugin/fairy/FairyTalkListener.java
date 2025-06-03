package com.engenhoso.serverplugin.fairy;

import com.engenhoso.serverplugin.MinezinServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class FairyTalkListener implements Listener {

    private final Random random = new Random();
    private final HashMap<UUID, Long> ultimoDialogo = new HashMap<>();

    private boolean podeFalar(Player jogador) {
        long agora = System.currentTimeMillis();
        long ultimo = ultimoDialogo.getOrDefault(jogador.getUniqueId(), 0L);
        if (agora - ultimo >= 5 * 60 * 1000) {
            ultimoDialogo.put(jogador.getUniqueId(), agora);
            return true;
        }
        return false;
    }

    @EventHandler
    public void aoRenascer(PlayerRespawnEvent event) {
        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;

        Bukkit.getScheduler().runTaskLater(MinezinServer.getInstance(), () -> {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_RESPAWN, jogador);
            }
        }, 40L);
    }

    @EventHandler
    public void aoLevarDano(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;

        if (random.nextDouble() < 0.4) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_TAKE_DAMAGE, jogador);
            }
        }
    }

    @EventHandler
    public void aoDroparItem(PlayerDropItemEvent event) {
        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;

        if (random.nextDouble() < 0.3) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_DROP_ITEM, jogador);
            }
        }
    }

    @EventHandler
    public void aoComer(PlayerItemConsumeEvent event) {
        Player jogador = event.getPlayer();
        if (!FairyManager.temFada(jogador)) return;

        if (random.nextDouble() < 0.5) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_EAT_ITEM, jogador);
            }
        }
    }

    @EventHandler
    public void aoCraftar(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;
        if (!event.getClick().isLeftClick() && !event.getClick().isShiftClick()) return;

        JavaPlugin plugin = MinezinServer.getInstance();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (random.nextDouble() < 0.4) {
                if (podeFalar(jogador)) {
                    FairyReactionManager.reagir(FairySituation.PLAYER_CRAFT_ITEM, jogador);
                }
            }
        }, 2L);
    }

    @EventHandler
    public void aoUsarTotem(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;
        if (!podeFalar(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_USE_TOTEM, jogador);
    }

    @EventHandler
    public void aoEntrarNoEnd(PlayerChangedWorldEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (jogador.getWorld().getEnvironment() == World.Environment.THE_END) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_ENTER_END, jogador);
            }
        }
    }

    @EventHandler
    public void aoRetornarOverworld(PlayerChangedWorldEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (jogador.getWorld().getEnvironment() == World.Environment.NORMAL) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.PLAYER_RETURN_OVERWORLD, jogador);
            }
        }
    }

    @EventHandler
    public void aoMatarCriatura(EntityDeathEvent e) {
        if (!(e.getEntity().getKiller() instanceof Player jogador)) return;
        if (!FairyManager.temFada(jogador)) return;
        if (!podeFalar(jogador)) return;
        FairyReactionManager.reagir(FairySituation.PLAYER_KILL_MOB, jogador);
    }

    @EventHandler
    public void aoEntrarDuranteTempestade(PlayerJoinEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (jogador.getWorld().hasStorm()) {
            if (podeFalar(jogador)) {
                FairyReactionManager.reagir(FairySituation.TIME_STORM, jogador);
            }
        }
    }

    @EventHandler
    public void aoMinerar(BlockBreakEvent e) {
        Player jogador = e.getPlayer();
        if (!FairyManager.temFada(jogador)) return;
        if (!podeFalar(jogador)) return;

        Material tipo = e.getBlock().getType();
        FairySituation situacao = null;

        switch (tipo) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON -> situacao = FairySituation.MINERA_FERRO;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD -> situacao = FairySituation.MINERA_OURO;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, DIAMOND -> situacao = FairySituation.MINERA_DIAMANTE;
            case ANCIENT_DEBRIS, NETHERITE_SCRAP, NETHERITE_INGOT -> situacao = FairySituation.MINERA_NETHERITE;
            case AMETHYST_BLOCK, BUDDING_AMETHYST, AMETHYST_SHARD -> situacao = FairySituation.MINERA_AMETISTA;
            default -> {{ /* nenhum minério relevante */ }}
        }

        if (situacao != null) {
            FairyReactionManager.reagir(situacao, jogador);
        }
    }
}

package com.engenhoso.serverplugin.features.classes.habilidades;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class HabilidadeMenuListener implements Listener {

    private final HabilidadeMenu menu;

    public HabilidadeMenuListener(HabilidadeMenu menu) {
        this.menu = menu;
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (!(event.getView().getTopInventory().getHolder() instanceof HabilidadeMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        HabilidadeMenuHolder holder = (HabilidadeMenuHolder) event.getView().getTopInventory().getHolder();

        if (slot == 37) {
            menu.abrir(player, FiltroHabilidade.TODAS);
            return;
        }

        if (slot == 38) {
            menu.abrir(player, FiltroHabilidade.PASSIVAS);
            return;
        }

        if (slot == 39) {
            menu.abrir(player, FiltroHabilidade.COMUNS);
            return;
        }

        if (slot == 40) {
            menu.abrir(player, FiltroHabilidade.ULTIMATES);
            return;
        }

        if (slot == 41) {
            menu.abrir(player, FiltroHabilidade.UPAVEIS);
            return;
        }

        if (slot == 42) {
            menu.abrir(player, FiltroHabilidade.BLOQUEADAS);
            return;
        }

        if (slot == 45) {
            menu.descartar(player);
            return;
        }

        if (slot >= 46 && slot <= 51) {
            menu.equiparSelecionada(player, slot);
            return;
        }

        if (slot == 52) {
            menu.limparRascunho(player);
            return;
        }

        if (slot == 53) {
            menu.salvar(player);
            return;
        }

        HabilidadeDefinicao clicada = obterHabilidadePeloSlot(player, holder, slot);

        if (clicada != null) {
            menu.selecionarHabilidade(player, clicada.getId());
            menu.abrir(player, holder.getFiltro());
        }
    }

    @EventHandler
    public void aoArrastar(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof HabilidadeMenuHolder) {
            event.setCancelled(true);
        }
    }

    private HabilidadeDefinicao obterHabilidadePeloSlot(Player player, HabilidadeMenuHolder holder, int slot) {
        int[] slotsHabilidades = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int indexVisual = -1;

        for (int i = 0; i < slotsHabilidades.length; i++) {
            if (slotsHabilidades[i] == slot) {
                indexVisual = i;
                break;
            }
        }

        if (indexVisual == -1) {
            return null;
        }

        int indexAtual = 0;

        for (HabilidadeDefinicao habilidade : menu.getRegistry().getPorClasse(holder.getClasse())) {
            boolean aparece = passaNoFiltro(player, habilidade, holder.getFiltro());

            if (!aparece) {
                continue;
            }

            if (indexAtual == indexVisual) {
                return habilidade;
            }

            indexAtual++;
        }

        return null;
    }

    private boolean passaNoFiltro(Player player, HabilidadeDefinicao habilidade, FiltroHabilidade filtro) {
        boolean desbloqueada = menu.getHabilidadeService().isDesbloqueada(player.getUniqueId(), habilidade);

        switch (filtro) {
            case PASSIVAS:
                return habilidade.getTipo() == TipoHabilidade.PASSIVA;
            case COMUNS:
                return habilidade.getTipo() == TipoHabilidade.COMUM;
            case ULTIMATES:
                return habilidade.getTipo() == TipoHabilidade.ULTIMATE;
            case UPAVEIS:
                return desbloqueada && menu.getHabilidadeService().podeUpar(player.getUniqueId(), habilidade);
            case BLOQUEADAS:
                return !desbloqueada;
            case TODAS:
            default:
                return true;
        }
    }
}
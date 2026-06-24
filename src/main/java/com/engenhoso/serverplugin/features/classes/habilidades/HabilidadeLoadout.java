package com.engenhoso.serverplugin.features.classes.habilidades;

import java.util.ArrayList;
import java.util.List;

public class HabilidadeLoadout {

    private String passiva;
    private final List<String> comuns;
    private String ultimate;

    public HabilidadeLoadout() {
        this.passiva = "";
        this.ultimate = "";
        this.comuns = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            this.comuns.add("");
        }
    }

    public HabilidadeLoadout copiar() {
        HabilidadeLoadout copia = new HabilidadeLoadout();
        copia.setPassiva(passiva);
        copia.setUltimate(ultimate);

        for (int i = 0; i < 4; i++) {
            copia.setComum(i, comuns.get(i));
        }

        return copia;
    }

    public String getPassiva() {
        return passiva == null ? "" : passiva;
    }

    public void setPassiva(String passiva) {
        this.passiva = passiva == null ? "" : passiva;
    }

    public String getUltimate() {
        return ultimate == null ? "" : ultimate;
    }

    public void setUltimate(String ultimate) {
        this.ultimate = ultimate == null ? "" : ultimate;
    }

    public List<String> getComuns() {
        return comuns;
    }

    public String getComum(int index) {
        if (index < 0 || index >= 4) {
            return "";
        }

        String habilidade = comuns.get(index);
        return habilidade == null ? "" : habilidade;
    }

    public void setComum(int index, String habilidadeId) {
        if (index < 0 || index >= 4) {
            return;
        }

        comuns.set(index, habilidadeId == null ? "" : habilidadeId);
    }

    public void limpar() {
        passiva = "";
        ultimate = "";

        for (int i = 0; i < 4; i++) {
            comuns.set(i, "");
        }
    }

    public void removerComumDuplicada(String habilidadeId) {
        if (habilidadeId == null || habilidadeId.isEmpty()) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (habilidadeId.equalsIgnoreCase(getComum(i))) {
                comuns.set(i, "");
            }
        }
    }
}
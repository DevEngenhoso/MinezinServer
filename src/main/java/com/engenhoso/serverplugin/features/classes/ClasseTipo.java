package com.engenhoso.serverplugin.features.classes;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Optional;

public enum ClasseTipo {

    TANQUE(
            "Tanque",
            "Classe corpo a corpo focada em resistência e chamar atenção.",
            Material.PLAYER_HEAD,
            9
    ),

    GUERREIRO(
            "Guerreiro",
            "Classe focada em dano corpo a corpo.",
            Material.PLAYER_HEAD,
            11
    ),

    ATIRADOR(
            "Atirador",
            "Classe focada em dano à distância usando arcos e bestas.",
            Material.PLAYER_HEAD,
            13
    ),

    MAGO(
            "Mago",
            "Classe focada em dano à distância usando magia.",
            Material.PLAYER_HEAD,
            15
    ),

    SACERDOTE(
            "Sacerdote",
            "Classe focada em suporte usando magia.",
            Material.PLAYER_HEAD,
            17
    );

    private final String nome;
    private final String descricao;
    private final Material icone;
    private final int slotMenu;

    ClasseTipo(String nome, String descricao, Material icone, int slotMenu) {
        this.nome = nome;
        this.descricao = descricao;
        this.icone = icone;
        this.slotMenu = slotMenu;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Material getIcone() {
        return icone;
    }

    public int getSlotMenu() {
        return slotMenu;
    }

    public static Optional<ClasseTipo> porSlot(int slot) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.getSlotMenu() == slot)
                .findFirst();
    }
}
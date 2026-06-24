package com.engenhoso.serverplugin.features.limiar;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.Material;

import java.util.Optional;

public enum LimiarTotemTipo {

    TANQUE(
            "tanque",
            "Tanque",
            ClasseTipo.TANQUE,
            Material.SHIELD,
            "https://textures.minecraft.net/texture/d2d419d03dd2b0786645e13cc0b40636f520092b8ab477e6aa9fd07d102b6de2"
    ),

    GUERREIRO(
            "guerreiro",
            "Guerreiro",
            ClasseTipo.GUERREIRO,
            Material.IRON_SWORD,
            "https://textures.minecraft.net/texture/83cc2c927773a28fb726a2c96d805bbb0dd869fbf533213933ecf9cbf8099a42"
    ),

    ATIRADOR(
            "atirador",
            "Atirador",
            ClasseTipo.ATIRADOR,
            Material.BOW,
            "https://textures.minecraft.net/texture/12c9bfaab4f9430cbd6eb37f22f0872b926f9456178a19011f2c08215a1c2b4b"
    ),

    MAGO(
            "mago",
            "Mago",
            ClasseTipo.MAGO,
            Material.BLAZE_ROD,
            "https://textures.minecraft.net/texture/e1b89fb810f833313256727c9ab91e939b9e68e80703afc384ff7b6d455568e3"
    ),

    SACERDOTE(
            "sacerdote",
            "Sacerdote",
            ClasseTipo.SACERDOTE,
            Material.TOTEM_OF_UNDYING,
            "https://textures.minecraft.net/texture/7c8a67612608032047ad365151d28e2851ced24fbcce048163dd3a9bd18dc8f7"
    ),

    SURVIVAL(
            "survival",
            "Portal do Survival",
            null,
            Material.TOTEM_OF_UNDYING,
            ""
    );

    private final String id;
    private final String nomeExibicao;
    private final ClasseTipo classeTipo;
    private final Material itemMaterial;
    private final String skinUrlPadrao;

    LimiarTotemTipo(
            String id,
            String nomeExibicao,
            ClasseTipo classeTipo,
            Material itemMaterial,
            String skinUrlPadrao
    ) {
        this.id = id;
        this.nomeExibicao = nomeExibicao;
        this.classeTipo = classeTipo;
        this.itemMaterial = itemMaterial;
        this.skinUrlPadrao = skinUrlPadrao;
    }

    public String getId() {
        return id;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public Optional<ClasseTipo> getClasseTipo() {
        return Optional.ofNullable(classeTipo);
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public String getSkinUrlPadrao() {
        return skinUrlPadrao;
    }

    public boolean isTotemClasse() {
        return classeTipo != null;
    }

    public boolean isTotemSurvival() {
        return this == SURVIVAL;
    }

    public static Optional<LimiarTotemTipo> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalizado = value.trim().toLowerCase();

        if (normalizado.equals("tank")) {
            return Optional.of(TANQUE);
        }

        if (normalizado.equals("arqueiro") || normalizado.equals("archer")) {
            return Optional.of(ATIRADOR);
        }

        for (LimiarTotemTipo tipo : values()) {
            if (tipo.name().equalsIgnoreCase(normalizado) || tipo.getId().equalsIgnoreCase(normalizado)) {
                return Optional.of(tipo);
            }
        }

        return Optional.empty();
    }
}
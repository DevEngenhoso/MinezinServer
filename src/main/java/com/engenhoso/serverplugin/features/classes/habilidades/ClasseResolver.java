package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface ClasseResolver {
    ClasseTipo obterClasse(Player player);
}
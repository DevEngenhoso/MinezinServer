package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.Material;

import java.util.List;

public class HabilidadeDefinicao {

    private final String id;
    private final String nome;
    private final ClasseTipo classe;
    private final TipoHabilidade tipo;
    private final Material material;
    private final List<String> descricao;
    private final boolean desbloqueadaPorPadrao;

    public HabilidadeDefinicao(
            String id,
            String nome,
            ClasseTipo classe,
            TipoHabilidade tipo,
            Material material,
            List<String> descricao,
            boolean desbloqueadaPorPadrao
    ) {
        this.id = id;
        this.nome = nome;
        this.classe = classe;
        this.tipo = tipo;
        this.material = material;
        this.descricao = descricao;
        this.desbloqueadaPorPadrao = desbloqueadaPorPadrao;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public ClasseTipo getClasse() {
        return classe;
    }

    public TipoHabilidade getTipo() {
        return tipo;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getDescricao() {
        return descricao;
    }

    public boolean isDesbloqueadaPorPadrao() {
        return desbloqueadaPorPadrao;
    }

    public int getNivelMaximo() {
        if (tipo == TipoHabilidade.COMUM) {
            return 5;
        }

        return 3;
    }
}
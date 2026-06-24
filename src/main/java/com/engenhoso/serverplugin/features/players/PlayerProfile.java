package com.engenhoso.serverplugin.features.players;

import java.util.UUID;

public class PlayerProfile {

    private final UUID uuid;

    private String name;
    private String classe;

    private boolean prologoConcluido;

    private int nivel;
    private long xp;
    private int pontosHabilidade;

    private String partyAtual;
    private String instanciaAtual;

    private String returnWorld;
    private Double returnX;
    private Double returnY;
    private Double returnZ;
    private Float returnYaw;
    private Float returnPitch;

    public PlayerProfile(
            UUID uuid,
            String name,
            String classe,
            boolean prologoConcluido,
            int nivel,
            long xp,
            int pontosHabilidade,
            String partyAtual,
            String instanciaAtual,
            String returnWorld,
            Double returnX,
            Double returnY,
            Double returnZ,
            Float returnYaw,
            Float returnPitch
    ) {
        this.uuid = uuid;
        this.name = name;
        this.classe = classe;
        this.prologoConcluido = prologoConcluido;
        this.nivel = nivel;
        this.xp = xp;
        this.pontosHabilidade = pontosHabilidade;
        this.partyAtual = partyAtual;
        this.instanciaAtual = instanciaAtual;
        this.returnWorld = returnWorld;
        this.returnX = returnX;
        this.returnY = returnY;
        this.returnZ = returnZ;
        this.returnYaw = returnYaw;
        this.returnPitch = returnPitch;
    }

    public static PlayerProfile criarNovo(UUID uuid, String name) {
        return new PlayerProfile(
                uuid,
                name,
                null,
                false,
                1,
                0L,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUuidString() {
        return uuid.toString();
    }

    public String getName() {
        return name;
    }

    public String getClasse() {
        return classe;
    }

    public boolean hasClasse() {
        return classe != null && !classe.isBlank();
    }

    public boolean isPrologoConcluido() {
        return prologoConcluido;
    }

    public int getNivel() {
        return nivel;
    }

    public long getXp() {
        return xp;
    }

    public int getPontosHabilidade() {
        return pontosHabilidade;
    }

    public String getPartyAtual() {
        return partyAtual;
    }

    public String getInstanciaAtual() {
        return instanciaAtual;
    }

    public String getReturnWorld() {
        return returnWorld;
    }

    public Double getReturnX() {
        return returnX;
    }

    public Double getReturnY() {
        return returnY;
    }

    public Double getReturnZ() {
        return returnZ;
    }

    public Float getReturnYaw() {
        return returnYaw;
    }

    public Float getReturnPitch() {
        return returnPitch;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public void setPrologoConcluido(boolean prologoConcluido) {
        this.prologoConcluido = prologoConcluido;
    }

    public void setNivel(int nivel) {
        this.nivel = Math.max(1, nivel);
    }

    public void setXp(long xp) {
        this.xp = Math.max(0L, xp);
    }

    public void setPontosHabilidade(int pontosHabilidade) {
        this.pontosHabilidade = Math.max(0, pontosHabilidade);
    }

    public void setPartyAtual(String partyAtual) {
        this.partyAtual = partyAtual;
    }

    public void setInstanciaAtual(String instanciaAtual) {
        this.instanciaAtual = instanciaAtual;
    }

    public void setReturnLocation(
            String returnWorld,
            Double returnX,
            Double returnY,
            Double returnZ,
            Float returnYaw,
            Float returnPitch
    ) {
        this.returnWorld = returnWorld;
        this.returnX = returnX;
        this.returnY = returnY;
        this.returnZ = returnZ;
        this.returnYaw = returnYaw;
        this.returnPitch = returnPitch;
    }

    public void limparReturnLocation() {
        setReturnLocation(null, null, null, null, null, null);
    }
}
package com.engenhoso.serverplugin.shared.hologram;

public class HologramLine {

    private final String text;

    private HologramLine(String text) {
        this.text = text == null ? "" : text;
    }

    public static HologramLine of(String text) {
        return new HologramLine(text);
    }

    public static HologramLine blank() {
        return new HologramLine("");
    }

    public String getText() {
        return text;
    }
}
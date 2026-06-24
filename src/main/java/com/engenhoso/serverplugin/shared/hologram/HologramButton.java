package com.engenhoso.serverplugin.shared.hologram;

public class HologramButton {

    private final String id;
    private final String label;
    private final float width;
    private final float height;
    private final boolean closeOnClick;
    private final HologramButtonAction action;

    private HologramButton(
            String id,
            String label,
            float width,
            float height,
            boolean closeOnClick,
            HologramButtonAction action
    ) {
        this.id = id;
        this.label = label;
        this.width = width;
        this.height = height;
        this.closeOnClick = closeOnClick;
        this.action = action;
    }

    public static HologramButton of(
            String id,
            String label,
            HologramButtonAction action
    ) {
        return new HologramButton(
                id,
                label,
                1.35f,
                0.42f,
                true,
                action
        );
    }

    public static HologramButton of(
            String id,
            String label,
            float width,
            float height,
            boolean closeOnClick,
            HologramButtonAction action
    ) {
        return new HologramButton(
                id,
                label,
                width,
                height,
                closeOnClick,
                action
        );
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isCloseOnClick() {
        return closeOnClick;
    }

    public HologramButtonAction getAction() {
        return action;
    }
}
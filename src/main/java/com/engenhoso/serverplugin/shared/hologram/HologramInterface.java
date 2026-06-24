package com.engenhoso.serverplugin.shared.hologram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HologramInterface {

    private final String id;
    private final String title;
    private final String subtitle;
    private final List<HologramLine> lines;
    private final List<HologramButton> buttons;

    private final double distanceFromAnchor;
    private final double verticalOffset;
    private final double lineSpacing;

    private final int lineWidth;
    private final long durationTicks;

    private HologramInterface(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.lines = Collections.unmodifiableList(new ArrayList<>(builder.lines));
        this.buttons = Collections.unmodifiableList(new ArrayList<>(builder.buttons));
        this.distanceFromAnchor = builder.distanceFromAnchor;
        this.verticalOffset = builder.verticalOffset;
        this.lineSpacing = builder.lineSpacing;
        this.lineWidth = builder.lineWidth;
        this.durationTicks = builder.durationTicks;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<HologramLine> getLines() {
        return lines;
    }

    public List<HologramButton> getButtons() {
        return buttons;
    }

    public double getDistanceFromAnchor() {
        return distanceFromAnchor;
    }

    public double getVerticalOffset() {
        return verticalOffset;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public long getDurationTicks() {
        return durationTicks;
    }

    public static class Builder {

        private final String id;

        private String title = "";
        private String subtitle = "";

        private final List<HologramLine> lines = new ArrayList<>();
        private final List<HologramButton> buttons = new ArrayList<>();

        private double distanceFromAnchor = 1.85;
        private double verticalOffset = 2.15;
        private double lineSpacing = 0.22;

        private int lineWidth = 260;
        private long durationTicks = 20L * 30L;

        private Builder(String id) {
            this.id = id;
        }

        public Builder title(String title) {
            this.title = title == null ? "" : title;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle == null ? "" : subtitle;
            return this;
        }

        public Builder line(String text) {
            this.lines.add(HologramLine.of(text));
            return this;
        }

        public Builder blankLine() {
            this.lines.add(HologramLine.blank());
            return this;
        }

        public Builder button(HologramButton button) {
            this.buttons.add(button);
            return this;
        }

        public Builder distanceFromAnchor(double distanceFromAnchor) {
            this.distanceFromAnchor = distanceFromAnchor;
            return this;
        }

        public Builder verticalOffset(double verticalOffset) {
            this.verticalOffset = verticalOffset;
            return this;
        }

        public Builder lineSpacing(double lineSpacing) {
            this.lineSpacing = lineSpacing;
            return this;
        }

        public Builder lineWidth(int lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder durationTicks(long durationTicks) {
            this.durationTicks = durationTicks;
            return this;
        }

        public HologramInterface build() {
            return new HologramInterface(this);
        }
    }
}
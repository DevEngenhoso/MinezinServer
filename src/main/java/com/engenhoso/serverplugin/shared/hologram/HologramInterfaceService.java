package com.engenhoso.serverplugin.shared.hologram;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramInterfaceService {

    private static final Color PANEL_BACKGROUND = Color.fromARGB(235, 2, 14, 26);
    private static final Color BUTTON_BACKGROUND = Color.fromARGB(235, 2, 20, 34);

    private static final int DEFAULT_PANEL_LINE_WIDTH = 360;
    private static final int DEFAULT_BUTTON_LINE_WIDTH = 220;

    private static final double MAX_RAY_DISTANCE = 10.0;

    /*
     * Desce a interface inteira.
     * Antes o painel ficava muito alto e grudava no teto/estrutura.
     */
    private static final double INTERFACE_VERTICAL_CORRECTION = -0.70;

    /*
     * Distância horizontal entre botões.
     * Antes ficavam próximos demais, aumentando chance de clique errado.
     */
    private static final double TWO_BUTTON_OFFSET = 1.75;
    private static final double MULTI_BUTTON_STEP = 3.50;

    /*
     * Redução real da hitbox.
     * 1.10 = largura clicável maior que o visual configurado.
     * 0.45 = altura clicável menor/moderada.
     */
    private static final double BUTTON_HITBOX_WIDTH_SCALE = 1.10;
    private static final double BUTTON_HITBOX_HEIGHT_SCALE = 0.45;

    /*
     * Tamanho mínimo para não ficar impossível de clicar.
     */
    private static final double BUTTON_HITBOX_MIN_WIDTH = 0.55;
    private static final double BUTTON_HITBOX_MIN_HEIGHT = 0.28;

    /*
     * Aproxima os botões da borda inferior do painel.
     *
     * Antes o cálculo usava -0.35, jogando os botões muito para baixo.
     * Quanto MAIOR este valor, mais os botões SOBEM.
     * Quanto MENOR este valor, mais os botões DESCEM.
     */
    private static final double BUTTON_DISTANCE_FROM_PANEL_BOTTOM = 1.10;

    private final JavaPlugin plugin;

    private final NamespacedKey keyOwner;
    private final NamespacedKey keyInterfaceId;
    private final NamespacedKey keyButtonRuntimeId;

    private final Map<UUID, ActiveHologramInterface> activeByPlayer = new HashMap<>();

    public HologramInterfaceService(JavaPlugin plugin) {
        this.plugin = plugin;

        this.keyOwner = new NamespacedKey(plugin, "hologram_interface_owner");
        this.keyInterfaceId = new NamespacedKey(plugin, "hologram_interface_id");
        this.keyButtonRuntimeId = new NamespacedKey(plugin, "hologram_interface_button");
    }

    public void open(Player player, Entity anchor, HologramInterface hologramInterface) {
        if (anchor == null || anchor.getWorld() == null) {
            return;
        }

        openAt(player, anchor.getLocation(), hologramInterface);
    }

    public void openAt(Player player, Location anchorLocation, HologramInterface hologramInterface) {
        if (player == null || anchorLocation == null || anchorLocation.getWorld() == null || hologramInterface == null) {
            return;
        }

        close(player);

        InterfaceGeometry geometry = calcularGeometria(anchorLocation, hologramInterface);

        ActiveHologramInterface active = new ActiveHologramInterface(
                player.getUniqueId(),
                hologramInterface.getId()
        );

        String panelText = montarTextoDoPainel(hologramInterface);

        TextDisplay panel = spawnTextDisplay(
                player,
                geometry.origin(),
                hologramInterface.getId(),
                panelText,
                Math.max(hologramInterface.getLineWidth(), DEFAULT_PANEL_LINE_WIDTH),
                PANEL_BACKGROUND
        );

        active.entityUuids.add(panel.getUniqueId());

        spawnButtonsOutsidePanel(player, hologramInterface, geometry, active, panelText);

        activeByPlayer.put(player.getUniqueId(), active);

        long durationTicks = hologramInterface.getDurationTicks();

        if (durationTicks > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ActiveHologramInterface current = activeByPlayer.get(player.getUniqueId());

                if (current == null) {
                    return;
                }

                if (!current.interfaceId.equals(hologramInterface.getId())) {
                    return;
                }

                close(player);
            }, durationTicks);
        }
    }

    public boolean handleInteraction(Player player, Entity entity) {
        if (!(entity instanceof Interaction)) {
            return false;
        }

        PersistentDataContainer data = entity.getPersistentDataContainer();

        String ownerRaw = data.get(keyOwner, PersistentDataType.STRING);
        String interfaceId = data.get(keyInterfaceId, PersistentDataType.STRING);
        String buttonRuntimeId = data.get(keyButtonRuntimeId, PersistentDataType.STRING);

        if (ownerRaw == null || interfaceId == null || buttonRuntimeId == null) {
            return false;
        }

        UUID ownerUuid;

        try {
            ownerUuid = UUID.fromString(ownerRaw);
        } catch (IllegalArgumentException exception) {
            return true;
        }

        if (!player.getUniqueId().equals(ownerUuid)) {
            player.sendActionBar(legacy("§8Esta interface não pertence a você."));
            return true;
        }

        ActiveHologramInterface active = activeByPlayer.get(player.getUniqueId());

        if (active == null) {
            return true;
        }

        if (!active.interfaceId.equals(interfaceId)) {
            return true;
        }

        HologramButton button = active.buttonsByRuntimeId.get(buttonRuntimeId);

        if (button == null) {
            return true;
        }

        executeButton(player, active, button);
        return true;
    }

    public boolean handleInteractionAt(PlayerInteractAtEntityEvent event) {
        return handleInteraction(event.getPlayer(), event.getRightClicked());
    }

    public boolean handleRayClick(Player player) {
        ActiveHologramInterface active = activeByPlayer.get(player.getUniqueId());

        if (active == null) {
            return false;
        }

        Location eye = player.getEyeLocation();
        Vector rayDirection = eye.getDirection().normalize();

        ButtonRayHit closestHit = null;

        for (ButtonHitbox hitbox : active.buttonHitboxes) {
            ButtonRayHit hit = rayIntersectsButton(eye, rayDirection, hitbox);

            if (!hit.hit()) {
                continue;
            }

            if (closestHit == null || hit.distance() < closestHit.distance()) {
                closestHit = hit;
            }
        }

        if (closestHit == null) {
            return false;
        }

        executeButton(player, active, closestHit.button());
        return true;
    }

    public boolean isHologramInterfaceEntity(Entity entity) {
        if (entity == null) {
            return false;
        }

        PersistentDataContainer data = entity.getPersistentDataContainer();

        return data.has(keyOwner, PersistentDataType.STRING)
                && data.has(keyInterfaceId, PersistentDataType.STRING);
    }

    public void close(Player player) {
        if (player == null) {
            return;
        }

        ActiveHologramInterface active = activeByPlayer.remove(player.getUniqueId());

        if (active == null) {
            return;
        }

        for (UUID uuid : active.entityUuids) {
            Entity entity = Bukkit.getEntity(uuid);

            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
    }

    public void closeAll() {
        List<UUID> players = new ArrayList<>(activeByPlayer.keySet());

        for (UUID playerUuid : players) {
            ActiveHologramInterface active = activeByPlayer.remove(playerUuid);

            if (active == null) {
                continue;
            }

            for (UUID entityUuid : active.entityUuids) {
                Entity entity = Bukkit.getEntity(entityUuid);

                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
            }
        }
    }

    private void executeButton(Player player, ActiveHologramInterface active, HologramButton button) {
        HologramInterfaceContext context = new HologramInterfaceContext(
                this,
                player,
                active.interfaceId,
                button.getId()
        );

        if (button.getAction() != null) {
            button.getAction().execute(context);
        }

        if (button.isCloseOnClick() && !context.isClosed()) {
            close(player);
        }
    }

    private void spawnButtonsOutsidePanel(
            Player player,
            HologramInterface hologramInterface,
            InterfaceGeometry geometry,
            ActiveHologramInterface active,
            String panelText
    ) {
        List<HologramButton> buttons = hologramInterface.getButtons();

        if (buttons.isEmpty()) {
            return;
        }

        double buttonY = calcularYDosBotoesForaDoPainel(panelText, hologramInterface);

        if (buttons.size() == 1) {
            spawnButton(player, hologramInterface, geometry, active, buttons.get(0), 0.0, buttonY);
            return;
        }

        if (buttons.size() == 2) {
            spawnButton(player, hologramInterface, geometry, active, buttons.get(0), -TWO_BUTTON_OFFSET, buttonY);
            spawnButton(player, hologramInterface, geometry, active, buttons.get(1), TWO_BUTTON_OFFSET, buttonY);
            return;
        }

        double startX = -((buttons.size() - 1) * (MULTI_BUTTON_STEP / 2.0));

        for (int i = 0; i < buttons.size(); i++) {
            spawnButton(
                    player,
                    hologramInterface,
                    geometry,
                    active,
                    buttons.get(i),
                    startX + (i * MULTI_BUTTON_STEP),
                    buttonY
            );
        }
    }

    private void spawnButton(
            Player player,
            HologramInterface hologramInterface,
            InterfaceGeometry geometry,
            ActiveHologramInterface active,
            HologramButton button,
            double rightOffset,
            double yOffset
    ) {
        String runtimeButtonId = UUID.randomUUID().toString();

        double visualWidth = Math.max(button.getWidth(), 2.0f);
        double visualHeight = Math.max(button.getHeight(), 0.9f);

        double hitboxWidth = Math.max(BUTTON_HITBOX_MIN_WIDTH, visualWidth * BUTTON_HITBOX_WIDTH_SCALE);
        double hitboxHeight = Math.max(BUTTON_HITBOX_MIN_HEIGHT, visualHeight * BUTTON_HITBOX_HEIGHT_SCALE);

        Location buttonCenter = geometry.origin().clone()
                .add(geometry.right().clone().multiply(rightOffset))
                .add(0.0, yOffset, 0.0);

        TextDisplay buttonText = spawnTextDisplay(
                player,
                buttonCenter,
                hologramInterface.getId(),
                button.getLabel(),
                DEFAULT_BUTTON_LINE_WIDTH,
                BUTTON_BACKGROUND
        );

        active.entityUuids.add(buttonText.getUniqueId());

        /*
         * Interaction nasce pelo pé da hitbox, não pelo centro.
         * Por isso descemos metade da altura real da hitbox.
         */
        Location interactionLocation = buttonCenter.clone().add(0.0, -(hitboxHeight / 2.0), 0.0);

        Interaction interaction = interactionLocation.getWorld().spawn(interactionLocation, Interaction.class);

        interaction.setInteractionWidth((float) hitboxWidth);
        interaction.setInteractionHeight((float) hitboxHeight);
        interaction.setResponsive(true);
        interaction.setPersistent(false);
        interaction.setInvulnerable(true);

        PersistentDataContainer data = interaction.getPersistentDataContainer();

        data.set(keyOwner, PersistentDataType.STRING, player.getUniqueId().toString());
        data.set(keyInterfaceId, PersistentDataType.STRING, hologramInterface.getId());
        data.set(keyButtonRuntimeId, PersistentDataType.STRING, runtimeButtonId);

        hideFromOtherPlayers(player, interaction);

        active.entityUuids.add(interaction.getUniqueId());
        active.buttonsByRuntimeId.put(runtimeButtonId, button);
        active.buttonHitboxes.add(new ButtonHitbox(button, buttonCenter, hitboxWidth, hitboxHeight));
    }

    private ButtonRayHit rayIntersectsButton(Location eye, Vector rayDirection, ButtonHitbox hitbox) {
        if (eye.getWorld() != hitbox.center().getWorld()) {
            return ButtonRayHit.miss();
        }

        Vector normal = hitbox.center().toVector().subtract(eye.toVector());

        if (normal.lengthSquared() < 0.0001) {
            return ButtonRayHit.miss();
        }

        normal.normalize();

        Vector up = new Vector(0.0, 1.0, 0.0);
        Vector right = up.clone().crossProduct(normal);

        if (right.lengthSquared() < 0.0001) {
            return ButtonRayHit.miss();
        }

        right.normalize();

        double denominator = rayDirection.dot(normal);

        if (Math.abs(denominator) < 0.0001) {
            return ButtonRayHit.miss();
        }

        Vector eyeToButton = hitbox.center().toVector().subtract(eye.toVector());
        double distance = eyeToButton.dot(normal) / denominator;

        if (distance < 0.0 || distance > MAX_RAY_DISTANCE) {
            return ButtonRayHit.miss();
        }

        Vector hitPoint = eye.toVector().add(rayDirection.clone().multiply(distance));
        Vector relative = hitPoint.subtract(hitbox.center().toVector());

        double localX = relative.dot(right);
        double localY = relative.dot(up);

        boolean insideX = Math.abs(localX) <= hitbox.width() / 2.0;
        boolean insideY = Math.abs(localY) <= hitbox.height() / 2.0;

        if (!insideX || !insideY) {
            return ButtonRayHit.miss();
        }

        return ButtonRayHit.hit(hitbox.button(), distance);
    }

    private TextDisplay spawnTextDisplay(
            Player player,
            Location location,
            String interfaceId,
            String text,
            int lineWidth,
            Color backgroundColor
    ) {
        World world = location.getWorld();

        if (world == null) {
            throw new IllegalStateException("Não é possível spawnar TextDisplay sem mundo.");
        }

        TextDisplay display = world.spawn(location, TextDisplay.class);

        display.text(legacy(text));
        display.setBillboard(Display.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setLineWidth(lineWidth);
        display.setShadowed(true);
        display.setSeeThrough(false);
        display.setPersistent(false);
        display.setInvulnerable(true);
        display.setBackgroundColor(backgroundColor);

        PersistentDataContainer data = display.getPersistentDataContainer();

        data.set(keyOwner, PersistentDataType.STRING, player.getUniqueId().toString());
        data.set(keyInterfaceId, PersistentDataType.STRING, interfaceId);

        hideFromOtherPlayers(player, display);

        return display;
    }

    private String montarTextoDoPainel(HologramInterface hologramInterface) {
        StringBuilder builder = new StringBuilder();

        builder.append("§b§l╔════════════════════════════════╗").append("\n");

        if (!hologramInterface.getTitle().isBlank()) {
            builder.append("§6§l").append(hologramInterface.getTitle()).append("\n");
        }

        if (!hologramInterface.getSubtitle().isBlank()) {
            builder.append("§7").append(hologramInterface.getSubtitle()).append("\n");
        }

        builder.append("§b§l╠════════════════════════════════╣").append("\n");

        for (HologramLine line : hologramInterface.getLines()) {
            if (line.getText().isBlank()) {
                builder.append(" ").append("\n");
            } else {
                builder.append(line.getText()).append("\n");
            }
        }

        builder.append("§b§l╚════════════════════════════════╝");

        return builder.toString();
    }

    private double calcularYDosBotoesForaDoPainel(String panelText, HologramInterface hologramInterface) {
        int linhas = contarLinhas(panelText);
        double alturaAproximada = linhas * hologramInterface.getLineSpacing();

        return -(alturaAproximada / 2.0) + BUTTON_DISTANCE_FROM_PANEL_BOTTOM;
    }

    private int contarLinhas(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }

        return text.split("\n", -1).length;
    }

    private InterfaceGeometry calcularGeometria(Location anchorLocation, HologramInterface hologramInterface) {
        Vector forward = anchorLocation.getDirection();
        forward.setY(0.0);

        if (forward.lengthSquared() < 0.01) {
            forward = new Vector(0.0, 0.0, 1.0);
        }

        forward.normalize();

        Vector right = new Vector(-forward.getZ(), 0.0, forward.getX());

        if (right.lengthSquared() < 0.01) {
            right = new Vector(1.0, 0.0, 0.0);
        }

        right.normalize();

        Location origin = anchorLocation.clone()
                .add(forward.clone().multiply(hologramInterface.getDistanceFromAnchor()))
                .add(0.0, hologramInterface.getVerticalOffset() + INTERFACE_VERTICAL_CORRECTION, 0.0);

        origin.setPitch(0.0f);

        return new InterfaceGeometry(origin, forward, right);
    }

    private void hideFromOtherPlayers(Player owner, Entity entity) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(owner.getUniqueId())) {
                continue;
            }

            onlinePlayer.hideEntity(plugin, entity);
        }
    }

    private Component legacy(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text == null ? "" : text);
    }

    private record InterfaceGeometry(
            Location origin,
            Vector forward,
            Vector right
    ) {
    }

    private record ButtonHitbox(
            HologramButton button,
            Location center,
            double width,
            double height
    ) {
    }

    private record ButtonRayHit(
            boolean hit,
            HologramButton button,
            double distance
    ) {
        private static ButtonRayHit hit(HologramButton button, double distance) {
            return new ButtonRayHit(true, button, distance);
        }

        private static ButtonRayHit miss() {
            return new ButtonRayHit(false, null, 0.0);
        }
    }

    private static class ActiveHologramInterface {

        private final UUID ownerUuid;
        private final String interfaceId;

        private final List<UUID> entityUuids = new ArrayList<>();
        private final Map<String, HologramButton> buttonsByRuntimeId = new HashMap<>();
        private final List<ButtonHitbox> buttonHitboxes = new ArrayList<>();

        private ActiveHologramInterface(UUID ownerUuid, String interfaceId) {
            this.ownerUuid = ownerUuid;
            this.interfaceId = interfaceId;
        }
    }
}
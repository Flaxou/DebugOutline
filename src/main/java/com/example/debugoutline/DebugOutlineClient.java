package com.example.debugoutline;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DebugOutlineClient implements ClientModInitializer {

    /** Rayon de scan : cube de (2 * RADIUS) blocs de côté centré sur le joueur. */
    private static final int RADIUS = 32;

    /** Rescan toutes les 10 ticks (0,5 s). Le résultat est mis en cache entre les scans. */
    private static final int SCAN_INTERVAL_TICKS = 10;

    private static KeyBinding toggleKey;
    private static boolean enabled = false;
    private static int tickCounter = 0;
    private static final List<BlockPos> targets = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.debugoutline.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.debugoutline"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(DebugOutlineClient::onClientTick);

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            if (enabled) {
                OutlineRenderer.render(ctx, targets);
            }
        });
    }

    private static void onClientTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("Debug outline : " + (enabled ? "ON" : "OFF"))
                                .formatted(enabled ? Formatting.GREEN : Formatting.RED),
                        true // actionbar, au-dessus de la hotbar
                );
            }
        }

        if (!enabled || client.world == null || client.player == null) {
            targets.clear();
            return;
        }

        if (tickCounter++ % SCAN_INTERVAL_TICKS == 0) {
            ChestScanner.scan(client.world, client.player.getBlockPos(), RADIUS, targets);
        }
    }
}

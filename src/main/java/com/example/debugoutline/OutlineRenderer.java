package com.example.debugoutline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Dessine les contours en orange, sans test de profondeur (visibles à travers les murs).
 *
 * En 1.21.2+, l'état GL ne se règle plus avec RenderSystem.disableDepthTest() : il passe
 * par un RenderPipeline. C'est la partie la plus sensible aux versions de l'API.
 * Si la compilation échoue ici, vérifie les noms de RenderPipelines / RenderLayer.of.
 */
public final class OutlineRenderer {

    private static final Identifier PIPELINE_ID = Identifier.of("debugoutline", "xray_lines");

    private static final RenderPipeline XRAY_LINES = RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(PIPELINE_ID)
            .withDepthTest(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();

    private static final RenderLayer XRAY_LAYER = RenderLayer.of(
            "debugoutline_xray",
            1536,
            XRAY_LINES,
            RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    private static final float R = 1.0f, G = 0.5f, B = 0.0f, A = 1.0f;

    private OutlineRenderer() {}

    public static void render(WorldRenderContext ctx, List<BlockPos> targets) {
        if (targets.isEmpty()) {
            return;
        }

        MatrixStack matrices = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();

        VertexConsumerProvider.Immediate consumers =
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer lines = consumers.getBuffer(XRAY_LAYER);

        matrices.push();
        // Les coordonnées monde doivent être relatives à la caméra
        matrices.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : targets) {
            WorldRenderer.drawBox(matrices, lines, new Box(pos), R, G, B, A);
        }

        matrices.pop();
        consumers.draw(XRAY_LAYER);
    }
}

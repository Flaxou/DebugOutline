package com.example.debugoutline;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Dessine les contours oranges des blocs ciblés.
 * Version de base : le test de profondeur est actif (le contour est caché par les murs).
 */
public final class OutlineRenderer {

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
        RenderLayer layer = RenderLayer.getLines();
        VertexConsumer lines = consumers.getBuffer(layer);

        matrices.push();
        // Coordonnées monde -> relatives à la caméra
        matrices.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (BlockPos pos : targets) {
            drawBox(lines, matrix, new Box(pos));
        }

        matrices.pop();
        consumers.draw(layer);
    }

    /** Ajoute les 12 arêtes de la boîte. */
    private static void drawBox(VertexConsumer c, Matrix4f m, Box box) {
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        float[][] v = {
                {x1, y1, z1}, {x2, y1, z1}, {x2, y1, z2}, {x1, y1, z2},
                {x1, y2, z1}, {x2, y2, z1}, {x2, y2, z2}, {x1, y2, z2}
        };
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] e : edges) {
            line(c, m, v[e[0]], v[e[1]]);
        }
    }

    private static void line(VertexConsumer c, Matrix4f m, float[] p, float[] q) {
        float dx = q[0] - p[0], dy = q[1] - p[1], dz = q[2] - p[2];
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = dx / len, ny = dy / len, nz = dz / len;

        c.vertex(m, p[0], p[1], p[2]).color(R, G, B, A).normal(nx, ny, nz);
        c.vertex(m, q[0], q[1], q[2]).color(R, G, B, A).normal(nx, ny, nz);
    }
}

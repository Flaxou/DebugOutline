package com.example.debugoutline;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.Map;

/**
 * Trouve les coffres dans un cube autour d'un centre.
 *
 * Au lieu de lire 65³ = 274 625 BlockStates, on parcourt uniquement les block entities
 * des chunks chargés qui chevauchent la zone. Les coffres (y compris piégés, qui héritent
 * de ChestBlockEntity) ont toujours une block entity, donc le résultat est identique
 * pour un coût bien plus faible.
 */
public final class ChestScanner {

    private ChestScanner() {}

    public static void scan(ClientWorld world, BlockPos center, int radius, List<BlockPos> out) {
        out.clear();

        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius - 1) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius - 1) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof ChestBlockEntity)) {
                        continue;
                    }
                    BlockPos pos = entry.getKey();
                    if (isInside(pos, center, radius)) {
                        out.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    /** Cube demi-ouvert : [-radius, +radius) sur chaque axe, soit exactement 2*radius blocs de côté. */
    private static boolean isInside(BlockPos pos, BlockPos center, int radius) {
        int dx = pos.getX() - center.getX();
        int dy = pos.getY() - center.getY();
        int dz = pos.getZ() - center.getZ();
        return dx >= -radius && dx < radius
                && dy >= -radius && dy < radius
                && dz >= -radius && dz < radius;
    }
}

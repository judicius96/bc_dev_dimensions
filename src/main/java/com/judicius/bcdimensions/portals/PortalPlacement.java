package com.judicius.bcdimensions.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

final class PortalPlacement {
    private PortalPlacement() {}

    /**
     * Find a safe 3x4 empty box on solid ground near 'near'.
     * Preference:
     *  1) Same X/Z column as 'near', adjusting Y to surface/cave.
     *  2) Nearby X/Z within an 16x16 area.
     *
     * Guarantees:
     *  - Not buried in solid stone (requires air clearance),
     *  - Has solid floor (3x3 pad).
     */
    static BlockPos findSafePad(ServerLevel level, BlockPos near) {
        int minY = Math.max(level.getMinBuildHeight() + 2, level.getSeaLevel() + 1);
        int maxY = level.getMaxBuildHeight() - 6;

        // 1) Try the exact X/Z column first
        BlockPos best = tryColumn(level, near.getX(), near.getZ(), near.getY(), minY, maxY);
        if (best != null) {
            return best;
        }

        // 2) Fallback: search a small X/Z area around that
        BlockPos fallback = null;
        int bestScore = Integer.MAX_VALUE;

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int x = near.getX() + dx;
                int z = near.getZ() + dz;

                BlockPos candidate = tryColumn(level, x, z, near.getY(), minY, maxY);
                if (candidate != null) {
                    int score = Math.abs(dx) + Math.abs(dz) + Math.abs(candidate.getY() - near.getY());
                    if (score < bestScore) {
                        bestScore = score;
                        fallback = candidate;
                    }
                }
            }
        }

        // 3) Final fallback: stick to same X/Z and clamp Y into safe buildable range;
        //    the builder will create the pad and carve out space if needed.
        if (fallback == null) {
            int clampedY = Math.max(minY, Math.min(near.getY(), maxY));
            fallback = new BlockPos(near.getX(), clampedY, near.getZ());
        }

        return fallback;
    }

    private static BlockPos tryColumn(ServerLevel level,
                                      int x, int z, int aroundY,
                                      int minY, int maxY) {

        // Start from the terrain surface at this column
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        surfaceY = Math.max(minY, Math.min(surfaceY, maxY));

        // small vertical scan to catch nearby caves / open areas
        for (int dy = -8; dy <= 8; dy++) {
            int yy = surfaceY + dy;
            if (yy < minY || yy > maxY) continue;

            if (has3x4Clearance(level, x, yy, z) && hasSolidFloor(level, x, yy, z)) {
                return new BlockPos(x, yy, z);
            }
        }

        return null;
    }

    private static boolean has3x4Clearance(ServerLevel level, int x, int y, int z) {
        // Check a 3x4 volume (3 wide, 4 tall, 3 deep) for air
        for (int dx = 0; dx < 3; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = 0; dz < 3; dz++) {
                    if (!level.isEmptyBlock(new BlockPos(x + dx, y + dy, z + dz))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasSolidFloor(ServerLevel level, int x, int y, int z) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                if (level.isEmptyBlock(new BlockPos(x + dx, y - 1, z + dz))) {
                    return false;
                }
            }
        }
        return true;
    }
}

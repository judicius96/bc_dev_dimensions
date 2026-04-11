package com.judicius.bcdimensions.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

final class PortalPlacement {
    private PortalPlacement() {}

    static BlockPos findSafePad(ServerLevel level, BlockPos near, int targetY) {
        int minY = level.getMinBuildHeight() + 5;
        int maxY = level.getMaxBuildHeight() - 6;

        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int x = near.getX() + dx;
                int z = near.getZ() + dz;

                for (int dy = -8; dy <= 8; dy++) {
                    int y = targetY + dy;
                    if (y < minY || y > maxY) continue;

                    if (hasSolidContext(level, x, y, z)) {
                        int score = Math.abs(dx) + Math.abs(dz) + Math.abs(dy);
                        if (score < bestScore) {
                            bestScore = score;
                            best = new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }

        if (best == null) {
            best = new BlockPos(near.getX(), targetY, near.getZ());
        }

        return best;
    }

    static BlockPos findSurfacePad(ServerLevel level, BlockPos near) {
        int minY = level.getMinBuildHeight() + 5;
        int maxY = level.getMaxBuildHeight() - 6;

        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int x = near.getX() + dx;
                int z = near.getZ() + dz;

                // Use heightmap to find actual surface
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                surfaceY = Math.max(minY, Math.min(surfaceY, maxY));

                if (hasSolidContext(level, x, surfaceY, z)) {
                    int score = Math.abs(dx) + Math.abs(dz);
                    if (score < bestScore) {
                        bestScore = score;
                        best = new BlockPos(x, surfaceY, z);
                    }
                }
            }
        }

        if (best == null) {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    near.getX(), near.getZ()) - 1;
            best = new BlockPos(near.getX(), surfaceY, near.getZ());
        }

        return best;
    }

    private static boolean hasSolidContext(ServerLevel level, int x, int y, int z) {
        BlockPos floor = new BlockPos(x, y - 1, z);
        if (level.isEmptyBlock(floor)) return false;
        if (level.getBlockState(floor).is(Blocks.BEDROCK)) return false;

        int solidCount = 0;
        for (int bx = 0; bx < 3; bx++) {
            for (int by = 0; by < 4; by++) {
                for (int bz = 0; bz < 3; bz++) {
                    BlockPos p = new BlockPos(x + bx, y + by, z + bz);
                    if (!level.isEmptyBlock(p) && !level.getBlockState(p).is(Blocks.BEDROCK)) {
                        solidCount++;
                    }
                }
            }
        }
        return solidCount >= 6;
    }

    static void carveArrivalSpace(ServerLevel level, BlockPos portalBase) {
        BlockPos center = portalBase.above(2);
        int radius = 4;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (!level.getBlockState(p).is(Blocks.BEDROCK)) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
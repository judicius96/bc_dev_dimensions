package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class MiningSeamFeature extends Feature<MiningSeamConfig> {

    public MiningSeamFeature(Codec<MiningSeamConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MiningSeamConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        MiningSeamConfig config = context.config();

        int y = origin.getY();
        if (y < config.yMin || y > config.yMax) return false;

        BlockState rawState = config.rawBlock.defaultBlockState();

        // Diagonal orientation — random angle in XZ, slight Y slope
        float angle = random.nextFloat() * (float) Math.PI;
        double dX = Math.sin(angle) * 12.0;
        double dY = (random.nextFloat() - 0.5) * 6.0; // diagonal Y component
        double dZ = Math.cos(angle) * 12.0;

        double startX = origin.getX() + 0.5 - dX / 2.0;
        double startY = origin.getY() + 0.5 - dY / 2.0;
        double startZ = origin.getZ() + 0.5 - dZ / 2.0;

        int steps = 12;
        int placedCount = 0;

        for (int step = 0; step < steps; step++) {
            double t = (double) step / (steps - 1); // 0.0 to 1.0
            double cx = startX + dX * t;
            double cy = startY + dY * t;
            double cz = startZ + dZ * t;

            // Ellipsoid radius — peaks at center (t=0.5), tapers to ~1 at ends
            double edgeFactor = 2.0 * Math.abs(t - 0.5); // 0 at center, 1 at ends
            double radius = 4.0 * (1.0 - edgeFactor * edgeFactor); // quadratic taper, max 4
            radius = Math.max(1.0, radius);

            int minX = (int) Math.floor(cx - radius);
            int minY = (int) Math.floor(cy - radius);
            int minZ = (int) Math.floor(cz - radius);
            int maxX = (int) Math.ceil(cx + radius);
            int maxY = (int) Math.ceil(cy + radius);
            int maxZ = (int) Math.ceil(cz + radius);

            for (int bx = minX; bx <= maxX; bx++) {
                for (int by = minY; by <= maxY; by++) {
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        double ddx = bx + 0.5 - cx;
                        double ddy = by + 0.5 - cy;
                        double ddz = bz + 0.5 - cz;
                        if (ddx * ddx + ddy * ddy + ddz * ddz > radius * radius) continue;
                        if (by < config.yMin || by > config.yMax) continue;

                        BlockPos candidate = new BlockPos(bx, by, bz);
                        BlockState existing = level.getBlockState(candidate);
                        if (existing.is(Blocks.STONE) || existing.is(Blocks.DEEPSLATE)) {
                            level.setBlock(candidate, rawState, 2);
                            placedCount++;
                        }
                    }
                }
            }
        }

        return placedCount > 0;
    }
}
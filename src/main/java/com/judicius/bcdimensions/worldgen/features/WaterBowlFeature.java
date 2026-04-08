package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WaterBowlFeature extends Feature<NoneFeatureConfiguration> {

    public WaterBowlFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Check if origin is on solid ground
        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below())) {
            return false;
        }

        // Random bowl size between 5 and 8
        int radius = 2 + random.nextInt(2); // radius 2-3 = diameter 5-7 (close enough to 5-8)

        // Create bowl
        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                double distance = Math.sqrt(x * x + z * z);

                // Natural irregular edges
                double noiseOffset = (random.nextDouble() - 0.5) * 0.8;
                double effectiveRadius = radius + noiseOffset;

                if (distance <= effectiveRadius) {
                    // Calculate depth based on distance from center
                    int depth;
                    if (distance < effectiveRadius * 0.3) {
                        depth = 3; // Center: 3 blocks deep
                    } else if (distance < effectiveRadius * 0.6) {
                        depth = 2; // Middle ring: 2 blocks deep
                    } else if (distance < effectiveRadius * 0.9) {
                        depth = 1; // Outer ring: 1 block deep
                    } else {
                        depth = 0; // Edge: mud only
                    }

                    // Dig out bowl
                    for (int y = 0; y > -depth; y--) {
                        level.setBlock(pos.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 3);
                    }

                    // Place water at bottom of depression
                    if (depth > 0) {
                        level.setBlock(pos.offset(0, -depth, 0), Blocks.WATER.defaultBlockState(), 3);
                    }

                    // Mud edges (only where depth is 0 or at water edge)
                    if (depth == 0 || (depth == 1 && distance > effectiveRadius * 0.7)) {
                        level.setBlock(pos, Blocks.MUD.defaultBlockState(), 3);
                    }

                    // Lily pads on water surface
                    if (depth > 0 && random.nextFloat() < 0.3) {
                        BlockPos lilyPos = pos.offset(0, -depth + 1, 0);
                        if (level.getBlockState(lilyPos).isAir() &&
                                level.getBlockState(lilyPos.below()).is(Blocks.WATER)) {
                            level.setBlock(lilyPos, Blocks.LILY_PAD.defaultBlockState(), 3);
                        }
                    }

                    // Firefly bushes around outer edge
                    if (depth == 0 && random.nextFloat() < 0.15) {
                        BlockPos bushPos = pos.above();
                        if (level.getBlockState(bushPos).isAir()) {
                            // Replace with your firefly bush block
                            level.setBlock(bushPos, Blocks.FLOWERING_AZALEA.defaultBlockState(), 3); // TEMP placeholder
                        }
                    }
                }
            }
        }

        return true;
    }
}
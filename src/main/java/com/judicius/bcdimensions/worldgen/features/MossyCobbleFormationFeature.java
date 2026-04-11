package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MossyCobbleFormationFeature extends Feature<NoneFeatureConfiguration> {

    public MossyCobbleFormationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Scan downward for moss block floor
        BlockPos center = null;
        for (int i = 0; i < 16; i++) {
            BlockPos check = origin.below(i);
            if (level.getBlockState(check).is(Blocks.MOSS_BLOCK)) {
                // Place center at floor level so formation is half buried
                center = check;
                break;
            }
        }
        if (center == null) return false;

        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();

        // Base radius 3-4
        int baseRadius = 3 + random.nextInt(2);

        // Per-axis warp factors — randomize each axis independently
        // Values between 0.6 and 1.4 give ellipsoid variation
        double scaleX = 0.6 + random.nextDouble() * 0.8;
        double scaleY = 0.6 + random.nextDouble() * 0.8;
        double scaleZ = 0.6 + random.nextDouble() * 0.8;

        // Additional per-block noise for surface irregularity
        double noiseStrength = 0.3 + random.nextDouble() * 0.4;

        int placed = 0;

        for (int dx = -baseRadius - 1; dx <= baseRadius + 1; dx++) {
            for (int dy = -(baseRadius + 1); dy <= baseRadius + 1; dy++) {
                for (int dz = -baseRadius - 1; dz <= baseRadius + 1; dz++) {
                    // Ellipsoid check with per-axis scaling
                    double nx = dx / (scaleX * baseRadius);
                    double ny = dy / (scaleY * baseRadius);
                    double nz = dz / (scaleZ * baseRadius);

                    // Add surface noise — use a hash of position for determinism
                    int hash = (dx * 1619 + dy * 31337 + dz * 6971) & Integer.MAX_VALUE;
                    double noise = ((hash % 1000) / 1000.0 - 0.5) * noiseStrength;

                    double dist = Math.sqrt(nx * nx + ny * ny + nz * nz) + noise;

                    if (dist > 1.0) continue;

                    BlockPos placePos = center.offset(dx, dy, dz);

                    // Only replace solid blocks or air — don't replace water
                    BlockState existing = level.getBlockState(placePos);
                    if (existing.is(Blocks.WATER)) continue;

                    level.setBlock(placePos, mossy, 3);
                    placed++;
                }
            }
        }

        return placed > 0;
    }
}
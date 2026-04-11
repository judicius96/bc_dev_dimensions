package com.judicius.bcdimensions.worldgen.features;

import com.judicius.bcdimensions.registry.BCRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

public class WaterBowlFeature extends Feature<NoneFeatureConfiguration> {

    public WaterBowlFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!level.getBlockState(origin.below()).isSolidRender(level, origin.below())) {
            return false;
        }

        Block fireflyBushBlock = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation("minecraft", "firefly_bush"));
        boolean hasFireflyBush = fireflyBushBlock != null && fireflyBushBlock != Blocks.AIR;

        BlockState radiantLichenState = BCRegistry.RADIANT_LICHEN.get().defaultBlockState();

        int radius = 5 + random.nextInt(3);
        int maxDepth = 3;
        int surfaceY = origin.getY();

        int size = (radius + 3) * 2 + 1;
        double[][] noiseMap = new double[size][size];
        for (int xi = 0; xi < size; xi++) {
            for (int zi = 0; zi < size; zi++) {
                noiseMap[xi][zi] = (random.nextDouble() - 0.5) * 1.5;
            }
        }

        int cx = origin.getX();
        int cz = origin.getZ();

        // Pass 1 — mud shell (radius+1 hemisphere)
        for (int x = -(radius + 1); x <= (radius + 1); x++) {
            for (int z = -(radius + 1); z <= (radius + 1); z++) {
                int xi = x + radius + 2;
                int zi = z + radius + 2;
                double noise = noiseMap[xi][zi];
                double effectiveRadius = radius + noise;

                for (int y = 0; y >= -(maxDepth + 1); y--) {
                    double nx = x / (effectiveRadius + 1);
                    double nz = z / (effectiveRadius + 1);
                    double ny = y / (double)(maxDepth + 1);
                    double dist = Math.sqrt(nx * nx + nz * nz + ny * ny);

                    if (dist <= 1.0) {
                        BlockPos pos = new BlockPos(cx + x, surfaceY + y, cz + z);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.MUD.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Pass 2 — carve air interior (radius hemisphere) + clear 3 above
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int xi = x + radius + 2;
                int zi = z + radius + 2;
                double noise = noiseMap[xi][zi];
                double effectiveRadius = radius + noise;

                for (int y = 0; y >= -maxDepth; y--) {
                    double nx = x / effectiveRadius;
                    double nz = z / effectiveRadius;
                    double ny = y / (double) maxDepth;
                    double dist = Math.sqrt(nx * nx + nz * nz + ny * ny);

                    if (dist <= 1.0) {
                        BlockPos pos = new BlockPos(cx + x, surfaceY + y, cz + z);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                        // Clear 3 blocks above surface opening
                        if (y == 0) {
                            for (int ay = 1; ay <= 3; ay++) {
                                BlockPos above = new BlockPos(cx + x, surfaceY + ay, cz + z);
                                if (!level.getBlockState(above).isAir()) {
                                    level.setBlock(above, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pass 3 — fill air below surfaceY with water
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int xi = x + radius + 2;
                int zi = z + radius + 2;
                double noise = noiseMap[xi][zi];
                double effectiveRadius = radius + noise;

                for (int y = -1; y >= -maxDepth; y--) {
                    double nx = x / effectiveRadius;
                    double nz = z / effectiveRadius;
                    double ny = y / (double) maxDepth;
                    double dist = Math.sqrt(nx * nx + nz * nz + ny * ny);

                    if (dist <= 1.0) {
                        BlockPos pos = new BlockPos(cx + x, surfaceY + y, cz + z);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Pass 4 — decorations
        for (int x = -(radius + 1); x <= (radius + 1); x++) {
            for (int z = -(radius + 1); z <= (radius + 1); z++) {
                int xi = x + radius + 2;
                int zi = z + radius + 2;
                double noise = noiseMap[xi][zi];
                double effectiveRadius = radius + noise;

                double nx = x / effectiveRadius;
                double nz = z / effectiveRadius;
                double dist = Math.sqrt(nx * nx + nz * nz);

                if (dist > 1.2) continue;

                BlockPos surfacePos = new BlockPos(cx + x, surfaceY, cz + z);
                BlockPos belowSurface = surfacePos.below();

                // Lily pads on water surface
                if (level.getBlockState(surfacePos).isAir() &&
                        level.getBlockState(belowSurface).is(Blocks.WATER) &&
                        random.nextFloat() < 0.25f) {
                    level.setBlock(surfacePos, Blocks.LILY_PAD.defaultBlockState(), 3);
                }

                // Firefly bushes on mud at surface level
                if (level.getBlockState(surfacePos).is(Blocks.MUD) &&
                        hasFireflyBush && random.nextFloat() < 0.45f) {
                    BlockPos bushPos = surfacePos.above();
                    if (level.getBlockState(bushPos).isAir()) {
                        level.setBlock(bushPos, fireflyBushBlock.defaultBlockState(), 3);
                    }
                }

                // Radiant lichen on mud walls below surface
                if (random.nextFloat() < 0.15f) {
                    for (int y = -1; y >= -maxDepth; y--) {
                        BlockPos wallPos = new BlockPos(cx + x, surfaceY + y, cz + z);
                        if (level.getBlockState(wallPos).is(Blocks.MUD)) {
                            level.setBlock(wallPos, radiantLichenState, 3);
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }
}
package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class MiningVeinFeature extends Feature<MiningVeinConfig> {

    public MiningVeinFeature(Codec<MiningVeinConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MiningVeinConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        MiningVeinConfig config = context.config();

        // Clamp origin Y to config range
        int y = origin.getY();
        if (y < config.yMin || y > config.yMax) return false;

        BlockState oreState = config.oreBlock.defaultBlockState();
        BlockState rawState = config.rawBlock.defaultBlockState();

        // Vanilla-style capsule vein — random angle, series of overlapping spheres
        float angle = random.nextFloat() * (float) Math.PI;
        // Random orientation weight — more horizontal than vertical
        boolean vertical = random.nextFloat() < 0.3f;

        double dX = vertical ? 0 : Math.sin(angle) * 6.0;
        double dY = vertical ? 6.0 : (random.nextFloat() - 0.5) * 2.0;
        double dZ = vertical ? 0 : Math.cos(angle) * 6.0;

        double startX = origin.getX() + 0.5 - dX / 2.0;
        double startY = origin.getY() + 0.5 - dY / 2.0;
        double startZ = origin.getZ() + 0.5 - dZ / 2.0;

        int steps = 4 + random.nextInt(4); // 4-7 steps along axis
        int placedCount = 0;
        int totalCount = 0;

        // Collect all positions first so we can scatter raw blocks
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();

        for (int step = 0; step < steps; step++) {
            double t = (double) step / steps;
            double cx = startX + dX * t;
            double cy = startY + dY * t;
            double cz = startZ + dZ * t;

            // Sphere radius varies per step — wider in middle, narrower at ends
            double radius = 1.5 + random.nextDouble() * 1.5;
            double edgeFactor = 2.0 * Math.abs(t - 0.5); // 0 at center, 1 at ends
            radius *= (1.0 - edgeFactor * 0.5);

            int minX = (int) Math.floor(cx - radius);
            int minY = (int) Math.floor(cy - radius);
            int minZ = (int) Math.floor(cz - radius);
            int maxX = (int) Math.ceil(cx + radius);
            int maxY = (int) Math.ceil(cy + radius);
            int maxZ = (int) Math.ceil(cz + radius);

            for (int bx = minX; bx <= maxX; bx++) {
                for (int by = minY; by <= maxY; by++) {
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        double dx = bx + 0.5 - cx;
                        double dy = by + 0.5 - cy;
                        double dz = bz + 0.5 - cz;
                        if (dx * dx + dy * dy + dz * dz > radius * radius) continue;
                        if (by < config.yMin || by > config.yMax) continue;

                        BlockPos candidate = new BlockPos(bx, by, bz);
                        BlockState existing = level.getBlockState(candidate);
                        if (existing.is(Blocks.STONE) || existing.is(Blocks.DEEPSLATE)) {
                            positions.add(candidate);
                        }
                    }
                }
            }
        }

        // Determine how many raw block substitutions — 1 to 3
        int rawCount = 1 + random.nextInt(3);
        java.util.Set<Integer> rawIndices = new java.util.HashSet<>();
        if (!positions.isEmpty()) {
            for (int i = 0; i < rawCount && i < positions.size(); i++) {
                rawIndices.add(random.nextInt(positions.size()));
            }
        }

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState toPlace = rawIndices.contains(i) ? rawState : oreState;
            level.setBlock(pos, toPlace, 2);
            placedCount++;
        }

        return placedCount > 0;
    }
}
package com.judicius.bcdimensions.worldgen.features;

import com.judicius.bcdimensions.registry.BCRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CaveMushroomFeature extends Feature<NoneFeatureConfiguration> {

    public CaveMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        BlockPos base = null;
        for (int i = 0; i < 12; i++) {
            BlockPos check = pos.below(i);
            if (level.getBlockState(check).is(BCRegistry.CAVE_MYCELIUM.get())) {
                base = check;
                break;
            }
        }
        if (base == null) return false;

        boolean large = random.nextBoolean();
        int bottomRadius = large ? 3 : 2; // 7x7 or 5x5
        int upperRadius  = large ? 2 : 1; // 5x5 or 3x3
        int stemHeight   = 2 + random.nextInt(2); // 2 or 3

        // Clearance: stem + 4 cap layers
        for (int y = 1; y <= stemHeight + 4; y++) {
            if (!level.getBlockState(base.above(y)).isAir()) return false;
        }

        boolean isBrown = random.nextBoolean();
        BlockState capBlock  = isBrown
                ? Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.RED_MUSHROOM_BLOCK.defaultBlockState();
        BlockState stemBlock = Blocks.MUSHROOM_STEM.defaultBlockState();

        // Stem
        for (int y = 1; y <= stemHeight; y++) {
            level.setBlock(base.above(y), stemBlock, 3);
        }

        BlockPos stemTop = base.above(stemHeight);

        // Layer 1 (bottom cap) — bottomRadius square with corners removed, stem in center
        placeSquare(level, stemTop.above(1), capBlock, bottomRadius, true);
        level.setBlock(stemTop.above(1), stemBlock, 3);

        // Layer 2 — upperRadius full square, stem in center
        placeSquare(level, stemTop.above(2), capBlock, upperRadius, false);
        level.setBlock(stemTop.above(2), stemBlock, 3);

        // Layer 3 — upperRadius full square, stem in center
        placeSquare(level, stemTop.above(3), capBlock, upperRadius, false);
        level.setBlock(stemTop.above(3), stemBlock, 3);

        // Layer 4 (top) — upperRadius full square, no stem
        placeSquare(level, stemTop.above(4), capBlock, upperRadius, false);

        return true;
    }

    private void placeSquare(WorldGenLevel level, BlockPos center, BlockState block, int radius, boolean removeCorners) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (removeCorners && Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                pos.set(center).move(dx, 0, dz);
                level.setBlock(pos, block, 3);
            }
        }
    }
}
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class GlowShroomFeature extends Feature<NoneFeatureConfiguration> {

    public GlowShroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ModList.get().isLoaded("quark")) return false;

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

        Block capBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("quark", "glow_shroom_block"));
        Block stemBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("quark", "glow_shroom_stem"));
        if (capBlock == null || capBlock == Blocks.AIR) return false;
        if (stemBlock == null || stemBlock == Blocks.AIR) return false;

        BlockState stemState = stemBlock.defaultBlockState();
        BlockState capState = capBlock.defaultBlockState();

        // Scan upward for available space
        int availableHeight = 0;
        for (int y = 1; y < 50; y++) {
            if (level.getBlockState(base.above(y)).isAir()) {
                availableHeight = y;
            } else {
                break;
            }
        }
        if (availableHeight < 8) return false;

        // Use 75-90% of available height for stem
        int height = (int)(availableHeight * (0.75f + random.nextFloat() * 0.15f));

        // Check full clearance for stem + conical cap (6 blocks tall)
        for (int y = 1; y <= height + 6; y++) {
            if (!level.getBlockState(base.above(y)).isAir()) return false;
        }

        // Thin 1x1 stem
        for (int y = 1; y <= height; y++) {
            level.setBlock(base.above(y), stemState, 3);
        }

        // Elongated conical cap — wide at base, narrow at tip
        // 6 layers getting smaller: 2,2,2,1,1,tip
        BlockPos top = base.above(height);
        placeCap(level, top, capState, 0, 2);
        placeCap(level, top, capState, 1, 2);
        placeCap(level, top, capState, 2, 2);
        placeCap(level, top, capState, 3, 1);
        placeCap(level, top, capState, 4, 1);
        level.setBlock(top.above(5), capState, 3);

        // Split stem — 25% chance
        if (random.nextFloat() < 0.25f) {
            int splitY = height - 4 - random.nextInt(4);
            if (splitY < 2) splitY = 2;
            BlockPos splitBase = base.above(splitY);

            int dx = random.nextBoolean() ? 1 : -1;
            int dz = random.nextBoolean() ? 1 : -1;

            BlockPos branchStart = splitBase.offset(dx, 1, dz);
            int branchHeight = 3 + random.nextInt(5);

            for (int y = 0; y < branchHeight; y++) {
                BlockPos branchPos = branchStart.above(y);
                if (level.getBlockState(branchPos).isAir()) {
                    level.setBlock(branchPos, stemState, 3);
                }
            }

            // Smaller elongated cap on branch
            BlockPos branchTop = branchStart.above(branchHeight);
            placeCap(level, branchTop, capState, 0, 1);
            placeCap(level, branchTop, capState, 1, 1);
            placeCap(level, branchTop, capState, 2, 1);
            if (level.getBlockState(branchTop.above(3)).isAir()) {
                level.setBlock(branchTop.above(3), capState, 3);
            }
        }

        return true;
    }

    private void placeCap(WorldGenLevel level, BlockPos top, BlockState block, int yOffset, int radius) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    mPos.set(top).move(dx, yOffset, dz);
                    if (level.getBlockState(mPos).isAir()) {
                        level.setBlock(mPos, block, 3);
                    }
                }
            }
        }
    }
}
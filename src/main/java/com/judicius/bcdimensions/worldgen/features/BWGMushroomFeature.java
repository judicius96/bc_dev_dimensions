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

public class BWGMushroomFeature extends Feature<NoneFeatureConfiguration> {

    public BWGMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ModList.get().isLoaded("biomeswevegone")) return false;
        if (!ModList.get().isLoaded("regions_unexplored")) return false;

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

        int type = random.nextInt(3);
        Block capBlock;
        Block stemBlock;
        Block glowBlock;

        switch (type) {
            case 0 -> {
                capBlock = getBlock("biomeswevegone", "green_mushroom_block");
                stemBlock = getBlock("biomeswevegone", "white_mushroom_stem");
                glowBlock = getBlock("regions_unexplored", "glowing_green_bioshroom_block");
            }
            case 1 -> {
                capBlock = getBlock("biomeswevegone", "weeping_milkcap_mushroom_block");
                stemBlock = getBlock("biomeswevegone", "white_mushroom_stem");
                glowBlock = getBlock("regions_unexplored", "glowing_pink_bioshroom_block");
            }
            default -> {
                capBlock = getBlock("biomeswevegone", "wood_blewit_mushroom_block");
                stemBlock = getBlock("biomeswevegone", "brown_mushroom_stem");
                glowBlock = getBlock("regions_unexplored", "glowing_yellow_bioshroom_block");
            }
        }

        if (capBlock == null || stemBlock == null || glowBlock == null) return false;

        // Stem 10-16 blocks, 2x2
        int height = 10 + random.nextInt(7);

        // Bottom disk radius 8-10 from stem center
        int bottomRadius = 8 + random.nextInt(3);
        int middleRadius = bottomRadius - 2;
        int topRadius = bottomRadius - 4;

        // Check clearance
        for (int y = 1; y <= height + 5; y++) {
            if (!level.getBlockState(base.above(y)).isAir()) return false;
        }

        BlockState capState = capBlock.defaultBlockState();
        BlockState stemState = stemBlock.defaultBlockState();
        BlockState glowState = glowBlock.defaultBlockState();

        // 2x2 stem with subtle curve at mid height
        int curveAt = height / 2;
        int ox = random.nextBoolean() ? 1 : 0;
        int oz = ox == 0 ? (random.nextBoolean() ? 1 : 0) : 0;

        for (int y = 1; y <= height; y++) {
            int cx = (y >= curveAt) ? ox : 0;
            int cz = (y >= curveAt) ? oz : 0;
            level.setBlock(base.above(y).offset(cx,     0, cz),     stemState, 3);
            level.setBlock(base.above(y).offset(cx + 1, 0, cz),     stemState, 3);
            level.setBlock(base.above(y).offset(cx,     0, cz + 1), stemState, 3);
            level.setBlock(base.above(y).offset(cx + 1, 0, cz + 1), stemState, 3);
        }

        // Cap offset follows curve
        BlockPos stemTop = base.above(height).offset(ox, 0, oz);

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        // Bottom disk — at stem top level
        // Cap blocks throughout, EXCEPT where distance matches middle disk circumference
        // (between middleRadius-1 and middleRadius) → replace with glow
        for (int dx = -bottomRadius; dx <= bottomRadius; dx++) {
            for (int dz = -bottomRadius; dz <= bottomRadius; dz++) {
                int dist2 = dx * dx + dz * dz;
                if (dist2 <= bottomRadius * bottomRadius) {
                    mPos.set(stemTop).move(dx, 0, dz);
                    if (level.getBlockState(mPos).isAir()) {
                        // Glow ring: exactly at middle disk circumference
                        boolean isGlowRing = dist2 > (middleRadius - 1) * (middleRadius - 1)
                                && dist2 <= middleRadius * middleRadius;
                        level.setBlock(mPos, isGlowRing ? glowState : capState, 3);
                    }
                }
            }
        }

        // Rim — one block below bottom disk, outer circumference only
        for (int dx = -bottomRadius; dx <= bottomRadius; dx++) {
            for (int dz = -bottomRadius; dz <= bottomRadius; dz++) {
                int dist2 = dx * dx + dz * dz;
                if (dist2 > (bottomRadius - 1) * (bottomRadius - 1) && dist2 <= bottomRadius * bottomRadius) {
                    mPos.set(stemTop).move(dx, -1, dz);
                    if (level.getBlockState(mPos).isAir()) {
                        level.setBlock(mPos, capState, 3);
                    }
                }
            }
        }

        // Middle disk — one block above bottom disk
        fillCircle(level, mPos, stemTop, capState, 1, middleRadius);

        // Top disk — two blocks above bottom disk
        fillCircle(level, mPos, stemTop, capState, 2, topRadius);

        return true;
    }

    private void fillCircle(WorldGenLevel level, BlockPos.MutableBlockPos mPos,
                            BlockPos center, BlockState block, int yOffset, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    mPos.set(center).move(dx, yOffset, dz);
                    if (level.getBlockState(mPos).isAir()) {
                        level.setBlock(mPos, block, 3);
                    }
                }
            }
        }
    }

    private Block getBlock(String modid, String name) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, name));
        return (block == null || block == Blocks.AIR) ? null : block;
    }
}
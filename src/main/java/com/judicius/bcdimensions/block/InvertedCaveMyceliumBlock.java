package com.judicius.bcdimensions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class InvertedCaveMyceliumBlock extends Block {

    public InvertedCaveMyceliumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only tick in the mining dimension
        if (!level.dimension().location().toString().equals("bc_dimensions:mining_dimension")) {
            return;
        }

        // Check light in the space below this ceiling block
        int lightLevel = level.getBrightness(LightLayer.BLOCK, pos.below());

        // Revert to dirt if too much light
        if (lightLevel > 8) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return;
        }

        // Spread to adjacent dirt blocks that are also on a ceiling
        // (i.e. have solid stone above them)
        for (int i = 0; i < 4; i++) {
            BlockPos spreadPos = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(2) - random.nextInt(2),
                    random.nextInt(3) - 1
            );
            BlockState spreadState = level.getBlockState(spreadPos);
            if (spreadState.is(Blocks.DIRT)) {
                // Target must have solid block above it (ceiling context)
                BlockState above = level.getBlockState(spreadPos.above());
                if (above.isSolidRender(level, spreadPos.above())) {
                    // Target must have air below it — preserve hanging features
                    BlockState below = level.getBlockState(spreadPos.below());
                    if (below.isAir()) {
                        // And must be dark below
                        int spreadLight = level.getBrightness(LightLayer.BLOCK, spreadPos.below());
                        if (spreadLight <= 8) {
                            level.setBlockAndUpdate(spreadPos, this.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
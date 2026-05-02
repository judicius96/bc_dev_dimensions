package com.judicius.bcdimensions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

public class PaletteFloorBlock extends Block {

    public PaletteFloorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level,
                                   BlockPos pos, Direction facing, IPlantable plantable) {
        return facing == Direction.UP;
    }
}
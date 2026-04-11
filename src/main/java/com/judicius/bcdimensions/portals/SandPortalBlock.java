package com.judicius.bcdimensions.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandPortalBlock extends NetherPortalBlock {

    public SandPortalBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        PortalUtils.spawnPortalParticles(level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer sp)) return;
        if (!PortalUtils.checkAndSetPortalCooldown(sp, level, 100)) return;
        if (!PortalUtils.canUseSandPortal(level)) return;

        ResourceKey<Level> destination = level.dimension() == DimKeys.SAND
                ? Level.OVERWORLD
                : DimKeys.SAND;

        ServerLevel destLevel = sp.server.getLevel(destination);
        if (destLevel == null) return;

        // Save overworld portal position before travelling to sand dimension
        if (level.dimension() == Level.OVERWORLD) {
            BCSandTeleporter.saveOverworldPortal(sp.getUUID(), pos);
        }

        entity.changeDimension(destLevel, new BCSandTeleporter(destLevel));
    }
}
package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class BCMiningTeleporter implements ITeleporter {

    // Store overworld portal position per player when they enter the mining dimension
    private static final Map<UUID, BlockPos> overworldPortalPos = new HashMap<>();

    private final ServerLevel level;

    public BCMiningTeleporter(ServerLevel level) {
        this.level = level;
    }

    public static void saveOverworldPortal(UUID playerUUID, BlockPos pos) {
        overworldPortalPos.put(playerUUID, pos.immutable());
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destLevel,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {

        BlockPos pos = new BlockPos((int) entity.getX(), (int) entity.getY(), (int) entity.getZ());

        return getOrMakePortal(entity, destLevel, pos).map(result -> {
            BlockState blockstate = destLevel.getBlockState(result.minCorner);
            Direction.Axis axis = blockstate.hasProperty(BlockStateProperties.AXIS)
                    ? blockstate.getValue(BlockStateProperties.AXIS)
                    : Direction.Axis.X;
            return PortalShape.createPortalInfo(destLevel, result, axis,
                    new Vec3(0.5, 0.0, 0.0), entity,
                    entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
        }).orElse(null);
    }

    private Optional<BlockUtil.FoundRectangle> getOrMakePortal(Entity entity, ServerLevel destLevel, BlockPos pos) {
        boolean goingToMining = destLevel.dimension() == DimKeys.MINING;

        if (!goingToMining) {
            // Returning to overworld — use saved portal position if available
            BlockPos savedPos = overworldPortalPos.remove(entity.getUUID());
            if (savedPos != null) {
                // Search for existing portal near saved position
                Optional<BlockUtil.FoundRectangle> existing = getExistingPortal(destLevel, savedPos, 32);
                if (existing.isPresent()) return existing;
                // Portal was removed — build new one at saved location
                return makePortalAt(destLevel, savedPos, Direction.Axis.X);
            }
        }

        Optional<BlockUtil.FoundRectangle> existing = getExistingPortal(destLevel, pos, 128);
        if (existing.isPresent()) return existing;

        Direction.Axis axis = Direction.Axis.X;
        return makePortal(destLevel, pos, axis, goingToMining);
    }

    private Optional<BlockUtil.FoundRectangle> getExistingPortal(ServerLevel destLevel, BlockPos pos, int radius) {
        PoiManager poiManager = destLevel.getPoiManager();
        poiManager.ensureLoadedAndValid(destLevel, pos, radius);
        Optional<BlockPos> optional = poiManager.findClosest(
                poiType -> poiType.is(BCPoiTypes.MINING_PORTAL_POI.getKey()),
                pos, radius, PoiManager.Occupancy.ANY);
        return optional.map(blockpos -> {
            destLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockpos), 3, blockpos);
            BlockState blockstate = destLevel.getBlockState(blockpos);
            Direction.Axis axis = blockstate.hasProperty(BlockStateProperties.AXIS)
                    ? blockstate.getValue(BlockStateProperties.AXIS)
                    : Direction.Axis.X;
            return BlockUtil.getLargestRectangleAround(blockpos,
                    axis, 21, Direction.Axis.Y, 21,
                    p -> destLevel.getBlockState(p) == blockstate);
        });
    }

    private Optional<BlockUtil.FoundRectangle> makePortal(ServerLevel destLevel, BlockPos pos,
                                                          Direction.Axis axis, boolean goingToMining) {
        int minY, maxY, clampedY;
        if (goingToMining) {
            // Find first non-bedrock block from bottom
            int y = destLevel.getMinBuildHeight();
            while (y < destLevel.getMaxBuildHeight() &&
                    destLevel.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).is(Blocks.BEDROCK)) {
                y++;
            }
            clampedY = y;
            PortalPlacement.carveArrivalSpace(destLevel, new BlockPos(pos.getX(), clampedY, pos.getZ()));
        } else {
            minY = destLevel.getMinBuildHeight() + 5;
            maxY = destLevel.getMaxBuildHeight() - 10;
            clampedY = Mth.clamp(
                    destLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()),
                    minY, maxY);
        }

        BlockPos base = new BlockPos(pos.getX(), clampedY, pos.getZ());
        PortalUtils.buildMiningReturnPortal(destLevel, base);

        return Optional.of(BlockUtil.getLargestRectangleAround(base.above(),
                axis, 21, Direction.Axis.Y, 21,
                p -> destLevel.getBlockState(p).is(BCRegistry.GRANITE_PORTAL.get())));
    }

    private Optional<BlockUtil.FoundRectangle> makePortalAt(ServerLevel destLevel, BlockPos base,
                                                            Direction.Axis axis) {
        PortalUtils.buildMiningReturnPortal(destLevel, base);
        return Optional.of(BlockUtil.getLargestRectangleAround(base.above(),
                axis, 21, Direction.Axis.Y, 21,
                p -> destLevel.getBlockState(p).is(BCRegistry.GRANITE_PORTAL.get())));
    }

    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel source, ServerLevel dest) {
        return false;
    }
}
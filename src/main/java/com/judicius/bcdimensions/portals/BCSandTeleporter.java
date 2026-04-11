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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

public class BCSandTeleporter implements ITeleporter {

    // Store overworld portal position per player when they enter the sand dimension
    private static final Map<UUID, BlockPos> overworldPortalPos = new HashMap<>();

    private final ServerLevel level;

    public BCSandTeleporter(ServerLevel level) {
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
        boolean goingToSand = destLevel.dimension() == DimKeys.SAND;

        if (!goingToSand) {
            // Returning to overworld — use saved portal position if available
            BlockPos savedPos = overworldPortalPos.remove(entity.getUUID());
            if (savedPos != null) {
                Optional<BlockUtil.FoundRectangle> existing = getExistingPortal(destLevel, savedPos, 32);
                if (existing.isPresent()) return existing;
                return makePortalAt(destLevel, savedPos, Direction.Axis.X);
            }
        }

        Optional<BlockUtil.FoundRectangle> existing = getExistingPortal(destLevel, pos, 128);
        if (existing.isPresent()) return existing;

        return makePortal(destLevel, pos, Direction.Axis.X);
    }

    private Optional<BlockUtil.FoundRectangle> getExistingPortal(ServerLevel destLevel, BlockPos pos, int radius) {
        PoiManager poiManager = destLevel.getPoiManager();
        poiManager.ensureLoadedAndValid(destLevel, pos, radius);
        Optional<BlockPos> optional = poiManager.findClosest(
                poiType -> poiType.is(BCPoiTypes.SAND_PORTAL_POI.getKey()),
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

    private Optional<BlockUtil.FoundRectangle> makePortal(ServerLevel destLevel, BlockPos pos, Direction.Axis axis) {
        // Force chunk load so we get real terrain data
        destLevel.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

        int minY = destLevel.getMinBuildHeight() + 5;
        int maxY = destLevel.getMaxBuildHeight() - 10;

        // Scan from top down to find first solid surface
        int y = maxY;
        while (y > minY && destLevel.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).isAir()) {
            y--;
        }
        int clampedY = Mth.clamp(y, minY, maxY);

        BlockPos base = new BlockPos(pos.getX(), clampedY, pos.getZ());
        PortalUtils.buildSandReturnPortal(destLevel, base);

        return Optional.of(BlockUtil.getLargestRectangleAround(base.above(),
                axis, 21, Direction.Axis.Y, 21,
                p -> destLevel.getBlockState(p).is(BCRegistry.SAND_PORTAL.get())));
    }

    private Optional<BlockUtil.FoundRectangle> makePortalAt(ServerLevel destLevel, BlockPos base,
                                                            Direction.Axis axis) {
        PortalUtils.buildSandReturnPortal(destLevel, base);
        return Optional.of(BlockUtil.getLargestRectangleAround(base.above(),
                axis, 21, Direction.Axis.Y, 21,
                p -> destLevel.getBlockState(p).is(BCRegistry.SAND_PORTAL.get())));
    }

    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel source, ServerLevel dest) {
        return false;
    }
}
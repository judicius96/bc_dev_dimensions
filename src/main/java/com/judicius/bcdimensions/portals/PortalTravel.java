package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class PortalTravel {

    private PortalTravel() {}

    public static void travelSand(ServerPlayer sp, BlockPos portalPos, Axis axis) {
        travelMirror(
                sp,
                portalPos,
                axis,
                DimKeys.SAND,
                BCRegistry.SAND_PORTAL.get(),
                PortalUtils::canUseSandPortal,
                PortalUtils::buildSandReturnPortal
        );
    }

    public static void travelMining(ServerPlayer sp, BlockPos portalPos, Axis axis) {
        travelMirror(
                sp,
                portalPos,
                axis,
                DimKeys.MINING,
                BCRegistry.GRANITE_PORTAL.get(),
                PortalUtils::canUseMiningPortal,
                PortalUtils::buildMiningReturnPortal
        );
    }

    private static void travelMirror(ServerPlayer sp,
                                     BlockPos portalPos,
                                     Axis axis,
                                     ResourceKey<Level> mirrorDim,
                                     Block portalBlock,
                                     Predicate<Level> canUsePredicate,
                                     BiConsumer<ServerLevel, BlockPos> buildReturnPortal) {

        Level level = sp.level();

        if (!canUsePredicate.test(level)) {
            return;
        }

        if (!PortalUtils.checkAndSetPortalCooldown(sp, level, 100)) {
            return;
        }

        ResourceKey<Level> toKey = (level.dimension() == mirrorDim)
                ? Level.OVERWORLD
                : mirrorDim;

        ServerLevel target = sp.server.getLevel(toKey);
        if (target == null) {
            return;
        }

        // Search from the player's current portal position in the target dimension
        // using the same X/Z but scanning a 128 block radius — matches vanilla behaviour
        BlockPos searchOrigin = new BlockPos(
                portalPos.getX(),
                portalPos.getY(),
                portalPos.getZ()
        );

        BlockPos existing = PortalUtils.findExistingPortal(
                target,
                searchOrigin,
                128,
                portalBlock,
                null
        );

        if (existing != null) {
            // Scan downward from portal to find solid floor
            BlockPos floorPos = existing;
            while (floorPos.getY() > target.getMinBuildHeight() &&
                    target.isEmptyBlock(floorPos.below())) {
                floorPos = floorPos.below();
            }

            double x = existing.getX() + 0.5;
            double y = floorPos.getY() + 0.1;
            double z = existing.getZ() + 0.5;

            if (axis == Axis.Z) x += 1.01;
            else if (axis == Axis.X) z += 1.01;

            sp.teleportTo(target, x, y, z, sp.getYRot(), sp.getXRot());
            return;
        }

        // No portal found within 128 blocks — build a fresh one
        BlockPos base = PortalPlacement.findSafePad(target, searchOrigin);
        buildReturnPortal.accept(target, base);

        sp.teleportTo(
                target,
                base.getX() + 2.5,
                base.getY() + 1,
                base.getZ() + 2.5,
                sp.getYRot(),
                sp.getXRot()
        );
    }
}

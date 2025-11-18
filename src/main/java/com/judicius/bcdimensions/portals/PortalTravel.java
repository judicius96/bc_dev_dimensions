package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.BCRegistry;
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

    private PortalTravel() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // Public entry points for each portal type
    // -------------------------------------------------------------------------

    /**
     * Overworld <-> Sand Dimension.
     */
    public static void travelSand(ServerPlayer sp, BlockPos portalPos, Axis axis) {
        travelMirror(
                sp,
                portalPos,
                axis,
                DimKeys.SAND,                        // mirror dimension
                BCRegistry.SAND_PORTAL.get(),        // portal block in that dim
                PortalUtils::canUseSandPortal,       // dimension gating
                PortalUtils::buildSandReturnPortal   // frame + base builder
        );
    }

    /**
     * Overworld <-> RU Mirror Dimension.
     */
    public static void travelRu(ServerPlayer sp, BlockPos portalPos, Axis axis) {
        travelMirror(
                sp,
                portalPos,
                axis,
                DimKeys.MIRROR_RU,
                BCRegistry.GRANITE_PORTAL.get(),
                PortalUtils::canUseRuPortal,
                PortalUtils::buildRuReturnPortal
        );
    }

    /**
     * Overworld <-> BWG Mirror Dimension.
     */
    public static void travelBwg(ServerPlayer sp, BlockPos portalPos, Axis axis) {
        travelMirror(
                sp,
                portalPos,
                axis,
                DimKeys.MIRROR_BWG,
                BCRegistry.DIORITE_PORTAL.get(),
                PortalUtils::canUseBwgPortal,
                PortalUtils::buildBwgReturnPortal
        );
    }

    // -------------------------------------------------------------------------
    // Core shared logic
    // -------------------------------------------------------------------------

    /**
     * Generic "mirror" travel:
     *  - Works between Overworld and a single mirror dimension.
     *  - Uses 1:1 coordinates (same X/Z anchor as current portal).
     *  - Reuses existing portal if found in target dim.
     *  - Otherwise finds a safe pad, builds a return portal, and teleports there.
     */
    private static void travelMirror(ServerPlayer sp,
                                     BlockPos portalPos,
                                     Axis axis,
                                     ResourceKey<Level> mirrorDim,
                                     Block portalBlock,
                                     Predicate<Level> canUsePredicate,
                                     BiConsumer<ServerLevel, BlockPos> buildReturnPortal) {

        Level level = sp.level();

        // Dimension gating: only allow Overworld <-> mirrorDim
        if (!canUsePredicate.test(level)) {
            return;
        }

        // Cooldown so we don't bounce back and forth instantly
        if (!PortalUtils.checkAndSetPortalCooldown(sp, level, 30)) {
            return;
        }

        // Decide which way we're going: Overworld <-> mirrorDim
        ResourceKey<Level> toKey = (level.dimension() == mirrorDim)
                ? Level.OVERWORLD
                : mirrorDim;

        ServerLevel target = sp.server.getLevel(toKey);
        if (target == null) {
            return;
        }

        // Anchor around the portal's block position.
        // This preserves "same X/Y/Z" semantics between dimensions at a high level.
        BlockPos origin = new BlockPos(
                portalPos.getX(),
                portalPos.getY(),
                portalPos.getZ()
        );

        // 1) Try to find an existing portal in the target dimension near that same spot
        BlockPos existing = PortalUtils.findExistingPortal(
                target,
                origin,
                32,          // search radius
                portalBlock,
                null
        );

        if (existing != null) {
            // Teleport into the existing portal we found
            sp.teleportTo(
                    target,
                    existing.getX() + 0.5,
                    existing.getY() + 0.1,
                    existing.getZ() + 0.5,
                    sp.getYRot(),
                    sp.getXRot()
            );
            return;
        }

        // 2) No portal found → find a safe pad and build a fresh return portal

        // This will move us out of solid stone or into a nearby cave if needed.
        BlockPos base = PortalPlacement.findSafePad(target, origin);

        // Build the frame + base at that pad
        buildReturnPortal.accept(target, base);

        // Land in the center of the pad, same as your old SandPortalBlock behavior
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

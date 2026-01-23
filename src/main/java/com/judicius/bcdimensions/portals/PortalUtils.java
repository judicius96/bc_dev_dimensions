package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

final class PortalUtils {
    private PortalUtils() {}

    static void spawnPortalParticles(Level level, BlockPos pos, RandomSource random) {
        // ambient sound occasionally
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    net.minecraft.sounds.SoundEvents.PORTAL_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.5F,
                    random.nextFloat() * 0.4F + 0.8F,
                    false
            );
        }

        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        level.addParticle(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                x, y, z,
                0.0, 0.0, 0.0
        );
    }

    static boolean canUseSandPortal(Level level) {
        return level.dimension() == Level.OVERWORLD
                || level.dimension() == DimKeys.SAND;
    }

    static boolean canUseMiningPortal(Level level) {
        return level.dimension() == Level.OVERWORLD
                || level.dimension() == DimKeys.MINING;
    }

    // -------------------------------------------------------------------------
    // cooldown
    // -------------------------------------------------------------------------
    static boolean checkAndSetPortalCooldown(ServerPlayer sp, Level level, int ticks) {
        if (sp.isOnPortalCooldown()) return false;
        long now = level.getGameTime();
        if (PortalCooldowns.isCooling(sp, now)) return false;
        sp.setPortalCooldown();
        PortalCooldowns.mark(sp, now, ticks);
        return true;
    }

    // -------------------------------------------------------------------------
    // generic portal finder
    // -------------------------------------------------------------------------
    static BlockPos findExistingPortal(Level level,
                                       BlockPos origin,
                                       int radius,
                                       Block portalBlock,
                                       Direction.Axis axis) {  // axis may be null
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int r = radius;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(cursor);

                    if (!state.is(portalBlock)) {
                        continue;
                    }

                    // Only enforce axis if the caller actually cares
                    if (axis != null && state.hasProperty(NetherPortalBlock.AXIS)) {
                        Direction.Axis found = state.getValue(NetherPortalBlock.AXIS);
                        if (found != axis) {
                            continue;
                        }
                    }

                    return cursor.immutable();
                }
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // sand portal builder (also used for return portal)
    // -------------------------------------------------------------------------
    static void buildSandReturnPortal(ServerLevel level, BlockPos base) {
        if (level == null || base == null) {
            return;
        }

        // 5x4 sandstone pad under portal
        for (int x = -2; x <= 2; x++) {
            for (int z = 0; z <= 3; z++) {
                level.setBlock(base.offset(x, -1, z), Blocks.SANDSTONE.defaultBlockState(), 3);
            }
        }

        // frame at x = 0, z = 0..3, y = 0..4
        for (int y = 0; y < 5; y++) {
            level.setBlock(base.offset(0, y, 0), Blocks.SANDSTONE.defaultBlockState(), 3);
            level.setBlock(base.offset(0, y, 3), Blocks.SANDSTONE.defaultBlockState(), 3);
        }
        for (int z = 0; z < 4; z++) {
            level.setBlock(base.offset(0, 0, z), Blocks.SANDSTONE.defaultBlockState(), 3);
            level.setBlock(base.offset(0, 4, z), Blocks.SANDSTONE.defaultBlockState(), 3);
        }

        BlockState portal = BCRegistry.SAND_PORTAL.get()
                .defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Direction.Axis.Z);

        // fill inside: z = 1..2, y = 1..3
        for (int z = 1; z <= 2; z++) {
            for (int y = 1; y <= 3; y++) {
                level.setBlock(base.offset(0, y, z), portal, 3);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mining portal builder (also guarded)
    // -------------------------------------------------------------------------
    static void buildMiningReturnPortal(ServerLevel level, BlockPos base) {
        if (level == null || base == null) {
            return;
        }

        // pad: same shape, different block
        for (int x = -2; x <= 2; x++) {
            for (int z = 0; z <= 3; z++) {
                level.setBlock(base.offset(x, -1, z), Blocks.POLISHED_GRANITE.defaultBlockState(), 3);
            }
        }

        for (int y = 0; y < 5; y++) {
            level.setBlock(base.offset(0, y, 0), Blocks.POLISHED_GRANITE.defaultBlockState(), 3);
            level.setBlock(base.offset(0, y, 3), Blocks.POLISHED_GRANITE.defaultBlockState(), 3);
        }
        for (int z = 0; z < 4; z++) {
            level.setBlock(base.offset(0, 0, z), Blocks.POLISHED_GRANITE.defaultBlockState(), 3);
            level.setBlock(base.offset(0, 4, z), Blocks.POLISHED_GRANITE.defaultBlockState(), 3);
        }

        BlockState portal = BCRegistry.GRANITE_PORTAL.get()
                .defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Direction.Axis.Z);

        for (int z = 1; z <= 2; z++) {
            for (int y = 1; y <= 3; y++) {
                level.setBlock(base.offset(0, y, z), portal, 3);
            }
        }
    }

}

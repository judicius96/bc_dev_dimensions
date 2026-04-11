package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.BCDimensionsConfig;
import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

final class PortalUtils {
    private PortalUtils() {}

    static void spawnPortalParticles(Level level, BlockPos pos, RandomSource random) {
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

    static boolean checkAndSetPortalCooldown(ServerPlayer sp, Level level, int ticks) {
        if (sp.isOnPortalCooldown()) return false;
        long now = level.getGameTime();
        if (PortalCooldowns.isCooling(sp, now)) return false;
        sp.setPortalCooldown();
        PortalCooldowns.mark(sp, now, ticks);
        return true;
    }

    private static Block getFrameBlock(List<? extends String> configList, Block fallback) {
        if (configList == null || configList.isEmpty()) return fallback;
        ResourceLocation id = ResourceLocation.tryParse(configList.get(0));
        if (id == null) return fallback;
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return block != null ? block : fallback;
    }

    static void buildSandReturnPortal(ServerLevel level, BlockPos base) {
        if (level == null || base == null) return;

        Block frame = getFrameBlock(BCDimensionsConfig.SAND_PORTAL_FRAME_BLOCKS.get(), Blocks.SANDSTONE);

        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < 5; y++)
                for (int z = 0; z <= 3; z++)
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);

        for (int x = -2; x <= 2; x++)
            for (int z = 0; z <= 3; z++)
                level.setBlock(base.offset(x, -1, z), frame.defaultBlockState(), 3);

        for (int y = 0; y < 5; y++) {
            level.setBlock(base.offset(0, y, 0), frame.defaultBlockState(), 3);
            level.setBlock(base.offset(0, y, 3), frame.defaultBlockState(), 3);
        }
        for (int z = 0; z < 4; z++) {
            level.setBlock(base.offset(0, 0, z), frame.defaultBlockState(), 3);
            level.setBlock(base.offset(0, 4, z), frame.defaultBlockState(), 3);
        }

        BlockState portal = BCRegistry.SAND_PORTAL.get()
                .defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Direction.Axis.Z);

        for (int z = 1; z <= 2; z++)
            for (int y = 1; y <= 3; y++)
                level.setBlock(base.offset(0, y, z), portal, 3);
    }

    static BlockPos findSurfacePos(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }

    static void buildMiningReturnPortal(ServerLevel level, BlockPos base) {
        if (level == null || base == null) return;

        Block frame = getFrameBlock(BCDimensionsConfig.MINING_PORTAL_FRAME_BLOCKS.get(), Blocks.POLISHED_GRANITE);

        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < 5; y++)
                for (int z = 0; z <= 3; z++)
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);

        for (int x = -2; x <= 2; x++)
            for (int z = 0; z <= 3; z++)
                level.setBlock(base.offset(x, -1, z), frame.defaultBlockState(), 3);

        for (int y = 0; y < 5; y++) {
            level.setBlock(base.offset(0, y, 0), frame.defaultBlockState(), 3);
            level.setBlock(base.offset(0, y, 3), frame.defaultBlockState(), 3);
        }
        for (int z = 0; z < 4; z++) {
            level.setBlock(base.offset(0, 0, z), frame.defaultBlockState(), 3);
            level.setBlock(base.offset(0, 4, z), frame.defaultBlockState(), 3);
        }

        BlockState portal = BCRegistry.GRANITE_PORTAL.get()
                .defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Direction.Axis.Z);

        for (int z = 1; z <= 2; z++)
            for (int y = 1; y <= 3; y++)
                level.setBlock(base.offset(0, y, z), portal, 3);
    }
}
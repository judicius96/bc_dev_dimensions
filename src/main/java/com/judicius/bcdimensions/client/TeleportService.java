package com.judicius.bcdimensions;

import com.judicius.bcdimensions.portals.SandPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Direction.Axis;

final class TeleportService {
    private TeleportService() {}

    static void teleport(ServerPlayer sp, Level from, ResourceKey<Level> toKey) {
        ServerLevel target = sp.server.getLevel(toKey);
        if (target == null) return;

        int x = (int)Math.floor(sp.getX());
        int z = (int)Math.floor(sp.getZ());
        if (toKey == Level.OVERWORLD) {
            BlockPos spawn = target.getSharedSpawnPos();
            x = spawn.getX();
            z = spawn.getZ();
        }

        int y = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= target.getMinBuildHeight() + 1) {
            y = target.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        }
        y = Math.max(y, target.getSeaLevel() + 1);
        y = Math.max(y, target.getMinBuildHeight() + 2);
        y = Math.min(y, target.getMaxBuildHeight() - 2);

        BlockPos base = new BlockPos(x, y, z);

        // 3x3 pad + 4x5 Z-facing frame + 2x3 portal
        for (int dx=0; dx<3; dx++) for (int dz=0; dz<3; dz++)
            target.setBlock(base.offset(dx, -1, dz), Blocks.SANDSTONE.defaultBlockState(), 3);
        for (int yy=0; yy<5; yy++) {
            target.setBlock(base.offset(0, yy, 0), Blocks.SANDSTONE.defaultBlockState(), 3);
            target.setBlock(base.offset(0, yy, 3), Blocks.SANDSTONE.defaultBlockState(), 3);
        }
        for (int dz=0; dz<4; dz++) {
            target.setBlock(base.offset(0, 0, dz), Blocks.SANDSTONE.defaultBlockState(), 3);
            target.setBlock(base.offset(0, 4, dz), Blocks.SANDSTONE.defaultBlockState(), 3);
        }
        var portalZ = BCRegistry.SAND_PORTAL.get().defaultBlockState().setValue(SandPortalBlock.AXIS, Axis.Z);
        for (int dz=1; dz<=2; dz++) for (int yy=1; yy<=3; yy++)
            target.setBlock(base.offset(0, yy, dz), portalZ, 3);

        sp.teleportTo(target, x + 2.5, y + 1, z + 2.5, sp.getYRot(), sp.getXRot());
    }
}
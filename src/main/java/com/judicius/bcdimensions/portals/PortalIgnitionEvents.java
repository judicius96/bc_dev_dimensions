package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = BCDimensions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PortalIgnitionEvents {

    private static final Logger LOGGER = LogManager.getLogger("BCDimensions|PortalIgnite");

    private PortalIgnitionEvents(){}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        if (e.getItemStack().getItem() != Items.FLINT_AND_STEEL) return;

        LOGGER.info("[PortalIgnite] RightClickBlock: player={} dim={} pos={}",
                e.getEntity().getGameProfile().getName(),
                level.dimension().location(),
                e.getPos());

        if (tryIgniteNear(level, e.getPos(), e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        if (e.getItemStack().getItem() != Items.FLINT_AND_STEEL) return;

        LOGGER.info("[PortalIgnite] RightClickItem: player={} dim={} pos={}",
                e.getEntity().getGameProfile().getName(),
                level.dimension().location(),
                e.getEntity().blockPosition());

        if (tryIgniteNear(level, e.getEntity().blockPosition(), e.getEntity())) {
            e.setCanceled(true);
        }
    }

    private static boolean tryIgniteNear(ServerLevel level, BlockPos center, Player player) {
        LOGGER.info("[PortalIgnite] Scanning for frames around {} in dim={}",
                center, level.dimension().location());

        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);

                    // existing sandstone portal
                    if (validFrameX(level, p)) {
                        LOGGER.info("[PortalIgnite] Found SANDSTONE frame (X) at {}", p);
                        fillPortalX(level, p);
                        return true;
                    }
                    if (validFrameZ(level, p)) {
                        LOGGER.info("[PortalIgnite] Found SANDSTONE frame (Z) at {}", p);
                        fillPortalZ(level, p);
                        return true;
                    }

                    // existing polished granite → RU portal
                    if (validGraniteFrameX(level, p)) {
                        LOGGER.info("[PortalIgnite] Found GRANITE frame (X) at {}", p);
                        fillGranitePortalX(level, p);
                        return true;
                    }
                    if (validGraniteFrameZ(level, p)) {
                        LOGGER.info("[PortalIgnite] Found GRANITE frame (Z) at {}", p);
                        fillGranitePortalZ(level, p);
                        return true;
                    }
                }
            }
        }

        LOGGER.info("[PortalIgnite] No valid frame found around {}", center);
        return false;
    }

    // ========== ORIGINAL SANDSTONE STUFF (leave as-is) ==========
    private static boolean validFrameX(ServerLevel level, BlockPos bl) {
        for (int x=0;x<4;x++) {
            if (!isSandstone(level.getBlockState(bl.offset(x, 0, 0)))) return false;
            if (!isSandstone(level.getBlockState(bl.offset(x, 4, 0)))) return false;
        }
        for (int y=0;y<5;y++) {
            if (!isSandstone(level.getBlockState(bl.offset(0, y, 0)))) return false;
            if (!isSandstone(level.getBlockState(bl.offset(3, y, 0)))) return false;
        }
        for (int x=1;x<=2;x++) for (int y=1;y<=3;y++) {
            BlockState s = level.getBlockState(bl.offset(x, y, 0));
            if (!s.isAir() && !s.is(BCRegistry.SAND_PORTAL.get())) return false;
        }
        return true;
    }

    private static boolean validFrameZ(ServerLevel level, BlockPos bl) {
        for (int z=0;z<4;z++) {
            if (!isSandstone(level.getBlockState(bl.offset(0, 0, z)))) return false;
            if (!isSandstone(level.getBlockState(bl.offset(0, 4, z)))) return false;
        }
        for (int y=0;y<5;y++) {
            if (!isSandstone(level.getBlockState(bl.offset(0, y, 0)))) return false;
            if (!isSandstone(level.getBlockState(bl.offset(0, y, 3)))) return false;
        }
        for (int z=1;z<=2;z++) for (int y=1;y<=3;y++) {
            BlockState s = level.getBlockState(bl.offset(0, y, z));
            if (!s.isAir() && !s.is(BCRegistry.SAND_PORTAL.get())) return false;
        }
        return true;
    }

    private static void fillPortalX(ServerLevel level, BlockPos bl) {
        var state = BCRegistry.SAND_PORTAL.get().defaultBlockState().setValue(SandPortalBlock.AXIS, Axis.X);
        BlockPos inner = bl.offset(1, 1, 0);
        for (int x=0;x<2;x++) for (int y=0;y<3;y++)
            level.setBlock(inner.offset(x, y, 0), state, 3);
    }

    private static void fillPortalZ(ServerLevel level, BlockPos bl) {
        var state = BCRegistry.SAND_PORTAL.get().defaultBlockState().setValue(SandPortalBlock.AXIS, Axis.Z);
        BlockPos inner = bl.offset(0, 1, 1);
        for (int z=0;z<2;z++) for (int y=0;y<3;y++)
            level.setBlock(inner.offset(0, y, z), state, 3);
    }

    private static boolean isSandstone(BlockState s) {
        return s.is(Blocks.SANDSTONE) || s.is(Blocks.CUT_SANDSTONE) || s.is(Blocks.SMOOTH_SANDSTONE);
    }
    // ============================================================

    // ========== NEW GRANITE / MINING PORTAL ADDITIONS ==========

    private static boolean isPolishedGranite(BlockState s) {
        return s.is(Blocks.POLISHED_GRANITE);
    }

    private static boolean validGraniteFrameX(ServerLevel level, BlockPos bl) {
        for (int x=0;x<4;x++) {
            if (!isPolishedGranite(level.getBlockState(bl.offset(x, 0, 0)))) return false;
            if (!isPolishedGranite(level.getBlockState(bl.offset(x, 4, 0)))) return false;
        }
        for (int y=0;y<5;y++) {
            if (!isPolishedGranite(level.getBlockState(bl.offset(0, y, 0)))) return false;
            if (!isPolishedGranite(level.getBlockState(bl.offset(3, y, 0)))) return false;
        }
        for (int x=1;x<=2;x++) for (int y=1;y<=3;y++) {
            BlockState s = level.getBlockState(bl.offset(x, y, 0));
            if (!s.isAir() && !s.is(BCRegistry.GRANITE_PORTAL.get())) return false;
        }
        return true;
    }

    private static boolean validGraniteFrameZ(ServerLevel level, BlockPos bl) {
        for (int z=0;z<4;z++) {
            if (!isPolishedGranite(level.getBlockState(bl.offset(0, 0, z)))) return false;
            if (!isPolishedGranite(level.getBlockState(bl.offset(0, 4, z)))) return false;
        }
        for (int y=0;y<5;y++) {
            if (!isPolishedGranite(level.getBlockState(bl.offset(0, y, 0)))) return false;
            if (!isPolishedGranite(level.getBlockState(bl.offset(0, y, 3)))) return false;
        }
        for (int z=1;z<=2;z++) for (int y=1;y<=3;y++) {
            BlockState s = level.getBlockState(bl.offset(0, y, z));
            if (!s.isAir() && !s.is(BCRegistry.GRANITE_PORTAL.get())) return false;
        }
        return true;
    }

    private static void fillGranitePortalX(ServerLevel level, BlockPos bl) {
        var state = BCRegistry.GRANITE_PORTAL.get().defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Axis.X);
        BlockPos inner = bl.offset(1, 1, 0);
        for (int x=0;x<2;x++) for (int y=0;y<3;y++)
            level.setBlock(inner.offset(x, y, 0), state, 3);
    }

    private static void fillGranitePortalZ(ServerLevel level, BlockPos bl) {
        var state = BCRegistry.GRANITE_PORTAL.get().defaultBlockState()
                .setValue(SandPortalBlock.AXIS, Axis.Z);
        BlockPos inner = bl.offset(0, 1, 1);
        for (int z=0;z<2;z++) for (int y=0;y<3;y++)
            level.setBlock(inner.offset(0, y, z), state, 3);
    }

}

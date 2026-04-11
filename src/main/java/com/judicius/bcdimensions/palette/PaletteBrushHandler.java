package com.judicius.bcdimensions.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "bc_dimensions")
public class PaletteBrushHandler {

    private static final String PALETTE_DIM = "bc_dimensions:palette";
    private static final String WRONG_DIM_MSG = "§cThis tool doesn't work in this dimension.";

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PaletteBrush)) return;

        // Outside Palette — cancel and warn
        if (!player.level().dimension().location().toString().equals(PALETTE_DIM)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal(WRONG_DIM_MSG), true);
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return;

        // Never interact with palette_block floor
        ResourceLocation blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId != null && blockId.toString().equals("bc_dimensions:palette_block")) return;

        PaletteProtectionData protection = PaletteProtectionData.get(level);

        // Shift+left-click — protect the block
        if (player.isShiftKeyDown()) {
            event.setCanceled(true);
            event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
            event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);

            if (protection.isProtected(pos)) {
                UUID owner = protection.getOwner(pos);
                String ownerName = player.getServer().getProfileCache()
                        .get(owner).map(p -> p.getName()).orElse("unknown");
                player.displayClientMessage(Component.literal("§eThis block is already protected by " + ownerName + "."), true);
            } else {
                protection.protect(pos, player.getUUID());
                player.displayClientMessage(Component.literal("§aBlock protected."), true);
            }
            return;
        }

        event.setCanceled(true);
        event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
        event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);

        // Regular left-click — check protection before breaking
        if (protection.isProtected(pos)) {
            UUID owner = protection.getOwner(pos);
            String ownerName = player.getServer().getProfileCache()
                    .get(owner).map(p -> p.getName()).orElse("unknown");
            player.displayClientMessage(Component.literal("§cThis block is protected by " + ownerName + "."), true);
            return;
        }

        // Instant break — no hardness check, no drop
        level.removeBlock(pos, false);
        level.levelEvent(2001, pos, Block.getId(state));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PaletteBrush)) return;

        // Outside Palette — handled by PaletteBrush.useOn
        if (!player.level().dimension().location().toString().equals(PALETTE_DIM)) return;

        // Only handle shift+right-click here — regular right-click handled by PaletteBrush.useOn
        if (!player.isShiftKeyDown()) return;

        ServerLevel level = player.serverLevel();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return;

        // Never interact with palette_block floor
        ResourceLocation blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId != null && blockId.toString().equals("bc_dimensions:palette_block")) return;

        event.setCanceled(true);

        PaletteProtectionData protection = PaletteProtectionData.get(level);

        if (!protection.isProtected(pos)) {
            player.displayClientMessage(Component.literal("§eThis block is not protected."), true);
            return;
        }

        UUID owner = protection.getOwner(pos);
        boolean isOwner = owner.equals(player.getUUID());
        boolean isOp = player.hasPermissions(2) && player.isCreative();

        if (isOwner || isOp) {
            protection.unprotect(pos);
            player.displayClientMessage(Component.literal("§aBlock protection removed."), true);
        } else {
            String ownerName = player.getServer().getProfileCache()
                    .get(owner).map(p -> p.getName()).orElse("unknown");
            player.displayClientMessage(Component.literal("§cYou cannot unprotect this block. It belongs to " + ownerName + "."), true);
        }
    }
}

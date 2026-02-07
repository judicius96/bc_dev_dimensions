package com.judicius.bcdimensions.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "bc_dimensions")
public class PaletteBlockRestrictions {

    private static final String PALETTE_DIM = "bc_dimensions:palette";
    private static final String BLOCK_MESSAGE = "§cYou can't use this type of spatial magic in this dimension.";

    @SubscribeEvent
    public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();

        // Only in Palette dimension
        if (!level.dimension().location().toString().equals(PALETTE_DIM)) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Check if item is a blocked block
        if (stack.getItem() instanceof BlockItem blockItem) {
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockItem.getBlock());
            if (blockId != null && isBlockedBlock(blockId)) {
                event.setCanceled(true);
                if (player instanceof ServerPlayer) {
                    player.displayClientMessage(Component.literal(BLOCK_MESSAGE), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = player.level();

        // Only in Palette dimension
        if (!level.dimension().location().toString().equals(PALETTE_DIM)) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && isBlockedItem(itemId)) {
            event.setCanceled(true);
            if (player instanceof ServerPlayer) {
                player.displayClientMessage(Component.literal(BLOCK_MESSAGE), true);
            }
        }
    }

    private static boolean isBlockedBlock(ResourceLocation blockId) {
        String id = blockId.toString();

        // Vanilla ender chest
        if (id.equals("minecraft:ender_chest")) return true;

        // EnderStorage mod blocks
        if (blockId.getNamespace().equals("enderstorage")) return true;

        // EnderTanks mod
        if (id.contains("ender_tank")) return true;

        return false;
    }

    private static boolean isBlockedItem(ResourceLocation itemId) {
        String id = itemId.toString();

        // EnderStorage pouch
        if (id.contains("ender_pouch")) return true;

        // Any other ender-based portable storage
        if (id.contains("ender") && (id.contains("pouch") || id.contains("bag"))) return true;

        return false;
    }
}

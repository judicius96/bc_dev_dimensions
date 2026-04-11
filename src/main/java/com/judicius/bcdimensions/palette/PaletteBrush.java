package com.judicius.bcdimensions.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class PaletteBrush extends Item {

    private static final String PALETTE_DIM = "bc_dimensions:palette";
    private static final String WRONG_DIM_MSG = "§cThis tool doesn't work in this dimension.";

    public PaletteBrush() {
        super(new Item.Properties().stacksTo(1));
    }

    // Right click on block face — give a full stack of that block
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        if (!isInPalette(level)) {
            player.displayClientMessage(Component.literal(WRONG_DIM_MSG), true);
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) return InteractionResult.FAIL;

        Item blockItem = ForgeRegistries.ITEMS.getValue(blockId);
        if (blockItem == null) return InteractionResult.FAIL;

        ItemStack stack = new ItemStack(blockItem, blockItem.getMaxStackSize());

        // Try to add to inventory, drop at player if full
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }

        return InteractionResult.SUCCESS;
    }

    // Left click — instant break ignoring hardness, handled via event in PaletteBrushHandler
    // Item itself just needs to be identifiable as a PaletteBrush for the handler to check

    private boolean isInPalette(Level level) {
        return level.dimension().location().toString().equals(PALETTE_DIM);
    }
}

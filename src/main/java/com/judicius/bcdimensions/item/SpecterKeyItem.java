package com.judicius.bcdimensions.item;

import com.judicius.bcdimensions.portals.DimKeys;
import com.judicius.bcdimensions.specter.SpecterHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpecterKeyItem extends Item {

    public SpecterKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (serverPlayer.level().dimension() == DimKeys.SPECTER) {
            // Exit specter dimension
            ServerLevel specterWorld = (ServerLevel) serverPlayer.level();
            SpecterHandler handler = SpecterHandler.get(specterWorld);
            handler.teleportPlayerBack(serverPlayer);
        } else {
            // Enter specter dimension
            ServerLevel specterWorld = serverPlayer.server.getLevel(DimKeys.SPECTER);
            if (specterWorld != null) {
                SpecterHandler handler = SpecterHandler.get(specterWorld);
                handler.teleportPlayerToCube(serverPlayer);
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}

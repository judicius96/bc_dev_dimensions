package com.judicius.bcdimensions.item;

import com.judicius.bcdimensions.portals.DimKeys;
import com.judicius.bcdimensions.spectral.SpectralHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpectralKeyItem extends Item {

    public SpectralKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (serverPlayer.level().dimension() == DimKeys.SPECTRAL) {
            // Exit spectral dimension
            ServerLevel spectralWorld = (ServerLevel) serverPlayer.level();
            SpectralHandler handler = SpectralHandler.get(spectralWorld);
            handler.teleportPlayerBack(serverPlayer);
        } else {
            // Enter spectral dimension
            ServerLevel spectralWorld = serverPlayer.server.getLevel(DimKeys.SPECTRAL);
            if (spectralWorld != null) {
                SpectralHandler handler = SpectralHandler.get(spectralWorld);
                handler.teleportPlayerToCube(serverPlayer);
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}

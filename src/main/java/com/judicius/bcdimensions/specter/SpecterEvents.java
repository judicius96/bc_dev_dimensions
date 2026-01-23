package com.judicius.bcdimensions.specter;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.portals.DimKeys;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BCDimensions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpecterEvents {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().dimension() != DimKeys.SPECTER) return;

        // Only check every 20 ticks (once per second) to reduce performance impact
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;

            ServerLevel specterWorld = (ServerLevel) serverPlayer.level();
            SpecterHandler handler = SpecterHandler.get(specterWorld);
            handler.checkPlayerPosition(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        // Get specter dimension
        ServerLevel specterWorld = serverPlayer.server.getLevel(DimKeys.SPECTER);
        if (specterWorld != null) {
            SpecterHandler handler = SpecterHandler.get(specterWorld);
            handler.unloadPlayerCube(serverPlayer.getUUID());
        }
    }
}
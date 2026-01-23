package com.judicius.bcdimensions.client;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BCDimensions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BCClient {
    private BCClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // old sand portal
            if (BCRegistry.SAND_PORTAL.get() != null) {
                ItemBlockRenderTypes.setRenderLayer(BCRegistry.SAND_PORTAL.get(), RenderType.translucent());
            }
            // new granite portal
            if (BCRegistry.GRANITE_PORTAL.get() != null) {
                ItemBlockRenderTypes.setRenderLayer(BCRegistry.GRANITE_PORTAL.get(), RenderType.translucent());
            }
        });
    }
}


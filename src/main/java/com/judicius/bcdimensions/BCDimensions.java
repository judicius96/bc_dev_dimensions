package com.judicius.bcdimensions;

import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BCDimensions.MODID)
public class BCDimensions {
    public static final String MODID = "bc_dimensions";

    public BCDimensions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all BC blocks/items/etc. (currently just blocks; add others here if BCRegistry has them)
        BCRegistry.BLOCKS.register(modBus);
        BCRegistry.ITEMS.register(modBus);
        BCRegistry.CHUNK_GENERATORS.register(modBus);

    }
}

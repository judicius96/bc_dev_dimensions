package com.judicius.bcdimensions;

import com.judicius.bcdimensions.compat.bwg.BwgCompat;
import com.judicius.bcdimensions.compat.bwg.BwgDimRegions;
//import com.judicius.bcdimensions.compat.bwg.BwgMirrorBiomeSource;
import com.judicius.bcdimensions.compat.bwg.BwgMirrorBiomeSource;
import com.judicius.bcdimensions.compat.ru.RuMirrorBiomeSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BCDimensions.MODID)
public class BCDimensions {
    public static final String MODID = "bc_dimensions";

    public BCDimensions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all BC blocks/items/etc. (currently just blocks; add others here if BCRegistry has them)
        BCRegistry.BLOCKS.register(modBus);

        // Run our setup
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register custom biome sources used by our dimensions
            Registry.register(
                    BuiltInRegistries.BIOME_SOURCE,
                    new ResourceLocation(MODID, "ru_mirror"),
                    RuMirrorBiomeSource.CODEC
            );
            Registry.register(
                    BuiltInRegistries.BIOME_SOURCE,
                    new ResourceLocation(MODID, "bwg_mirror"),
                    BwgMirrorBiomeSource.CODEC
            );

            // Hook BWG surface rules into our BWG dimension
            BwgCompat.hookSurfaceRules();

            // Register our cloned BWG regions for the mirror dimension
           BwgDimRegions.register();
        });
    }
}

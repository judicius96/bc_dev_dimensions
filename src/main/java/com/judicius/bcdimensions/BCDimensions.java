package com.judicius.bcdimensions;

import com.judicius.bcdimensions.registry.BCBiomes;
import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BCDimensions.MODID)
public class BCDimensions {

    public static final String MODID = "bc_dimensions";

    public BCDimensions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BCBiomes.init();
        BCRegistry.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BCDimensionsConfig.SPEC, "bc_dimensions-common.toml");
    }
}
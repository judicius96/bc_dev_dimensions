package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public final class BCPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, BCDimensions.MODID);

    public static final RegistryObject<PoiType> SAND_PORTAL_POI =
            POI_TYPES.register("sand_portal", () ->
                    new PoiType(
                            Set.copyOf(BCRegistry.SAND_PORTAL.get().getStateDefinition().getPossibleStates()),
                            0,
                            1
                    ));

    public static final RegistryObject<PoiType> MINING_PORTAL_POI =
            POI_TYPES.register("mining_portal", () ->
                    new PoiType(
                            Set.copyOf(BCRegistry.GRANITE_PORTAL.get().getStateDefinition().getPossibleStates()),
                            0,
                            1
                    ));

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
    }

    private BCPoiTypes() {}
}
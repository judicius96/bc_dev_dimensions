package com.judicius.bcdimensions.portals;

import com.judicius.bcdimensions.BCDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class DimKeys {
    private DimKeys() {}

    public static final ResourceKey<Level> SAND =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(BCDimensions.MODID, "sand_dimension"));
/*
    public static final ResourceKey<Level> MINING =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(BCDimensions.MODID, "mining_dimension"));
*/
    public static final ResourceKey<Level> MIRROR_RU =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(BCDimensions.MODID, "ru_mirror_dimension"));

    public static final ResourceKey<Level> MIRROR_BWG =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(BCDimensions.MODID, "bwg_mirror_dimension"));
}

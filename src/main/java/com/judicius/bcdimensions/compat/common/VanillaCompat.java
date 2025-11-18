package com.judicius.bcdimensions.compat.common;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Utility to inject the vanilla overworld multi-noise climate grid
 * (plains, forests, all oceans, etc.) into a mirror dimension.
 *
 * Reads minecraft:overworld from the MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST
 * registry and converts its Holder<Biome> entries to ResourceKey<Biome>.
 */
public final class VanillaCompat {

    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|VanillaCompat");

    private VanillaCompat() {
        // utility
    }

    public static void addOverworldBiomes(RegistryAccess registryAccess,
                                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> adder) {

        // Registry of multi-noise parameter lists (one of which is minecraft:overworld)
        Registry<MultiNoiseBiomeSourceParameterList> paramRegistry =
                registryAccess.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        ResourceKey<MultiNoiseBiomeSourceParameterList> overworldKey =
                ResourceKey.create(
                        Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
                        new ResourceLocation("minecraft", "overworld")
                );

        MultiNoiseBiomeSourceParameterList overworldParams = paramRegistry.get(overworldKey);
        if (overworldParams == null) {
            LOGGER.warn("VanillaCompat: no MultiNoiseBiomeSourceParameterList for minecraft:overworld; vanilla biomes will not be added.");
            return;
        }

        // Get the actual climate parameter list for the overworld
        Climate.ParameterList<Holder<Biome>> params = overworldParams.parameters();

        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : params.values()) {
            Holder<Biome> holder = entry.getSecond();
            holder.unwrapKey().ifPresent(key -> {
                adder.accept(Pair.of(entry.getFirst(), key));
            });
        }
    }
}

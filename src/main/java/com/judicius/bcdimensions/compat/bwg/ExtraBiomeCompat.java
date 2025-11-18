package com.judicius.bcdimensions.compat.bwg;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

/**
 * Drop-in helper for adding extra mod biomes to the BWG mirror regions,
 * but only if those mods are actually loaded.
 */
public final class ExtraBiomeCompat {
    private ExtraBiomeCompat() {}

    public static void addExtraBiomes(
            Registry<Biome> biomeRegistry,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out
    ) {
        // Ars Elemental
        if (ModList.get().isLoaded("ars_elemental")) {
            add(out, "ars_elemental", "blazing_forest", warmDry());
            add(out, "ars_elemental", "cascading_forest", temperateHumid());
            add(out, "ars_elemental", "flashing_forest", temperate());
            add(out, "ars_elemental", "flourishing_forest", temperateHumid());
        }

        // Ars Nouveau
        if (ModList.get().isLoaded("ars_nouveau")) {
            add(out, "ars_nouveau", "archwood_forest", temperate());
        }

        // Hexerei
        if (ModList.get().isLoaded("hexerei")) {
            add(out, "hexerei", "willow_swamp", swampy());
        }
    }

    private static void add(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out,
                            String modid,
                            String path,
                            Climate.ParameterPoint point) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation(modid, path));
        out.accept(Pair.of(point, key));
    }

    // ----------------------------------------------------
    // Basic climate presets — swap these later for real tuples
    // ----------------------------------------------------
    private static Climate.ParameterPoint temperate() {
        return Climate.parameters(
                Climate.Parameter.span(0.25F, 0.55F),  // temperature
                Climate.Parameter.span(0.30F, 0.80F),  // humidity
                Climate.Parameter.span(0.0F, 1.0F),    // continentalness
                Climate.Parameter.span(0.0F, 1.0F),    // erosion
                Climate.Parameter.point(0.0F),         // depth
                Climate.Parameter.point(0.0F),         // weirdness
                0.0F                                   // offset
        );
    }

    private static Climate.ParameterPoint temperateHumid() {
        return Climate.parameters(
                Climate.Parameter.span(0.30F, 0.70F),
                Climate.Parameter.span(0.70F, 1.0F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.point(0.0F),
                0.0F
        );
    }

    private static Climate.ParameterPoint warmDry() {
        return Climate.parameters(
                Climate.Parameter.span(0.70F, 1.0F),
                Climate.Parameter.span(0.0F, 0.40F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.point(0.0F),
                0.0F
        );
    }

    private static Climate.ParameterPoint swampy() {
        return Climate.parameters(
                Climate.Parameter.span(0.60F, 0.90F),
                Climate.Parameter.span(0.80F, 1.0F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.span(0.0F, 1.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.point(0.0F),
                0.0F
        );
    }
}

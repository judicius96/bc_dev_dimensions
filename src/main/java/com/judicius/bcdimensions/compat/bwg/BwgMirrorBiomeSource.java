package com.judicius.bcdimensions.compat.bwg;

import com.judicius.bcdimensions.compat.common.VanillaCompat;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Region;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * BWG+vanilla biome source for the BWG mirror dimension.
 *
 * Uses our custom BWG mirror regions:
 *   BwgDimRegions.REGION_1
 *   BwgDimRegions.REGION_2
 *   BwgDimRegions.REGION_3
 *
 * plus:
 *   - ExtraBiomeCompat (Ars, Hexerei, etc.)
 *   - Vanilla overworld multi-noise parameter list via VanillaCompat
 *
 * Region.DEFERRED_PLACEHOLDER mappings are stripped so they never
 * become real biomes in this self-contained dimension.
 *
 * Actual biome selection is delegated to a MultiNoiseBiomeSource
 * built from the combined parameter list, so behavior matches
 * vanilla/TerraBlender instead of a hand-rolled distance check.
 */
public class BwgMirrorBiomeSource extends BiomeSource {

    public static final Codec<BwgMirrorBiomeSource> CODEC = Codec.unit(BwgMirrorBiomeSource::new);
    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|BwgMirrorBiomeSource");

    // Built lazily
    private MultiNoiseBiomeSource delegate;
    private Set<Holder<Biome>> possibleBiomes = Set.of();
    private boolean built = false;

    public BwgMirrorBiomeSource() {
    }

    @Override
    public Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        ensureBuilt();
        if (this.delegate == null) {
            throw new IllegalStateException("BWG mirror: delegate MultiNoiseBiomeSource was not built (no server?).");
        }
        return this.delegate.getNoiseBiome(x, y, z, sampler);
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes() {
        ensureBuilt();
        return this.possibleBiomes.stream();
    }

    // -------------------------------------------------------------------------
    // Build BWG+vanilla climate → biome parameter list and delegate
    // -------------------------------------------------------------------------
    private void ensureBuilt() {
        if (this.built) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.warn("BWG mirror: tried to build biome list without a server; delegate will remain null.");
            this.delegate = null;
            this.possibleBiomes = Set.of();
            this.built = true;
            return;
        }

        var registryAccess = server.registryAccess();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        HolderGetter<Biome> biomeGetter = registryAccess.lookupOrThrow(Registries.BIOME);

        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> rawList = new ArrayList<>();

        // Strip Region.DEFERRED_PLACEHOLDER so placeholders never become biomes here
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> adder = pair -> {
            ResourceKey<Biome> key = pair.getSecond();
            if (key == Region.DEFERRED_PLACEHOLDER) {
                return;
            }
            rawList.add(pair);
        };

        // 1) BWG custom regions
        BwgDimRegions.REGION_1.addBiomes(biomeRegistry, adder);
        BwgDimRegions.REGION_2.addBiomes(biomeRegistry, adder);
        BwgDimRegions.REGION_3.addBiomes(biomeRegistry, adder);

        // 2) Extra mod biomes (Ars, Hexerei, etc.)
        ExtraBiomeCompat.addExtraBiomes(biomeRegistry, adder);

        // 3) Full vanilla overworld climate grid
        VanillaCompat.addOverworldBiomes(registryAccess, adder);

        // Convert to Holder-based ParameterList that MultiNoiseBiomeSource expects
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> holderPairs = new ArrayList<>();
        Set<Holder<Biome>> possible = new HashSet<>();

        for (var entry : rawList) {
            Climate.ParameterPoint point = entry.getFirst();
            ResourceKey<Biome> biomeKey = entry.getSecond();
            biomeGetter.get(biomeKey).ifPresent(holder -> {
                holderPairs.add(Pair.of(point, holder));
                possible.add(holder);
            });
        }

        Climate.ParameterList<Holder<Biome>> parameterList = new Climate.ParameterList<>(holderPairs);
        this.delegate = MultiNoiseBiomeSource.createFromList(parameterList);
        this.possibleBiomes = Set.copyOf(possible);

        long oceanCount = holderPairs.stream()
                .map(p -> p.getSecond().unwrapKey())
                .filter(Optional::isPresent)
                .map(opt -> opt.get().location().getPath())
                .filter(path -> path.contains("ocean"))
                .count();

        LOGGER.info(
                "BWG mirror: built MultiNoiseBiomeSource with {} BWG+compat+vanilla parameter points (placeholders stripped); {} look like ocean biomes.",
                holderPairs.size(), oceanCount
        );

        this.built = true;
    }
}
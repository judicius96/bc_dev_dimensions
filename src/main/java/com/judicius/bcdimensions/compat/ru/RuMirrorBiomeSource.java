package com.judicius.bcdimensions.compat.ru;

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
import net.minecraftforge.server.ServerLifecycleHooks;
import net.regions_unexplored.world.level.biome.RegionPrimaryBiomeBuilder;
import net.regions_unexplored.world.level.biome.RegionSecondaryBiomeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Region;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * RU+vanilla biome source for the RU mirror dimension.
 *
 * Uses RU's own climate builders:
 *   - RegionPrimaryBiomeBuilder
 *   - RegionSecondaryBiomeBuilder
 *
 * plus the vanilla overworld multi-noise parameter list
 * via VanillaCompat.addOverworldBiomes(...).
 *
 * Region.DEFERRED_PLACEHOLDER mappings are stripped so they never
 * become real biomes in this self-contained dimension.
 */
public class RuMirrorBiomeSource extends BiomeSource {

    public static final Codec<RuMirrorBiomeSource> CODEC = Codec.unit(RuMirrorBiomeSource::new);
    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|RuMirrorBiomeSource");

    private List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> ruParams = Collections.emptyList();
    private boolean built = false;

    public RuMirrorBiomeSource() {
    }

    @Override
    public Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        ensureBuilt();
        Climate.TargetPoint target = sampler.sample(x, y, z);
        HolderGetter<Biome> biomeGetter = getServerBiomeGetter();
        if (biomeGetter == null) {
            throw new IllegalStateException("RU mirror: server biome registry not available.");
        }

        Holder<Biome> best = pickClosest(target, biomeGetter, this.ruParams);
        if (best != null) {
            return best;
        }

        throw new IllegalStateException("RU mirror: no RU/vanilla biomes could be resolved from registry.");
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes() {
        ensureBuilt();
        HolderGetter<Biome> biomeGetter = getServerBiomeGetter();
        if (biomeGetter == null) {
            return Stream.empty();
        }
        Set<Holder<Biome>> out = new HashSet<>();
        for (var entry : this.ruParams) {
            biomeGetter.get(entry.getSecond()).ifPresent(out::add);
        }
        return out.stream();
    }

    // -------------------------------------------------------------------------
    // Build RU+vanilla climate → biome parameter list
    // -------------------------------------------------------------------------
    private void ensureBuilt() {
        if (this.built) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.warn("RU mirror: tried to build biome list without a server; leaving empty.");
            this.ruParams = Collections.emptyList();
            this.built = true;
            return;
        }

        var registryAccess = server.registryAccess();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> list = new ArrayList<>();

        // Strip Region.DEFERRED_PLACEHOLDER so placeholders never become biomes here
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> adder = pair -> {
            ResourceKey<Biome> key = pair.getSecond();
            if (key == Region.DEFERRED_PLACEHOLDER) {
                return;
            }
            list.add(pair);
        };

        try {
            // 1) RU climate builders (RU's grid, which already assumes coexistence with vanilla)
            RegionPrimaryBiomeBuilder primary = new RegionPrimaryBiomeBuilder();
            RegionSecondaryBiomeBuilder secondary = new RegionSecondaryBiomeBuilder();

            primary.addBiomes(adder);
            secondary.addBiomes(adder);

            // 2) Full vanilla overworld climate grid
            VanillaCompat.addOverworldBiomes(registryAccess, adder);

        } catch (Exception e) {
            LOGGER.error("RU mirror: error building RU+vanilla mirror biome parameter list", e);
        }

        long oceanCount = list.stream()
                .map(p -> p.getSecond().location().getPath())
                .filter(path -> path.contains("ocean"))
                .count();

        LOGGER.info(
                "RU mirror: collected {} RU+vanilla biome parameter points (placeholders stripped), {} of them look like ocean biomes.",
                list.size(), oceanCount
        );

        this.ruParams = List.copyOf(list);
        this.built = true;
    }

    private static HolderGetter<Biome> getServerBiomeGetter() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.registryAccess().lookupOrThrow(Registries.BIOME);
    }

    // -------------------------------------------------------------------------
    // closest match (same style as BWG mirror)
    // -------------------------------------------------------------------------
    private Holder<Biome> pickClosest(Climate.TargetPoint target,
                                      HolderGetter<Biome> biomeGetter,
                                      List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> candidates) {
        double bestDist = Double.MAX_VALUE;
        Holder<Biome> best = null;

        for (var entry : candidates) {
            Climate.ParameterPoint point = entry.getFirst();
            double dist = climateDistance(point, target);
            if (dist < bestDist) {
                var holder = biomeGetter.get(entry.getSecond());
                if (holder.isPresent()) {
                    bestDist = dist;
                    best = holder.get();
                } else {
                    LOGGER.debug("RU mirror: biome not present in registry: {}", entry.getSecond().location());
                }
            }
        }
        return best;
    }

    private static double climateDistance(Climate.ParameterPoint p, Climate.TargetPoint t) {
        double d = 0.0D;
        d += axisDist(p.temperature(), t.temperature());
        d += axisDist(p.humidity(), t.humidity());
        d += axisDist(p.continentalness(), t.continentalness());
        d += axisDist(p.erosion(), t.erosion());
        d += axisDist(p.depth(), t.depth());
        d += axisDist(p.weirdness(), t.weirdness());
        return d;
    }

    private static double axisDist(Climate.Parameter param, float value) {
        float center = (param.min() + param.max()) * 0.5F;
        return Math.abs(center - value);
    }
}

package com.judicius.bcdimensions.compat.bwg;

import com.judicius.bcdimensions.BCDimensions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.potionstudios.biomeswevegone.config.configs.BWGWorldGenConfig;
import net.potionstudios.biomeswevegone.util.BWGUtil;
import net.potionstudios.biomeswevegone.world.level.levelgen.biome.BWGBiomes;
import net.potionstudios.biomeswevegone.world.level.levelgen.biome.selector.BWGBiomeSelectors;
import net.potionstudios.biomeswevegone.world.level.levelgen.biome.selector.TerraBlenderBiomeSelectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.TerrablenderOverworldBiomeBuilder;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BwgDimRegions {
    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|BwgDimRegions");

    private BwgDimRegions() {}

    public static void register() {
        //Regions.register(REGION_1);
        //Regions.register(REGION_2);
        //Regions.register(REGION_3);

        LOGGER.info("BC Dimensions: registered BWG mirror regions:");
        LOGGER.info(" - {} (id={})", REGION_1.getDebugId(), REGION_1.getId());
        LOGGER.info(" - {} (id={})", REGION_2.getDebugId(), REGION_2.getId());
        LOGGER.info(" - {} (id={})", REGION_3.getDebugId(), REGION_3.getId());
    }

    public static final BwgDimRegion REGION_1;
    public static final BwgDimRegion REGION_2;
    public static final BwgDimRegion REGION_3;

    static {
        BWGWorldGenConfig cfg = BWGWorldGenConfig.INSTANCE.get();
        int weight = cfg.regionWeight();

        LOGGER.info("BC Dimensions: BWG mirror regions using BWG regionWeight={}", weight);

        REGION_1 = new BwgDimRegion(
                new ResourceLocation(BCDimensions.MODID, "bwg_dim_region_1"),
                weight,
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.OCEANS_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.MIDDLE_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.MIDDLE_BIOMES_VARIANT_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PLATEAU_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PLATEAU_BIOMES_VARIANT_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.SHATTERED_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.BEACH_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PEAK_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PEAK_BIOMES_VARIANT_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.SLOPE_BIOMES_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SLOPE_BIOMES_VARIANT_TERRABLENDER.value()),
                Util.make(new IdentityHashMap<ResourceKey<Biome>, ResourceKey<Biome>>(), map -> {
                    map.put(Biomes.SWAMP, BWGBiomes.CYPRESS_SWAMPLANDS);
                    map.put(Biomes.MANGROVE_SWAMP, BWGBiomes.BAYOU);
                }),
                cfg.enabledBiomes()
        );

        REGION_2 = new BwgDimRegion(
                new ResourceLocation(BCDimensions.MODID, "bwg_dim_region_2"),
                weight,
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.OCEANS_2_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.MIDDLE_BIOMES_2_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.MIDDLE_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PLATEAU_BIOMES_2_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PLATEAU_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SHATTERED_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.BEACH_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PEAK_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PEAK_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SLOPE_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SLOPE_BIOMES_VARIANT_TERRABLENDER.value()),
                Util.make(new IdentityHashMap<ResourceKey<Biome>, ResourceKey<Biome>>(), map -> {
                    map.put(Biomes.SWAMP, BWGBiomes.WHITE_MANGROVE_MARSHES);
                    map.put(Biomes.MANGROVE_SWAMP, BWGBiomes.CYPRESS_WETLANDS);
                }),
                cfg.enabledBiomes()
        );

        REGION_3 = new BwgDimRegion(
                new ResourceLocation(BCDimensions.MODID, "bwg_dim_region_3"),
                weight,
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.OCEANS_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.MIDDLE_BIOMES_3_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.MIDDLE_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(BWGBiomeSelectors.PLATEAU_BIOMES_3_BWG.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PLATEAU_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SHATTERED_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.BEACH_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PEAK_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.PEAK_BIOMES_VARIANT_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SLOPE_BIOMES_TERRABLENDER.value()),
                BWGUtil._2DResourceKeyArrayTo2DList(TerraBlenderBiomeSelectors.SLOPE_BIOMES_VARIANT_TERRABLENDER.value()),
                Util.make(new IdentityHashMap<ResourceKey<Biome>, ResourceKey<Biome>>(), map -> {
                    map.put(Biomes.SWAMP, BWGBiomes.BAYOU);
                    map.put(Biomes.MANGROVE_SWAMP, BWGBiomes.CYPRESS_SWAMPLANDS);
                }),
                cfg.enabledBiomes()
        );
    }

    public static class BwgDimRegion extends Region {

        private final ExposedBuilder builder;
        private final Map<ResourceKey<Biome>, ResourceKey<Biome>> swapper;
        private final Map<ResourceKey<Biome>, Boolean> enabledMap;
        private final ResourceLocation id;

        public BwgDimRegion(ResourceLocation name,
                            int weight,
                            ResourceKey<Biome>[][] oceans,
                            ResourceKey<Biome>[][] middle,
                            ResourceKey<Biome>[][] middleVar,
                            ResourceKey<Biome>[][] plateau,
                            ResourceKey<Biome>[][] plateauVar,
                            ResourceKey<Biome>[][] shattered,
                            ResourceKey<Biome>[][] beach,
                            ResourceKey<Biome>[][] peak,
                            ResourceKey<Biome>[][] peakVar,
                            ResourceKey<Biome>[][] slope,
                            ResourceKey<Biome>[][] slopeVar,
                            Map<ResourceKey<Biome>, ResourceKey<Biome>> swapper,
                            Map<ResourceKey<Biome>, Boolean> enabledMap) {
            super(name, RegionType.OVERWORLD, weight);
            this.id = name;
            this.swapper = swapper;
            this.enabledMap = enabledMap;

            Predicate<ResourceKey<Biome>> valid = key -> key != null && key != Biomes.THE_VOID;

            oceans = filter(oceans, valid);
            middle = filter(middle, valid);
            middleVar = filter(middleVar, valid);
            plateau = filter(plateau, valid);
            plateauVar = filter(plateauVar, valid);
            shattered = filter(shattered, valid);
            beach = filter(beach, valid);
            peak = filter(peak, valid);
            peakVar = filter(peakVar, valid);
            slope = filter(slope, valid);
            slopeVar = filter(slopeVar, valid);

            this.builder = new ExposedBuilder(
                    oceans, middle, middleVar,
                    plateau, plateauVar,
                    shattered, beach,
                    peak, peakVar,
                    slope, slopeVar
            );

            LOGGER.info("BWG Dim Region constructed: id={} weight={}", this.id, weight);
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public String getDebugId() {
            return this.id.toString();
        }

        @Override
        public void addBiomes(Registry<Biome> registry,
                              Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

            final int[] total = {0};
            final int[] swapped = {0};
            final int[] disabled = {0};
            final int[] deferredMissing = {0};
            final int[] deferredDisabled = {0};

            LOGGER.info("BWG Dim Region {}: begin addBiomes", this.id);

            this.builder.addPublicBiomes(pair -> {
                Climate.ParameterPoint point = pair.getFirst();
                ResourceKey<Biome> biomeKey = pair.getSecond();

                total[0]++;

                // Begin Registry Check
                //Check missing from registry -> placeholder
                if (!registry.containsKey(biomeKey)) {
                    if (deferredMissing[0] < 20) {
                        LOGGER.warn("BWG Dim Region {}: missing biome in registry: {}",
                                this.id, biomeKey.location());
                    }
                    deferredMissing[0]++;
                    mapper.accept(Pair.of(point, Region.DEFERRED_PLACEHOLDER));
                    return;
                }

                //Disabled by BWG Config -> placeholder
                boolean enabled = this.enabledMap.getOrDefault(biomeKey, true);
                if (!enabled) {
                    if (deferredDisabled[0] < 20) {
                        LOGGER.warn("BWG Dim Region {}: biome disabled by config: {}",
                                this.id, biomeKey.location());
                    }
                    disabled[0]++;
                    deferredDisabled[0]++;
                    mapper.accept(Pair.of(point, Region.DEFERRED_PLACEHOLDER));
                    return;
                }

                // Normal or swapped
                ResourceKey<Biome> finalKey = this.swapper.getOrDefault(biomeKey, biomeKey);
                if (finalKey != biomeKey) {
                    swapped[0]++;
                }
                mapper.accept(Pair.of(point, finalKey));
            });

            LOGGER.info(
                    "BWG Dim Region {}: mapped={} swapped={} disabled(deferred)={} missing(deferred)={}",
                    this.id, total[0], swapped[0], deferredDisabled[0], deferredMissing[0]
            );

            // Extra compat biomes (Ars, Hexerei, etc.)
            ExtraBiomeCompat.addExtraBiomes(registry, mapper);
            LOGGER.info("BWG Dim Region {}: ExtraBiomeCompat finished.", this.id);
        }

        private static ResourceKey<Biome>[][] filter(ResourceKey<Biome>[][] in, Predicate<ResourceKey<Biome>> keep) {
            for (int i = 0; i < in.length; i++) {
                for (int j = 0; j < in[i].length; j++) {
                    ResourceKey<Biome> key = in[i][j];
                    if (key != null && !keep.test(key)) {
                        in[i][j] = null;
                    }
                }
            }
            return in;
        }
    }

    /**
     * tiny subclass to expose the protected addBiomes(...) from TB
     */
    private static class ExposedBuilder extends TerrablenderOverworldBiomeBuilder {
        public ExposedBuilder(ResourceKey<Biome>[][] oceans,
                              ResourceKey<Biome>[][] middle,
                              ResourceKey<Biome>[][] middleVar,
                              ResourceKey<Biome>[][] plateau,
                              ResourceKey<Biome>[][] plateauVar,
                              ResourceKey<Biome>[][] shattered,
                              ResourceKey<Biome>[][] beach,
                              ResourceKey<Biome>[][] peak,
                              ResourceKey<Biome>[][] peakVar,
                              ResourceKey<Biome>[][] slope,
                              ResourceKey<Biome>[][] slopeVar) {
            super(oceans, middle, middleVar,
                    plateau, plateauVar,
                    shattered, beach,
                    peak, peakVar,
                    slope, slopeVar);
        }

        public void addPublicBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
            // call the protected method from inside the subclass
            this.addBiomes(consumer);
        }
    }
}

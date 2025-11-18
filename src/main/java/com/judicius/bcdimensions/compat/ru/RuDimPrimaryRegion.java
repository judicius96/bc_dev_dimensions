package com.judicius.bcdimensions.compat.ru;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.regions_unexplored.world.level.biome.RegionPrimaryBiomeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RuDimPrimaryRegion extends Region {

    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|RuDimPrimary");

    public RuDimPrimaryRegion() {
        // weight 5 as before
        super(new ResourceLocation("bc_dimensions", "ru_dim_primary"), RegionType.OVERWORLD, 5);
        LOGGER.info("RU Dim Primary Region constructed: id=bc_dimensions:ru_dim_primary weight=5");
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        AtomicInteger total = new AtomicInteger();
        AtomicInteger missing = new AtomicInteger();

        LOGGER.info("RU Dim Primary Region: begin addBiomes");

        // Wrap the mapper so we can collect some stats
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> countingMapper = pair -> {
            total.incrementAndGet();
            ResourceKey<Biome> biomeKey = pair.getSecond();

            if (!registry.containsKey(biomeKey)) {
                missing.incrementAndGet();
            }

            mapper.accept(pair);
        };

        // Delegate to RU's builder
        new RegionPrimaryBiomeBuilder().addBiomes(countingMapper);

        LOGGER.info(
                "RU Dim Primary Region: mapped={} missingInRegistry={}",
                total.get(), missing.get()
        );
    }
}

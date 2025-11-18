package com.judicius.bcdimensions.compat.ru;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.regions_unexplored.world.level.biome.RegionSecondaryBiomeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RuDimSecondaryRegion extends Region {

    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions|RuDimSecondary");

    public RuDimSecondaryRegion() {
        // weight 2 as before
        super(new ResourceLocation("bc_dimensions", "ru_dim_secondary"), RegionType.OVERWORLD, 2);
        LOGGER.info("RU Dim Secondary Region constructed: id=bc_dimensions:ru_dim_secondary weight=2");
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        AtomicInteger total = new AtomicInteger();
        AtomicInteger missing = new AtomicInteger();

        LOGGER.info("RU Dim Secondary Region: begin addBiomes");

        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> countingMapper = pair -> {
            total.incrementAndGet();
            ResourceKey<Biome> biomeKey = pair.getSecond();

            if (!registry.containsKey(biomeKey)) {
                missing.incrementAndGet();
            }

            mapper.accept(pair);
        };

        new RegionSecondaryBiomeBuilder().addBiomes(countingMapper);

        LOGGER.info(
                "RU Dim Secondary Region: mapped={} missingInRegistry={}",
                total.get(), missing.get()
        );
    }
}

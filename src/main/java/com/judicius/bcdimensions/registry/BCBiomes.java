package com.judicius.bcdimensions.registry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BCBiomes {

    private static final Logger LOGGER = LogManager.getLogger();

    // -------------------------------------------------------------------------
    // Registrations
    // -------------------------------------------------------------------------

    public static final RegistryObject<Biome> MOSSY_CAVERNS =
            BCRegistry.BIOMES.register("mossy_caverns", BCBiomes::mossyCaverns);

    public static final RegistryObject<Biome> PALE_MOSS_GROWTH =
            BCRegistry.BIOMES.register("pale_moss_growth", BCBiomes::paleMossGrowth);

    // -------------------------------------------------------------------------
    // Mossy Caverns
    // -------------------------------------------------------------------------

    private static Biome mossyCaverns() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

        // Frogs — temperate, always present
        spawns.addSpawn(MobCategory.CREATURE,
                new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 4));

        // HMAG mobs — conditional
        if (ModList.get().isLoaded("hmag")) {
            addModdedSpawn(spawns, MobCategory.MONSTER, "hmag:alraune",  5,  1, 1);
            addModdedSpawn(spawns, MobCategory.MONSTER, "hmag:glaryad",  15, 1, 2);
        }

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .skyColor(0)
                .fogColor(2696968)
                .waterColor(4159204)
                .waterFogColor(329011)
                .ambientParticle(new AmbientParticleSettings(
                        ParticleTypes.SPORE_BLOSSOM_AIR, 0.005f))
                .ambientMoodSound(new AmbientMoodSettings(
                        SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0));

        return new BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.9f)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .temperatureAdjustment(TemperatureModifier.NONE)
                .build();
    }

    // -------------------------------------------------------------------------
    // Pale Moss Growth — stub, full design pending
    // -------------------------------------------------------------------------

    private static Biome paleMossGrowth() {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .skyColor(0)
                .fogColor(2696968) // placeholder — tune in game
                .waterColor(4159204)
                .waterFogColor(329011)
                .ambientMoodSound(new AmbientMoodSettings(
                        SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0));

        return new BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.5f)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .temperatureAdjustment(TemperatureModifier.NONE)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a modded entity spawn by string resource location.
     * Logs a warning and skips silently if the entity type is not registered.
     */
    @SuppressWarnings("unchecked")
    private static void addModdedSpawn(MobSpawnSettings.Builder builder, MobCategory category,
                                       String entityId, int weight, int minCount, int maxCount) {
        ResourceLocation rl = new ResourceLocation(entityId);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type != null) {
            builder.addSpawn(category, new MobSpawnSettings.SpawnerData(
                    (EntityType<? extends net.minecraft.world.entity.Mob>) type,
                    weight, minCount, maxCount));
        } else {
            LOGGER.warn("[BCDimensions] BCBiomes: Entity type '{}' not found — skipping spawner.", entityId);
        }
    }

    // Force static field initialization at mod load time
    public static void init() {}

    private BCBiomes() {}
}
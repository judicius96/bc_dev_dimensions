package com.judicius.bcdimensions.registry;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.item.SpectralKeyItem;
import com.judicius.bcdimensions.palette.PaletteBrush;
import com.judicius.bcdimensions.portals.MiningPortalBlock;
import com.judicius.bcdimensions.portals.SandPortalBlock;
import com.judicius.bcdimensions.worldgen.MushroomCavernsChunkGenerator;
import com.judicius.bcdimensions.worldgen.SpectralChunkGenerator;
import com.judicius.bcdimensions.worldgen.features.WaterBowlFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BCRegistry {

    /* ===================== */
    /*  Deferred Registers   */
    /* ===================== */

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BCDimensions.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BCDimensions.MODID);

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, BCDimensions.MODID);

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, BCDimensions.MODID);

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, BCDimensions.MODID);

    /* ===================== */
    /*        Blocks         */
    /* ===================== */

    public static final RegistryObject<Block> SAND_PORTAL =
            BLOCKS.register("sand_portal",
                    () -> new SandPortalBlock(BlockBehaviour.Properties.of()
                            .noCollission()
                            .noOcclusion()
                            .strength(-1.0F)
                            .lightLevel(s -> 11)));

    public static final RegistryObject<Block> GRANITE_PORTAL =
            BLOCKS.register("granite_portal",
                    () -> new MiningPortalBlock(BlockBehaviour.Properties.of()
                            .noCollission()
                            .noOcclusion()
                            .strength(-1.0F)
                            .lightLevel(s -> 11)));

    public static final RegistryObject<Block> SPECTRAL_BLOCK =
            BLOCKS.register("spectral_block",
                    () -> new com.judicius.bcdimensions.block.SpectralBlock(BlockBehaviour.Properties.of()
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.STONE)));

    public static final RegistryObject<Block> PALETTE_FLOOR =
            BLOCKS.register("palette_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(-1.0F, 3600000.0F)
                            .lightLevel(state -> 3)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, type) -> false)));

    public static final RegistryObject<Block> INVERTED_MYCELIUM =
            BLOCKS.register("inverted_mycelium",
                    () -> new MyceliumBlock(
                            BlockBehaviour.Properties.copy(Blocks.MYCELIUM)
                    ));

    public static final RegistryObject<Item> INVERTED_MYCELIUM_ITEM =
            ITEMS.register("inverted_mycelium",
                    () -> new BlockItem(INVERTED_MYCELIUM.get(), new Item.Properties()));

    public static final RegistryObject<Block> RADIANT_LICHEN =
            BLOCKS.register("radiant_lichen",
                    () -> new GlowLichenBlock(
                            BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)
                    ));

    public static final RegistryObject<Item> RADIANT_LICHEN_ITEM =
            ITEMS.register("radiant_lichen",
                    () -> new BlockItem(RADIANT_LICHEN.get(), new Item.Properties()));

    /* ===================== */
    /*         Items         */
    /* ===================== */

    public static final RegistryObject<Item> PALETTE_BRUSH =
            ITEMS.register("palette_brush", PaletteBrush::new);

    public static final RegistryObject<Item> SPECTRAL_KEY =
            ITEMS.register("spectral_key", () -> new SpectralKeyItem(new Item.Properties().stacksTo(1)));

    /* ===================== */
    /*       Features        */
    /* ===================== */

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WATER_BOWL =
            FEATURES.register("water_bowl",
                    () -> new WaterBowlFeature(NoneFeatureConfiguration.CODEC));

    /* ===================== */
    /*   Chunk Generators    */
    /* ===================== */

    public static final RegistryObject<Codec<? extends ChunkGenerator>> MUSHROOM_CAVERNS_CODEC =
            CHUNK_GENERATORS.register("mushroom_caverns",
                    () -> MushroomCavernsChunkGenerator.CODEC);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> SPECTRAL_GENERATOR_CODEC =
            CHUNK_GENERATORS.register("spectral_generator",
                    () -> SpectralChunkGenerator.CODEC);

    /* ===================== */
    /*    Registration Hook  */
    /* ===================== */

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        CHUNK_GENERATORS.register(bus);
        BIOME_SOURCES.register(bus);
        FEATURES.register(bus);
    }

    private BCRegistry() {}
}
package com.judicius.bcdimensions.registry;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.block.InvertedCaveMyceliumBlock;
import com.judicius.bcdimensions.item.SpectralKeyItem;
import com.judicius.bcdimensions.palette.PaletteBrush;
import com.judicius.bcdimensions.portals.BCPoiTypes;
import com.judicius.bcdimensions.portals.MiningPortalBlock;
import com.judicius.bcdimensions.portals.SandPortalBlock;
import com.judicius.bcdimensions.worldgen.MiningCaveCarver;
import com.judicius.bcdimensions.worldgen.MushroomCavernsChunkGenerator;
import com.judicius.bcdimensions.worldgen.SpectralChunkGenerator;
import com.judicius.bcdimensions.worldgen.features.BWGMushroomFeature;
import com.judicius.bcdimensions.worldgen.features.CaveMushroomFeature;
import com.judicius.bcdimensions.worldgen.features.GlowShroomFeature;
import com.judicius.bcdimensions.worldgen.features.MiningSeamConfig;
import com.judicius.bcdimensions.worldgen.features.MiningSeamFeature;
import com.judicius.bcdimensions.worldgen.features.MiningVeinConfig;
import com.judicius.bcdimensions.worldgen.features.MiningVeinFeature;
import com.judicius.bcdimensions.worldgen.features.MossyCobbleFormationFeature;
import com.judicius.bcdimensions.worldgen.features.WaterBowlFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
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

    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, BCDimensions.MODID);

    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(Registries.BIOME, BCDimensions.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BCDimensions.MODID);

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

    public static final RegistryObject<Block> CAVE_MYCELIUM =
            BLOCKS.register("cave_mycelium",
                    () -> new MyceliumBlock(
                            BlockBehaviour.Properties.copy(Blocks.MYCELIUM)
                                    .lightLevel(state -> 8)
                    ));

    public static final RegistryObject<Item> CAVE_MYCELIUM_ITEM =
            ITEMS.register("cave_mycelium",
                    () -> new BlockItem(CAVE_MYCELIUM.get(), new Item.Properties()));

    public static final RegistryObject<Block> INVERTED_CAVE_MYCELIUM =
            BLOCKS.register("inverted_cave_mycelium",
                    () -> new InvertedCaveMyceliumBlock(
                            BlockBehaviour.Properties.copy(Blocks.MYCELIUM)
                                    .randomTicks()
                                    .lightLevel(state -> 8)
                    ));

    public static final RegistryObject<Item> INVERTED_CAVE_MYCELIUM_ITEM =
            ITEMS.register("inverted_cave_mycelium",
                    () -> new BlockItem(INVERTED_CAVE_MYCELIUM.get(), new Item.Properties()));

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

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CAVE_MUSHROOM =
            FEATURES.register("cave_mushroom",
                    () -> new CaveMushroomFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BWG_MUSHROOM =
            FEATURES.register("bwg_mushroom",
                    () -> new BWGMushroomFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GLOW_SHROOM =
            FEATURES.register("glow_shroom",
                    () -> new GlowShroomFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MOSSY_COBBLE_FORMATION =
            FEATURES.register("mossy_cobble_formation",
                    () -> new MossyCobbleFormationFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<MiningVeinConfig>> MINING_VEIN =
            FEATURES.register("mining_vein",
                    () -> new MiningVeinFeature(MiningVeinConfig.CODEC));

    public static final RegistryObject<Feature<MiningSeamConfig>> MINING_SEAM =
            FEATURES.register("mining_seam",
                    () -> new MiningSeamFeature(MiningSeamConfig.CODEC));

    /* ===================== */
    /*       Carvers         */
    /* ===================== */

    public static final RegistryObject<WorldCarver<?>> MINING_CAVE_CARVER =
            CARVERS.register("mining_cave",
                    () -> new MiningCaveCarver(CaveCarverConfiguration.CODEC));

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
    /*     Creative Tab      */
    /* ===================== */

    public static final RegistryObject<CreativeModeTab> BC_DIMENSIONS_TAB =
            CREATIVE_TABS.register("bc_dimensions_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.bc_dimensions"))
                            .icon(() -> new ItemStack(SPECTRAL_KEY.get()))
                            .displayItems((params, output) -> {
                                output.accept(SPECTRAL_KEY.get());
                                output.accept(PALETTE_BRUSH.get());
                                output.accept(CAVE_MYCELIUM_ITEM.get());
                                output.accept(INVERTED_CAVE_MYCELIUM_ITEM.get());
                                output.accept(RADIANT_LICHEN_ITEM.get());
                            })
                            .build());

    /* ===================== */
    /*    Registration Hook  */
    /* ===================== */

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        CHUNK_GENERATORS.register(bus);
        BIOME_SOURCES.register(bus);
        FEATURES.register(bus);
        CARVERS.register(bus);
        BIOMES.register(bus);
        CREATIVE_TABS.register(bus);
        BCPoiTypes.register(bus);
    }

    private BCRegistry() {}
}
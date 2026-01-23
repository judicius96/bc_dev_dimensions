package com.judicius.bcdimensions.registry;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.item.SpecterKeyItem;
import com.judicius.bcdimensions.portals.MiningPortalBlock;
import com.judicius.bcdimensions.portals.SandPortalBlock;
import com.judicius.bcdimensions.worldgen.MushroomCavernsChunkGenerator;
import com.judicius.bcdimensions.worldgen.SpecterChunkGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCRegistry {
    // Blocks
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BCDimensions.MODID);

    public static final RegistryObject<Block> SAND_PORTAL = BLOCKS.register("sand_portal",
            () -> new SandPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11)));

    public static final RegistryObject<Block> GRANITE_PORTAL = BLOCKS.register("granite_portal",
            () -> new MiningPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11)));

    public static final RegistryObject<Block> PALETTE_FLOOR = BLOCKS.register("palette_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .lightLevel(state -> 3)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
            )
    );

    // Items
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BCDimensions.MODID);

    public static final RegistryObject<Item> SPECTER_KEY = ITEMS.register("specter_key",
            () -> new SpecterKeyItem(new Item.Properties().stacksTo(1)));

    // Chunk Generator Codecs
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, BCDimensions.MODID);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> MUSHROOM_CAVERNS_CODEC =
            CHUNK_GENERATORS.register("mushroom_caverns",
                    () -> MushroomCavernsChunkGenerator.CODEC);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> SPECTER_GENERATOR_CODEC =
            CHUNK_GENERATORS.register("specter_generator",
                    () -> SpecterChunkGenerator.CODEC);

    public static final RegistryObject<Block> SPECTER_BLOCK = BLOCKS.register("specter_block",
            () -> new com.judicius.bcdimensions.block.SpecterBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE)));

    // Biome Source Codecs
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, BCDimensions.MODID);

}
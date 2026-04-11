package com.judicius.bcdimensions.worldgen;

import com.judicius.bcdimensions.registry.BCRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MushroomCavernsChunkGenerator extends NoiseBasedChunkGenerator {

    // Biomes that manage their own floor/ceiling — skip our replacement for these
    private static final Set<ResourceKey<Biome>> EXCLUDED_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "dripstone_caves")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", "lush_caves")),
            ResourceKey.create(Registries.BIOME, new ResourceLocation("regions_unexplored", "prismachasm"))
    );

    // Biomes that get moss floor/ceiling treatment
    private static final Set<ResourceKey<Biome>> MOSSY_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, new ResourceLocation("bc_dimensions", "mossy_cavern"))
    );

    // Biomes that get pale moss floor/ceiling treatment
    private static final Set<ResourceKey<Biome>> PALE_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, new ResourceLocation("bc_dimensions", "pale_moss_growth"))
    );

    public static final Codec<MushroomCavernsChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.generatorSettings)
            ).apply(instance, MushroomCavernsChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> generatorSettings;

    private PerlinSimplexNoise floorNoise;
    private PerlinSimplexNoise ceilingNoise;
    private PerlinSimplexNoise floorBlockNoise;
    private PerlinSimplexNoise ceilingBlockNoise;

    // Block pools — built once at construction, mod-aware
    private final List<Block> floorBlocks;
    private final List<Block> ceilingBlocks;
    private final List<Block> sublayerBlocks;

    // Pale moss block — resolved at construction, falls back to stone if VanillaBackport absent
    private final Block paleMossBlock;

    public MushroomCavernsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.generatorSettings = settings;

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(1234L));
        floorNoise        = new PerlinSimplexNoise(random, List.of(0));
        ceilingNoise      = new PerlinSimplexNoise(random, List.of(1));
        floorBlockNoise   = new PerlinSimplexNoise(random, List.of(2));
        ceilingBlockNoise = new PerlinSimplexNoise(random, List.of(3));

        this.floorBlocks    = buildFloorBlocks();
        this.ceilingBlocks  = buildCeilingBlocks();
        this.sublayerBlocks = buildSublayerBlocks();
        this.paleMossBlock  = buildPaleMossBlock();
    }

    // -------------------------------------------------------------------------
    // Block pool builders — mod-aware
    // -------------------------------------------------------------------------

    private static Block getPeatAlternative() {
        if (ModList.get().isLoaded("biomeswevegone")) {
            Block peat = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("biomeswevegone", "peat"));
            if (peat != null && peat != Blocks.AIR) return peat;
        }
        if (ModList.get().isLoaded("regions_unexplored")) {
            Block peatDirt = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("regions_unexplored", "peat_dirt"));
            if (peatDirt != null && peatDirt != Blocks.AIR) return peatDirt;
        }
        return Blocks.PODZOL;
    }

    private static Block buildPaleMossBlock() {
        Block block = ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation("minecraft", "pale_moss_block"));
        return (block != null && block != Blocks.AIR) ? block : Blocks.STONE;
    }

    private static List<Block> buildFloorBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(Blocks.MYCELIUM);
        blocks.add(getPeatAlternative());
        blocks.add(Blocks.MUD);
        return blocks;
    }

    private static List<Block> buildCeilingBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(BCRegistry.INVERTED_CAVE_MYCELIUM.get());
        blocks.add(Blocks.ROOTED_DIRT);
        blocks.add(Blocks.DIRT);
        if (ModList.get().isLoaded("decorative_blocks")) {
            Block rockyDirt = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("decorative_blocks", "rocky_dirt"));
            if (rockyDirt != null && rockyDirt != Blocks.AIR) {
                blocks.add(rockyDirt);
            }
        }
        return blocks;
    }

    private static List<Block> buildSublayerBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(Blocks.DIRT);
        blocks.add(Blocks.COARSE_DIRT);
        blocks.add(Blocks.ROOTED_DIRT);
        if (ModList.get().isLoaded("decorative_blocks")) {
            Block rockyDirt = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("decorative_blocks", "rocky_dirt"));
            if (rockyDirt != null && rockyDirt != Blocks.AIR) {
                blocks.add(rockyDirt);
            }
        }
        return blocks;
    }

    private BlockState getSublayerBlock(int x, int y, int z) {
        int hash = (x * 1619 + y * 31337 + z * 6971) & Integer.MAX_VALUE;
        return sublayerBlocks.get(hash % sublayerBlocks.size()).defaultBlockState();
    }

    private BlockState getFloorSurfaceBlock(int x, int z) {
        double primary   = floorBlockNoise.getValue(x * 0.005, z * 0.005, false);
        double secondary = ceilingBlockNoise.getValue(x * 0.01, z * 0.01, false);

        if (primary > -0.4) return Blocks.DIRT.defaultBlockState();
        if (secondary > 0.2) return Blocks.MUD.defaultBlockState();
        return getPeatAlternative().defaultBlockState();
    }

    private BlockState getCeilingSurfaceBlock(int x, int z) {
        double primary   = ceilingBlockNoise.getValue(x * 0.005, z * 0.005, false);
        double secondary = floorBlockNoise.getValue(x * 0.012, z * 0.012, false);

        if (secondary > 0.6) return BCRegistry.INVERTED_CAVE_MYCELIUM.get().defaultBlockState();

        if (primary < -0.2) {
            if (!ceilingBlocks.isEmpty() && ceilingBlocks.size() > 2) {
                return ceilingBlocks.get(ceilingBlocks.size() - 1).defaultBlockState();
            }
            return Blocks.ROOTED_DIRT.defaultBlockState();
        }

        return Blocks.ROOTED_DIRT.defaultBlockState();
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // Surface building
    // -------------------------------------------------------------------------

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures,
                             RandomState randomState, ChunkAccess chunk) {

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        final int minBuildY        = region.getMinBuildHeight();
        final int maxBuildY        = region.getMaxBuildHeight();
        final int topBedrockStartY = maxBuildY - 3;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                int floorY   = getFloorHeight(worldX, worldZ);
                int ceilingY = getCeilingHeight(worldX, worldZ);

                ceilingY = Math.min(ceilingY, topBedrockStartY - 3);

                // Determine biome for this column
                BlockPos samplePos = new BlockPos(worldX, floorY, worldZ);
                Holder<Biome> biomeHolder = region.getBiome(samplePos);

                boolean excluded = biomeHolder.unwrapKey()
                        .map(EXCLUDED_BIOMES::contains)
                        .orElse(false);

                boolean mossy = biomeHolder.unwrapKey()
                        .map(MOSSY_BIOMES::contains)
                        .orElse(false);

                boolean pale = biomeHolder.unwrapKey()
                        .map(PALE_BIOMES::contains)
                        .orElse(false);

                for (int y = minBuildY; y < maxBuildY; y++) {
                    pos.set(minX + x, y, minZ + z);
                    BlockState state;
                    if (excluded) {
                        state = getVanillaBlockStateForPosition(y, minBuildY, topBedrockStartY, floorY, ceilingY);
                    } else if (mossy) {
                        state = getMossyBlockStateForPosition(y, floorY, ceilingY, minBuildY, topBedrockStartY, worldX, worldZ);
                    } else if (pale) {
                        state = getPaleMossyBlockStateForPosition(y, floorY, ceilingY, minBuildY, topBedrockStartY);
                    } else {
                        state = getBlockStateForPosition(y, floorY, ceilingY, minBuildY, topBedrockStartY, worldX, worldZ);
                    }
                    chunk.setBlockState(pos, state, false);
                }

                // Hanging roots — 25% chance below rooted dirt on ceiling
                // Skipped for mossy and pale biomes — hanging vegetation handled by placed features
                if (!excluded && !mossy && !pale) {
                    BlockPos ceilingPos = new BlockPos(worldX, ceilingY, worldZ);
                    if (chunk.getBlockState(ceilingPos).is(Blocks.ROOTED_DIRT)) {
                        BlockPos rootsPos = ceilingPos.below();
                        if (chunk.getBlockState(rootsPos).isAir() &&
                                region.getRandom().nextFloat() < 0.25f) {
                            chunk.setBlockState(rootsPos, Blocks.HANGING_ROOTS.defaultBlockState(), false);
                        }
                    }
                }
            }
        }

        // DO NOT call super.buildSurface — vanilla would overwrite our cave air with terrain
    }

    private BlockState getBlockStateForPosition(
            int y, int floorY, int ceilingY,
            int minBuildY, int topBedrockStartY,
            int worldX, int worldZ
    ) {
        if (y <= minBuildY + 4) return Blocks.BEDROCK.defaultBlockState();
        if (y >= topBedrockStartY) return Blocks.BEDROCK.defaultBlockState();
        if (y <= 32) return Blocks.DEEPSLATE.defaultBlockState();
        if (y < floorY - 1 && y >= floorY - 6) return getSublayerBlock(worldX, y, worldZ);
        if (y < floorY - 6) return Blocks.STONE.defaultBlockState();
        if (y == floorY - 1) return getFloorSurfaceBlock(worldX, worldZ);
        if (y >= floorY && y < ceilingY) return Blocks.AIR.defaultBlockState();
        if (y == ceilingY) return getCeilingSurfaceBlock(worldX, worldZ);
        if (y > ceilingY && y < topBedrockStartY) return Blocks.STONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private BlockState getMossyBlockStateForPosition(
            int y, int floorY, int ceilingY,
            int minBuildY, int topBedrockStartY,
            int worldX, int worldZ
    ) {
        if (y <= minBuildY + 4) return Blocks.BEDROCK.defaultBlockState();
        if (y >= topBedrockStartY) return Blocks.BEDROCK.defaultBlockState();
        if (y <= 32) return Blocks.DEEPSLATE.defaultBlockState();
        if (y < floorY - 1 && y >= floorY - 6) return getSublayerBlock(worldX, y, worldZ);
        if (y < floorY - 6) return Blocks.STONE.defaultBlockState();
        if (y == floorY - 1 || y == floorY - 2 || y == floorY - 3) return Blocks.MOSS_BLOCK.defaultBlockState();
        if (y >= floorY && y < ceilingY) return Blocks.AIR.defaultBlockState();
        if (y == ceilingY) return Blocks.MOSS_BLOCK.defaultBlockState();
        if (y > ceilingY && y < topBedrockStartY) return Blocks.STONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private BlockState getPaleMossyBlockStateForPosition(
            int y, int floorY, int ceilingY,
            int minBuildY, int topBedrockStartY
    ) {
        BlockState paleMoss = paleMossBlock.defaultBlockState();
        if (y <= minBuildY + 4) return Blocks.BEDROCK.defaultBlockState();
        if (y >= topBedrockStartY) return Blocks.BEDROCK.defaultBlockState();
        if (y <= 32) return Blocks.DEEPSLATE.defaultBlockState();
        if (y < floorY - 6) return Blocks.STONE.defaultBlockState();
        if (y == floorY - 1 || y == floorY - 2 || y == floorY - 3) return paleMoss;
        if (y >= floorY && y < ceilingY) return Blocks.AIR.defaultBlockState();
        if (y == ceilingY) return paleMoss;
        if (y > ceilingY && y < topBedrockStartY) return Blocks.STONE.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    // -------------------------------------------------------------------------
    // Height calculations
    // -------------------------------------------------------------------------

    private int getFloorHeight(int x, int z) {
        double noise              = floorNoise.getValue(x * 0.01, z * 0.01, false);
        double largeFeaturesNoise = floorNoise.getValue(x * 0.005, z * 0.005, false);

        int extraHeight = largeFeaturesNoise > 0.5
                ? (int)((largeFeaturesNoise - 0.5) * 20)
                : 0;

        return 62 + (int)(noise * 8) + extraHeight;
    }

    private int getCeilingHeight(int x, int z) {
        double noise              = ceilingNoise.getValue(x * 0.01, z * 0.01, false);
        double largeFeaturesNoise = floorNoise.getValue(x * 0.005, z * 0.005, false);

        int extraDrop = largeFeaturesNoise > 0.5
                ? (int)((largeFeaturesNoise - 0.5) * -20)
                : 0;

        return 100 + (int)(noise * 8) + extraDrop;
    }

    private BlockState getVanillaBlockStateForPosition(int y, int minBuildY, int topBedrockStartY, int floorY, int ceilingY) {
        if (y <= minBuildY + 4) return Blocks.BEDROCK.defaultBlockState();
        if (y >= topBedrockStartY) return Blocks.BEDROCK.defaultBlockState();
        if (y <= 32) return Blocks.DEEPSLATE.defaultBlockState();
        if (y >= floorY && y < ceilingY) return Blocks.AIR.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    // -------------------------------------------------------------------------
    // Feature decoration and carvers
    // -------------------------------------------------------------------------

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk,
                                     StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step) {
        if (step == GenerationStep.Carving.AIR) {
            super.applyCarvers(region, seed, random, biomeManager, structureManager, chunk, step);
        }
    }
}
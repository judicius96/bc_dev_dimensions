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
    private PerlinSimplexNoise caveNoise;

    // Block pools — built once at construction, mod-aware
    private final List<Block> floorBlocks;
    private final List<Block> ceilingBlocks;
    private final List<Block> sublayerBlocks;

    public MushroomCavernsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.generatorSettings = settings;

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(1234L));
        floorNoise        = new PerlinSimplexNoise(random, List.of(0));
        ceilingNoise      = new PerlinSimplexNoise(random, List.of(1));
        floorBlockNoise   = new PerlinSimplexNoise(random, List.of(2));
        ceilingBlockNoise = new PerlinSimplexNoise(random, List.of(3));
        caveNoise         = new PerlinSimplexNoise(random, List.of(4));

        this.floorBlocks    = buildFloorBlocks();
        this.ceilingBlocks  = buildCeilingBlocks();
        this.sublayerBlocks = buildSublayerBlocks();
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

    private static List<Block> buildFloorBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(Blocks.MYCELIUM);
        blocks.add(getPeatAlternative());
        blocks.add(Blocks.MUD);
        return blocks;
    }

    private static List<Block> buildCeilingBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(BCRegistry.INVERTED_MYCELIUM.get());
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

        if (primary > -0.4) return Blocks.MYCELIUM.defaultBlockState();
        if (secondary > 0.2) return Blocks.MUD.defaultBlockState();
        return getPeatAlternative().defaultBlockState();
    }

    private BlockState getCeilingSurfaceBlock(int x, int z) {
        double primary   = ceilingBlockNoise.getValue(x * 0.005, z * 0.005, false);
        double secondary = floorBlockNoise.getValue(x * 0.012, z * 0.012, false);

        // Inverted mycelium — independent secondary patches (~20%)
        if (secondary > 0.6) return BCRegistry.INVERTED_MYCELIUM.get().defaultBlockState();

        // Rocky dirt if available, otherwise rooted dirt absorbs its share
        if (primary < -0.2) {
            if (!ceilingBlocks.isEmpty() && ceilingBlocks.size() > 2) {
                // rocky dirt is index 3 if decorative_blocks is loaded
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

                // Check if this column's biome is excluded from our replacement
                BlockPos samplePos = new BlockPos(worldX, floorY, worldZ);
                Holder<Biome> biomeHolder = region.getBiome(samplePos);
                boolean excluded = biomeHolder.unwrapKey()
                        .map(EXCLUDED_BIOMES::contains)
                        .orElse(false);

                for (int y = minBuildY; y < maxBuildY; y++) {
                    pos.set(minX + x, y, minZ + z);
                    BlockState state;
                    if (excluded) {
                        // Let vanilla/biome handle this column
                        state = getVanillaBlockStateForPosition(y, minBuildY, topBedrockStartY);
                    } else {
                        state = getBlockStateForPosition(
                                y, floorY, ceilingY, minBuildY, topBedrockStartY,
                                worldX, worldZ
                        );
                    }
                    chunk.setBlockState(pos, state, false);
                }

                // Hanging roots — 25% chance below rooted dirt on ceiling
                if (!excluded) {
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

        super.buildSurface(region, structures, randomState, chunk);
    }

    private BlockState getBlockStateForPosition(
            int y, int floorY, int ceilingY,
            int minBuildY, int topBedrockStartY,
            int worldX, int worldZ
    ) {
        // Bedrock floor
        if (y <= minBuildY + 4) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        // Bedrock ceiling cap
        if (y >= topBedrockStartY) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        // Deepslate layer
        if (y <= 32) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }

        // Below floor: sublayer 5 blocks deep, then solid stone
        if (y < floorY - 1 && y >= floorY - 6) {
            return getSublayerBlock(worldX, y, worldZ);
        }

        // Below sublayer: solid stone
        if (y < floorY - 6) {
            return Blocks.STONE.defaultBlockState();
        }

        // Floor surface block
        if (y == floorY - 1) {
            return getFloorSurfaceBlock(worldX, worldZ);
        }

        // Cave air
        if (y >= floorY && y < ceilingY) {
            return Blocks.AIR.defaultBlockState();
        }

        // Ceiling surface block
        if (y == ceilingY) {
            return getCeilingSurfaceBlock(worldX, worldZ);
        }

        // Above ceiling: solid stone up to bedrock cap
        if (y > ceilingY && y < topBedrockStartY) {
            return Blocks.STONE.defaultBlockState();
        }

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

    private BlockState getVanillaBlockStateForPosition(int y, int minBuildY, int topBedrockStartY) {
        if (y <= minBuildY + 4) return Blocks.BEDROCK.defaultBlockState();
        if (y >= topBedrockStartY) return Blocks.BEDROCK.defaultBlockState();
        if (y <= 32) return Blocks.DEEPSLATE.defaultBlockState();
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
        // Vanilla carvers disabled — our chunk generator defines the cave shape
    }
}
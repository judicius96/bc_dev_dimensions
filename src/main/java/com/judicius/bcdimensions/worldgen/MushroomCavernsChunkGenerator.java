package com.judicius.bcdimensions.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;

public class MushroomCavernsChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<MushroomCavernsChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.generatorSettings)
            ).apply(instance, MushroomCavernsChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> generatorSettings;
    private PerlinSimplexNoise floorNoise;
    private PerlinSimplexNoise ceilingNoise;

    public MushroomCavernsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.generatorSettings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {

        // Initialize noise if needed
        if (floorNoise == null) {
            WorldgenRandom random = new WorldgenRandom(new net.minecraft.world.level.levelgen.LegacyRandomSource(region.getSeed()));
            floorNoise = new PerlinSimplexNoise(random, List.of(0));
            ceilingNoise = new PerlinSimplexNoise(random, List.of(1));
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // Process each column in the chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                int floorY = getFloorHeight(worldX, worldZ);
                int ceilingY = getCeilingHeight(worldX, worldZ);

                for (int y = region.getMinBuildHeight(); y < region.getMaxBuildHeight(); y++) {
                    pos.set(minX + x, y, minZ + z);
                    BlockState state = getBlockStateForPosition(y, floorY, ceilingY);

                    if (state != null) {
                        chunk.setBlockState(pos, state, false);
                    }
                }
            }
        }
    }

    private BlockState getBlockStateForPosition(int y, int floorY, int ceilingY) {
        // Bedrock floor (Y 0-4)
        if (y <= 4) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        // Below floor: deepslate and stone
        if (y < floorY) {
            if (y < 32) {
                return Blocks.DEEPSLATE.defaultBlockState();
            }
            return Blocks.STONE.defaultBlockState();
        }

        // Floor surface layer (biome surface rules can convert later)
        if (y == floorY) {
            return Blocks.STONE.defaultBlockState();
        }

        // Cavern air
        if (y > floorY && y < ceilingY) {
            return Blocks.AIR.defaultBlockState();
        }

        // Above ceiling
        if (y >= ceilingY && y < 251) {
            return Blocks.STONE.defaultBlockState();
        }

        // Bedrock ceiling
        if (y >= 251) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        return Blocks.AIR.defaultBlockState();
    }

    private int getFloorHeight(int x, int z) {
        double noise = floorNoise.getValue(x * 0.02, z * 0.02, false);
        return 62 + (int) (noise * 8);
    }

    private int getCeilingHeight(int x, int z) {
        double noise = ceilingNoise.getValue(x * 0.02, z * 0.02, false);
        return 100 + (int) (noise * 8);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             net.minecraft.world.level.biome.BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk,
                             GenerationStep.Carving step) {
        super.applyCarvers(region, seed, random, biomeManager, structureManager, chunk, step);
    }
}

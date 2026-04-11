package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

public class MiningVeinConfig implements FeatureConfiguration {

    public static final Codec<MiningVeinConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.BLOCKS.getCodec().fieldOf("ore_block").forGetter(c -> c.oreBlock),
                    ForgeRegistries.BLOCKS.getCodec().fieldOf("raw_block").forGetter(c -> c.rawBlock),
                    Codec.INT.fieldOf("y_min").forGetter(c -> c.yMin),
                    Codec.INT.fieldOf("y_max").forGetter(c -> c.yMax)
            ).apply(instance, MiningVeinConfig::new)
    );

    public final Block oreBlock;
    public final Block rawBlock;
    public final int yMin;
    public final int yMax;

    public MiningVeinConfig(Block oreBlock, Block rawBlock, int yMin, int yMax) {
        this.oreBlock = oreBlock;
        this.rawBlock = rawBlock;
        this.yMin = yMin;
        this.yMax = yMax;
    }
}
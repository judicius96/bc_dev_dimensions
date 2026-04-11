package com.judicius.bcdimensions.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

public class MiningSeamConfig implements FeatureConfiguration {

    public static final Codec<MiningSeamConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.BLOCKS.getCodec().fieldOf("raw_block").forGetter(c -> c.rawBlock),
                    Codec.INT.fieldOf("y_min").forGetter(c -> c.yMin),
                    Codec.INT.fieldOf("y_max").forGetter(c -> c.yMax)
            ).apply(instance, MiningSeamConfig::new)
    );

    public final Block rawBlock;
    public final int yMin;
    public final int yMax;

    public MiningSeamConfig(Block rawBlock, int yMin, int yMax) {
        this.rawBlock = rawBlock;
        this.yMin = yMin;
        this.yMax = yMax;
    }
}

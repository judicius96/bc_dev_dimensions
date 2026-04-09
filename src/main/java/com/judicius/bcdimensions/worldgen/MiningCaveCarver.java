package com.judicius.bcdimensions.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

public class MiningCaveCarver extends CaveWorldCarver {

    // Main cavern Y band — carver skips this range
    private static final int CAVERN_FLOOR_Y = 56;   // floorY - 6 (below sublayer)
    private static final int CAVERN_CEILING_Y = 106; // ceilingY + 1

    // Breach chance — very rare, allows occasional connection to main cavern
    private static final float BREACH_CHANCE = 0.01f;

    public MiningCaveCarver(Codec<CaveCarverConfiguration> configCodec) {
        super(configCodec);
    }

    // Larger caves than vanilla — 3x radius like Undergarden
    @Override
    protected float getThickness(RandomSource random) {
        return super.getThickness(random) * 3.0F;
    }

    @Override
    protected boolean carveEllipsoid(CarvingContext context, CaveCarverConfiguration config,
                                     ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
                                     Aquifer aquifer, double x, double y, double z,
                                     double horizontalRadius, double verticalRadius,
                                     CarvingMask carvingMask, WorldCarver.CarveSkipChecker skipChecker) {

        // Check if this ellipsoid overlaps the main cavern Y band
        double ellipsoidMinY = y - verticalRadius;
        double ellipsoidMaxY = y + verticalRadius;

        boolean overlapsMainCavern = ellipsoidMaxY > CAVERN_FLOOR_Y && ellipsoidMinY < CAVERN_CEILING_Y;

        if (overlapsMainCavern) {
            return false;
        }

        return super.carveEllipsoid(context, config, chunk, biomeAccessor, aquifer,
                x, y, z, horizontalRadius, verticalRadius, carvingMask, skipChecker);
    }
}

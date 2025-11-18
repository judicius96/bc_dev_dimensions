package com.judicius.bcdimensions;

import com.judicius.bcdimensions.portals.BwgPortalBlock;
import com.judicius.bcdimensions.portals.RuPortalBlock;
import com.judicius.bcdimensions.portals.SandPortalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BCDimensions.MODID);

    public static final RegistryObject<Block> SAND_PORTAL = BLOCKS.register("sand_portal",
            () -> new SandPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11)));

    public static final RegistryObject<Block> GRANITE_PORTAL = BLOCKS.register("granite_portal",
            () -> new RuPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11)));

    public static final RegistryObject<Block> DIORITE_PORTAL = BLOCKS.register("diorite_portal",
            () -> new BwgPortalBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().strength(-1.0F).lightLevel(s -> 11)));
}

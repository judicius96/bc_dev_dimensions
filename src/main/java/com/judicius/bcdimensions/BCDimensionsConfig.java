package com.judicius.bcdimensions;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class BCDimensionsConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SAND_PORTAL_FRAME_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MINING_PORTAL_FRAME_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<String> GALLERY_MODE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        GALLERY_MODE = builder
                .comment(
                        "Gallery display mode for /palette <filter>.",
                        "GRID: blocks wrap into rows of 72, stepping away from the player.",
                        "CONTINUOUS: blocks generate in a single continuous row.",
                        "Default: GRID"
                )
                .define("galleryMode", "GRID");

        builder.comment("Sand Portal Settings").push("sandPortal");

        SAND_PORTAL_FRAME_BLOCKS = builder
                .comment(
                        "Blocks that form a valid Sand Portal frame.",
                        "Use registry names e.g. minecraft:sandstone",
                        "The first block in the list is used when auto-building the return portal.",
                        "Players may rebuild the return portal using any combination of blocks from this list."
                )
                .defineList("frameBlocks",
                        List.of("minecraft:sandstone", "minecraft:cut_sandstone", "minecraft:smooth_sandstone"),
                        entry -> entry instanceof String);

        builder.pop();

        builder.comment("Mining Portal Settings").push("miningPortal");

        MINING_PORTAL_FRAME_BLOCKS = builder
                .comment(
                        "Blocks that form a valid Mining Portal frame.",
                        "Use registry names e.g. minecraft:polished_granite",
                        "The first block in the list is used when auto-building the return portal.",
                        "Players may rebuild the return portal using any combination of blocks from this list."
                )
                .defineList("frameBlocks",
                        List.of("minecraft:polished_granite", "minecraft:granite"),
                        entry -> entry instanceof String);

        builder.pop();

        SPEC = builder.build();
    }
}


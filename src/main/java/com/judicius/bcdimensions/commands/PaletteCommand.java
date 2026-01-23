package com.judicius.bcdimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class PaletteCommand {

    private static final Map<UUID, ReturnLocation> returnLocations = new HashMap<>();

    private static class ReturnLocation {
        ResourceKey<Level> dimension;
        BlockPos pos;
        float yaw;
        float pitch;

        ReturnLocation(ResourceKey<Level> dimension, BlockPos pos, float yaw, float pitch) {
            this.dimension = dimension;
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("palette")
                .then(Commands.literal("enter")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();

                            // Save current location to memory
                            returnLocations.put(player.getUUID(), new ReturnLocation(
                                    player.level().dimension(),
                                    player.blockPosition(),
                                    player.getYRot(),
                                    player.getXRot()
                            ));

                            // Save to persistent NBT data
                            CompoundTag playerData = player.getPersistentData();
                            playerData.putString("palette_return_dim", player.level().dimension().location().toString());
                            playerData.putInt("palette_return_x", player.blockPosition().getX());
                            playerData.putInt("palette_return_y", player.blockPosition().getY());
                            playerData.putInt("palette_return_z", player.blockPosition().getZ());
                            playerData.putFloat("palette_return_yaw", player.getYRot());
                            playerData.putFloat("palette_return_pitch", player.getXRot());

                            // Teleport to palette dimension
                            ServerLevel paletteLevel = player.getServer().getLevel(
                                    ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                                            new ResourceLocation("bc_dimensions", "palette"))
                            );

                            if (paletteLevel != null) {
                                player.teleportTo(paletteLevel, 0.5, -63, 0.5, 0, 0);
                                player.sendSystemMessage(Component.literal("Welcome to the Palette dimension!"));
                                return 1;
                            } else {
                                source.sendFailure(Component.literal("Palette dimension not found!"));
                                return 0;
                            }
                        }))
                .then(Commands.literal("exit")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();

                            ReturnLocation returnLoc = returnLocations.get(player.getUUID());

                            // If not in memory, try loading from persistent data
                            if (returnLoc == null) {
                                CompoundTag playerData = player.getPersistentData();
                                if (playerData.contains("palette_return_dim")) {
                                    ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                                            new ResourceLocation(playerData.getString("palette_return_dim")));
                                    BlockPos pos = new BlockPos(
                                            playerData.getInt("palette_return_x"),
                                            playerData.getInt("palette_return_y"),
                                            playerData.getInt("palette_return_z")
                                    );
                                    returnLoc = new ReturnLocation(dim, pos,
                                            playerData.getFloat("palette_return_yaw"),
                                            playerData.getFloat("palette_return_pitch"));
                                }
                            }

                            if (returnLoc == null) {
                                source.sendFailure(Component.literal("No return location found!"));
                                return 0;
                            }

                            ServerLevel returnLevel = player.getServer().getLevel(returnLoc.dimension);

                            if (returnLevel != null) {
                                player.teleportTo(returnLevel,
                                        returnLoc.pos.getX() + 0.5,
                                        returnLoc.pos.getY(),
                                        returnLoc.pos.getZ() + 0.5,
                                        returnLoc.yaw,
                                        returnLoc.pitch);

                                // Clear saved location
                                returnLocations.remove(player.getUUID());
                                CompoundTag playerData = player.getPersistentData();
                                playerData.remove("palette_return_dim");
                                playerData.remove("palette_return_x");
                                playerData.remove("palette_return_y");
                                playerData.remove("palette_return_z");
                                playerData.remove("palette_return_yaw");
                                playerData.remove("palette_return_pitch");

                                player.sendSystemMessage(Component.literal("Returned to previous location!"));
                                return 1;
                            } else {
                                source.sendFailure(Component.literal("Return dimension not found!"));
                                return 0;
                            }
                        }))
                .then(Commands.literal("clear")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();

                            if (!level.dimension().location().toString().equals("bc_dimensions:palette")) {
                                source.sendFailure(Component.literal("This command can only be used in the Palette dimension!"));
                                return 0;
                            }

                            ServerPlayer player = source.getPlayerOrException();
                            clearGallery(level, player);
                            return 1;
                        }))
                .then(Commands.argument("filter", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();

                            if (!level.dimension().location().toString().equals("bc_dimensions:palette")) {
                                source.sendFailure(Component.literal("This command can only be used in the Palette dimension!"));
                                return 0;
                            }

                            ServerPlayer player = source.getPlayerOrException();
                            String filter = StringArgumentType.getString(context, "filter");

                            generateGallery(level, player, filter);
                            return 1;
                        })));
    }

    private static void generateGallery(ServerLevel level, ServerPlayer player, String filter) {
        Direction playerFacing = player.getDirection();
        BlockPos startPos = player.blockPosition().relative(playerFacing, 5);

        Map<String, List<Block>> blocksByMod = new LinkedHashMap<>();
        Set<String> seenTranslationKeys = new HashSet<>();

        ForgeRegistries.BLOCKS.forEach(block -> {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null) {
                String path = id.getPath();

                if (!containsWholeWord(path, filter)) {
                    return;
                }

                if (isNonBuildingBlock(block, path)) {
                    return;
                }

                String translationKey = block.getDescriptionId();
                if (seenTranslationKeys.contains(translationKey)) {
                    return;
                }
                seenTranslationKeys.add(translationKey);

                String modId = id.getNamespace();
                blocksByMod.computeIfAbsent(modId, k -> new ArrayList<>()).add(block);
            }
        });

        int currentZ = 0;
        int totalBlocks = 0;

        for (Map.Entry<String, List<Block>> entry : blocksByMod.entrySet()) {
            String modId = entry.getKey();
            List<Block> blocks = entry.getValue();

            blocks.sort((a, b) -> {
                String pathA = ForgeRegistries.BLOCKS.getKey(a).getPath();
                String pathB = ForgeRegistries.BLOCKS.getKey(b).getPath();
                return getBlockPriority(pathA) - getBlockPriority(pathB);
            });

            int columnHeight = 0;
            int columnZ = 0;

            for (Block block : blocks) {
                String blockPath = ForgeRegistries.BLOCKS.getKey(block).getPath();

                // Skip gates - they'll be placed by their matching fence
                if (blockPath.contains("fence_gate")) {
                    continue;
                }

                BlockState state = block.defaultBlockState();

                // Remove waterlogging from all blocks
                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    state = state.setValue(BlockStateProperties.WATERLOGGED, false);
                }

                if (block instanceof LeavesBlock) {
                    state = state.setValue(LeavesBlock.PERSISTENT, true);
                }

                // Handle doors - take 2 vertical spaces, then move to next column
                if (blockPath.contains("door") && !blockPath.contains("trap")) {
                    BlockPos pos = startPos.offset(0, 0, currentZ + columnZ);

                    if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                            state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        BlockState lowerState = state
                                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        level.setBlock(pos, lowerState, 3);

                        BlockState upperState = state
                                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        level.setBlock(pos.above(), upperState, 3);

                        totalBlocks++;
                        columnZ++;
                        columnHeight = 0;
                        continue;
                    }
                }

                // Handle pressure plates - solo column
                if (blockPath.contains("pressure_plate")) {
                    BlockPos pos = startPos.offset(0, 0, currentZ + columnZ);

                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                    }

                    level.setBlock(pos, state, 3);
                    totalBlocks++;
                    columnZ++;
                    columnHeight = 0;
                    continue;
                }

                // Handle fences - place at Y=2 with gate next to it
                if (blockPath.contains("fence") && !blockPath.contains("gate")) {
                    BlockPos fencePos = startPos.offset(0, 2, currentZ + columnZ);

                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                    }

                    level.setBlock(fencePos, state, 3);
                    totalBlocks++;

                    // Place gate next to fence at same height
                    String gateId = blockPath.replace("fence", "fence_gate");
                    Block gateBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modId, gateId));

                    if (gateBlock != null && gateBlock != Blocks.AIR) {
                        BlockPos gatePos = fencePos.offset(0, 0, 1);
                        BlockState gateState = gateBlock.defaultBlockState();

                        // Remove waterlogging from gate
                        if (gateState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                            gateState = gateState.setValue(BlockStateProperties.WATERLOGGED, false);
                        }

                        if (gateState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                            gateState = gateState.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        }
                        level.setBlock(gatePos, gateState, 3);
                        totalBlocks++;
                    }

                    // Don't increment columnHeight, leave space for other blocks
                    continue;
                }

                // Handle trapdoors
                if (blockPath.contains("trapdoor")) {
                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) &&
                            state.hasProperty(BlockStateProperties.HALF)) {
                        state = state
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing)
                                .setValue(BlockStateProperties.HALF, Half.BOTTOM);
                    }
                }

                // Regular blocks - stack up to 3 high
                BlockPos pos = startPos.offset(0, columnHeight, currentZ + columnZ);

                // Rotate directional blocks
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                } else if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
                    int rotation = switch (playerFacing) {
                        case NORTH -> 0;
                        case EAST -> 4;
                        case SOUTH -> 8;
                        case WEST -> 12;
                        default -> 0;
                    };
                    state = state.setValue(BlockStateProperties.ROTATION_16, rotation);
                }

                level.setBlock(pos, state, 3);
                totalBlocks++;
                columnHeight++;

                // Move to next column when we hit 3 blocks
                if (columnHeight >= 3) {
                    columnZ++;
                    columnHeight = 0;
                }
            }

            currentZ += (columnZ + 2);
        }

        player.sendSystemMessage(Component.literal("Generated gallery with " + totalBlocks + " blocks from " + blocksByMod.size() + " mods!"));
    }

    private static int getBlockPriority(String path) {
        if (path.contains("planks")) return 1;
        if (path.contains("log") || path.contains("wood")) return 2;
        if (path.contains("stairs")) return 3;
        if (path.contains("slab")) return 4;
        if (path.contains("fence") && !path.contains("gate")) return 5;
        if (path.contains("door") && !path.contains("trap")) return 6;
        if (path.contains("trapdoor")) return 7;
        if (path.contains("pressure_plate")) return 8;
        if (path.contains("button")) return 9;
        if (path.contains("sign")) return 10;
        return 100;
    }

    private static void clearGallery(ServerLevel level, ServerPlayer player) {
        Direction playerFacing = player.getDirection();
        BlockPos startPos = player.blockPosition().relative(playerFacing, 5);

        int cleared = 0;
        int floorY = player.blockPosition().getY() - 1;

        for (int x = -100; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                for (int z = -100; z < 100; z++) {
                    BlockPos pos = startPos.offset(x, y, z);

                    if (pos.getY() <= floorY) {
                        continue;
                    }

                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        cleared++;
                    }
                }
            }
        }

        // Clear dropped items
        net.minecraft.world.phys.AABB clearBox = new net.minecraft.world.phys.AABB(
                startPos.offset(-100, 0, -100),
                startPos.offset(100, 100, 100)
        );

        List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class,
                clearBox
        );

        for (net.minecraft.world.entity.item.ItemEntity item : items) {
            item.discard();
        }

        player.sendSystemMessage(Component.literal("Cleared " + cleared + " blocks and " + items.size() + " items!"));
    }

    private static boolean containsWholeWord(String text, String word) {
        // Handle multi-word searches (like "dark_oak")
        if (word.contains("_")) {
            return text.contains(word);
        }

        String[] parts = text.split("_");

        boolean hasWord = false;
        for (String part : parts) {
            if (part.equals(word)) {
                hasWord = true;
                break;
            }
        }

        if (!hasWord) return false;

        // When searching for "oak", exclude dark_oak
        if (word.equals("oak") && text.contains("dark")) {
            return false;
        }

        return true;
    }

    private static boolean isNonBuildingBlock(Block block, String path) {
        // Check tags
        if (block.defaultBlockState().is(BlockTags.FLOWERS)) {
            return true;
        }

        // Check path strings
        return path.contains("propagule") ||
                path.contains("potted") ||
                path.contains("torch") ||
                path.contains("sapling") ||
                path.contains("coral") ||
                path.contains("kelp") ||
                path.contains("flower_pot") ||
                path.contains("mushroom");
    }
}
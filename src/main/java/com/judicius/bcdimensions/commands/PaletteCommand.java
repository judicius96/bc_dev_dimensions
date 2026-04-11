package com.judicius.bcdimensions.command;

import com.judicius.bcdimensions.BCDimensionsConfig;
import com.judicius.bcdimensions.palette.PaletteProtectionData;
import com.judicius.bcdimensions.palette.PaletteStateData;
import com.judicius.bcdimensions.registry.BCRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
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

    private static final ResourceKey<Level> PALETTE_DIM_KEY = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation("bc_dimensions", "palette")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("palette")

                // /palette enter
                .then(Commands.literal("enter")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            ServerLevel currentLevel = (ServerLevel) player.level();

                            PaletteStateData paletteData = PaletteStateData.get(currentLevel);
                            PaletteStateData.PlayerPaletteState state = paletteData.getState(player.getUUID());

                            // Capture return location BEFORE teleport
                            String returnDim = currentLevel.dimension().location().toString();
                            int returnX = player.blockPosition().getX();
                            int returnY = player.blockPosition().getY();
                            int returnZ = player.blockPosition().getZ();
                            float returnYaw = player.getYRot();
                            float returnPitch = player.getXRot();

                            // Calculate per-player Palette coordinates using scale factor
                            double paletteX = returnX * 16 + 0.5;
                            double paletteZ = returnZ * 16 + 0.5;

                            if (state.isInside) {
                                // Already has a session — just teleport them back in, no new snapshot
                                player.sendSystemMessage(Component.literal("§eYou have an active Palette session. Returning you to it."));
                            } else {
                                // Fresh entry — snapshot before clearing
                                state.saveSnapshot(player);
                                state.isInside = true;
                                paletteData.markDirty();
                            }

                            // Save return location into PaletteStateData before teleport
                            state.returnDim = returnDim;
                            state.returnX = returnX;
                            state.returnY = returnY;
                            state.returnZ = returnZ;
                            state.returnYaw = returnYaw;
                            state.returnPitch = returnPitch;
                            paletteData.markDirty();

                            ServerLevel paletteLevel = player.getServer().getLevel(PALETTE_DIM_KEY);
                            if (paletteLevel == null) {
                                source.sendFailure(Component.literal("Palette dimension not found!"));
                                return 0;
                            }

                            player.teleportTo(paletteLevel, paletteX, -63, paletteZ, 0, 0);

                            // Clear inventory and give paintbrush AFTER teleport
                            player.getInventory().clearContent();
                            ItemStack brush = new ItemStack(BCRegistry.PALETTE_BRUSH.get());
                            player.getInventory().add(brush);

                            player.sendSystemMessage(Component.literal("§aWelcome to the Palette dimension! Right-click blocks to copy a stack. Left-click to remove."));
                            return 1;
                        }))

                // /palette exit
                .then(Commands.literal("exit")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            ServerLevel currentLevel = (ServerLevel) player.level();

                            PaletteStateData paletteData = PaletteStateData.get(currentLevel);
                            PaletteStateData.PlayerPaletteState state = paletteData.getState(player.getUUID());

                            // Restore snapshot
                            if (state.hasSnapshot) {
                                state.restoreSnapshot(player);
                            } else {
                                source.sendFailure(Component.literal("No palette snapshot found. Contact an admin."));
                                return 0;
                            }

                            // Get return location from PaletteStateData
                            if (state.returnDim == null || state.returnDim.isEmpty()) {
                                source.sendFailure(Component.literal("No return location found! Contact an admin."));
                                return 0;
                            }

                            ResourceKey<Level> dim = ResourceKey.create(
                                    net.minecraft.core.registries.Registries.DIMENSION,
                                    new ResourceLocation(state.returnDim));
                            BlockPos pos = new BlockPos(state.returnX, state.returnY, state.returnZ);
                            float yaw = state.returnYaw;
                            float pitch = state.returnPitch;

                            ServerLevel returnLevel = player.getServer().getLevel(dim);
                            if (returnLevel == null) {
                                source.sendFailure(Component.literal("Return dimension not found! Contact an admin."));
                                return 0;
                            }

                            player.teleportTo(returnLevel,
                                    pos.getX() + 0.5,
                                    pos.getY(),
                                    pos.getZ() + 0.5,
                                    yaw, pitch);

                            // Clear palette state (also clears return location)
                            state.clear();
                            paletteData.markDirty();

                            player.sendSystemMessage(Component.literal("§aReturned to previous location."));
                            return 1;
                        }))

                // /palette restore <player> — OP only
                .then(Commands.literal("restore")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                    ServerLevel level = source.getLevel();

                                    PaletteStateData paletteData = PaletteStateData.get(level);
                                    PaletteStateData.PlayerPaletteState state = paletteData.getState(target.getUUID());

                                    if (!state.hasSnapshot) {
                                        source.sendFailure(Component.literal("No palette snapshot found for " + target.getName().getString()));
                                        return 0;
                                    }

                                    state.restoreSnapshot(target);
                                    source.sendSuccess(() -> Component.literal("§aPalette snapshot restored for " + target.getName().getString()), true);
                                    target.sendSystemMessage(Component.literal("§aYour palette snapshot has been restored by an admin."));
                                    return 1;
                                })))

                // /palette clear — must be in Palette dimension
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

                // /palette <filter> — generate gallery
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
        Direction spreadDir = playerFacing.getClockWise();
        BlockPos startPos = player.blockPosition().relative(playerFacing, 5);

        boolean isGrid = BCDimensionsConfig.GALLERY_MODE.get().equalsIgnoreCase("GRID");

        Map<String, List<Block>> blocksByMod = new LinkedHashMap<>();
        Set<String> seenTranslationKeys = new HashSet<>();

        ForgeRegistries.BLOCKS.forEach(block -> {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null) {
                String path = id.getPath();

                if (!containsWholeWord(path, filter)) return;
                if (isNonBuildingBlock(block, path)) return;

                String translationKey = block.getDescriptionId();
                if (seenTranslationKeys.contains(translationKey)) return;
                seenTranslationKeys.add(translationKey);

                String modId = id.getNamespace();
                blocksByMod.computeIfAbsent(modId, k -> new ArrayList<>()).add(block);
            }
        });

        int currentZ = 0;   // spread direction offset (right)
        int currentRow = 0; // forward direction offset (away from player), grid mode only
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

                if (blockPath.contains("fence_gate")) continue;

                // Grid mode — wrap to new row at 72
                if (isGrid && (currentZ + columnZ) >= 72) {
                    currentRow += 5;
                    currentZ = 0;
                    columnZ = 0;
                    columnHeight = 0;
                }

                BlockPos rowBase = isGrid
                        ? startPos.relative(playerFacing, currentRow)
                        : startPos;

                BlockState state = block.defaultBlockState();

                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    state = state.setValue(BlockStateProperties.WATERLOGGED, false);
                }

                if (block instanceof LeavesBlock) {
                    state = state.setValue(LeavesBlock.PERSISTENT, true);
                }

                // Doors
                if (blockPath.contains("door") && !blockPath.contains("trap")) {
                    BlockPos pos = rowBase.relative(spreadDir, currentZ + columnZ);

                    if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                            state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        state = state
                                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        level.setBlock(pos, state, 3);

                        BlockState upper = block.defaultBlockState()
                                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        level.setBlock(pos.above(), upper, 3);

                        totalBlocks++;
                        columnZ++;
                        columnHeight = 0;
                    }
                    continue;
                }

                // Pressure plates
                if (blockPath.contains("pressure_plate")) {
                    BlockPos pos = rowBase.relative(spreadDir, currentZ + columnZ);
                    level.setBlock(pos, state, 3);
                    totalBlocks++;
                    columnZ++;
                    columnHeight = 0;
                    continue;
                }

                // Fences + gates
                if (blockPath.contains("fence") && !blockPath.contains("gate")) {
                    BlockPos fencePos = rowBase.relative(spreadDir, currentZ + columnZ).above(2);

                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                    }

                    level.setBlock(fencePos, state, 3);
                    totalBlocks++;

                    String gateId = blockPath.replace("fence", "fence_gate");
                    Block gateBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modId, gateId));

                    if (gateBlock != null && gateBlock != Blocks.AIR) {
                        BlockPos gatePos = fencePos.relative(spreadDir, 1);
                        BlockState gateState = gateBlock.defaultBlockState();

                        if (gateState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                            gateState = gateState.setValue(BlockStateProperties.WATERLOGGED, false);
                        }
                        if (gateState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                            gateState = gateState.setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing);
                        }
                        level.setBlock(gatePos, gateState, 3);
                        totalBlocks++;
                    }
                    continue;
                }

                // Trapdoors
                if (blockPath.contains("trapdoor")) {
                    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) &&
                            state.hasProperty(BlockStateProperties.HALF)) {
                        state = state
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, playerFacing)
                                .setValue(BlockStateProperties.HALF, Half.BOTTOM);
                    }
                }

                // Regular blocks — rotate to face player, stack up to 3 high
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

                BlockPos pos = rowBase.relative(spreadDir, currentZ + columnZ).above(columnHeight);
                level.setBlock(pos, state, 3);
                totalBlocks++;
                columnHeight++;

                if (columnHeight >= 3) {
                    columnZ++;
                    columnHeight = 0;
                }
            }

            currentZ += (columnZ + 2);
        }

        player.sendSystemMessage(Component.literal("§aGenerated gallery with " + totalBlocks + " blocks from " + blocksByMod.size() + " mods."));
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

        PaletteProtectionData protection = PaletteProtectionData.get(level);
        int skipped = 0;

        for (int x = -100; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                for (int z = -100; z < 100; z++) {
                    BlockPos pos = startPos.offset(x, y, z);
                    if (pos.getY() <= floorY) continue;
                    if (level.getBlockState(pos).isAir()) continue;
                    if (protection.isProtected(pos)) {
                        skipped++;
                        continue;
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    cleared++;
                }
            }
        }

        net.minecraft.world.phys.AABB clearBox = new net.minecraft.world.phys.AABB(
                startPos.offset(-100, 0, -100),
                startPos.offset(100, 100, 100)
        );

        List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, clearBox);

        for (net.minecraft.world.entity.item.ItemEntity item : items) {
            item.discard();
        }

        player.sendSystemMessage(Component.literal("§aCleared " + cleared + " blocks and " + items.size() + " items. §e" + skipped + " protected blocks skipped."));
    }

    private static boolean containsWholeWord(String text, String word) {
        if (word.contains("_")) return text.contains(word);

        String[] parts = text.split("_");
        boolean hasWord = false;
        for (String part : parts) {
            if (part.equals(word)) { hasWord = true; break; }
        }
        if (!hasWord) return false;
        if (word.equals("oak") && text.contains("dark")) return false;
        if (word.equals("oak") && text.contains("pale")) return false;
        return true;
    }

    private static boolean isNonBuildingBlock(Block block, String path) {
        if (block.defaultBlockState().is(BlockTags.FLOWERS)) return true;

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

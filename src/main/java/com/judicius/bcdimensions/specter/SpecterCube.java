package com.judicius.bcdimensions.specter;

import com.judicius.bcdimensions.registry.BCRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpecterCube {
    private final UUID owner;
    private final int position;
    private int height;
    private BlockPos spawnBlock;
    private final List<UUID> guests;
    private final SpecterHandler handler;

    public SpecterCube(SpecterHandler handler, UUID owner, int position) {
        this.handler = handler;
        this.owner = owner;
        this.position = position;
        this.height = 128; // Starting height
        this.spawnBlock = new BlockPos(position * 16 + 8, 2, 8);
        this.guests = new ArrayList<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public int getPosition() {
        return position;
    }

    public int getHeight() {
        return height;
    }

    public BlockPos getSpawnBlock() {
        return spawnBlock;
    }

    public void setSpawnBlock(BlockPos pos) {
        this.spawnBlock = pos;
        this.handler.setDirty();
    }

    public void generate(ServerLevel world) {
        System.out.println("[SpecterCube] Generating cube for position: " + position);
        BlockPos corner = new BlockPos(position * 16, 0, 0);
        BlockPos opposite = corner.offset(15, height + 1, 15);
        System.out.println("[SpecterCube] Corner: " + corner + " Opposite: " + opposite);

        // Generate hollow barrier cube
        generateHollowCube(world, corner, opposite, BCRegistry.SPECTER_BLOCK.get().defaultBlockState());
        System.out.println("[SpecterCube] Cube generation complete");
    }

    private void generateHollowCube(ServerLevel world, BlockPos pos1, BlockPos pos2, BlockState state) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Only place blocks on the walls (hollow cube)
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        world.setBlock(new BlockPos(x, y, z), state, 3);
                    }
                }
            }
        }
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putString("owner", owner.toString());
        tag.putInt("position", position);
        tag.putInt("height", height);
        tag.putInt("spawnX", spawnBlock.getX());
        tag.putInt("spawnY", spawnBlock.getY());
        tag.putInt("spawnZ", spawnBlock.getZ());

    }

    public static SpecterCube readFromNBT(SpecterHandler handler, CompoundTag tag) {
        UUID owner = UUID.fromString(tag.getString("owner"));
        int position = tag.getInt("position");
        SpecterCube cube = new SpecterCube(handler, owner, position);
        cube.height = tag.getInt("height");
        cube.spawnBlock = new BlockPos(
                tag.getInt("spawnX"),
                tag.getInt("spawnY"),
                tag.getInt("spawnZ")
        );
        return cube;
    }

    public boolean isPlayerAllowed(UUID playerUUID) {
        return owner.equals(playerUUID) || guests.contains(playerUUID);
    }
}
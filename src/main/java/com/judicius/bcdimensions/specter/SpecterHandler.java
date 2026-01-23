package com.judicius.bcdimensions.specter;

import com.judicius.bcdimensions.portals.DimKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecterHandler extends SavedData {
    private static final String DATA_NAME = "bc_specter_handler";

    private final Map<UUID, SpecterCube> cubes = new HashMap<>();
    private int positionCounter = 0;
    private final ServerLevel world;

    public SpecterHandler(ServerLevel world) {
        this.world = world;
    }

    public static SpecterHandler get(ServerLevel world) {
        DimensionDataStorage storage = world.getDataStorage();
        return storage.computeIfAbsent(
                tag -> load(world, tag),
                () -> new SpecterHandler(world),
                DATA_NAME
        );
    }

    public static SpecterHandler load(ServerLevel world, CompoundTag tag) {
        SpecterHandler handler = new SpecterHandler(world);
        handler.positionCounter = tag.getInt("positionCounter");

        ListTag cubeList = tag.getList("cubes", Tag.TAG_COMPOUND);
        for (int i = 0; i < cubeList.size(); i++) {
            CompoundTag cubeTag = cubeList.getCompound(i);
            SpecterCube cube = SpecterCube.readFromNBT(handler, cubeTag);
            handler.cubes.put(cube.getOwner(), cube);
        }

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("positionCounter", positionCounter);

        ListTag cubeList = new ListTag();
        for (SpecterCube cube : cubes.values()) {
            CompoundTag cubeTag = new CompoundTag();
            cube.writeToNBT(cubeTag);
            cubeList.add(cubeTag);
        }
        tag.put("cubes", cubeList);

        return tag;
    }

    public SpecterCube getOrCreateCube(UUID playerUUID) {
        return cubes.computeIfAbsent(playerUUID, uuid -> {
            SpecterCube cube = new SpecterCube(this, uuid, positionCounter);
            positionCounter += 16;
            cube.generate(world);
            setDirty();
            return cube;
        });
    }

    public SpecterCube getCube(UUID playerUUID) {
        return cubes.get(playerUUID);
    }

    public SpecterCube getCubeAtPosition(BlockPos pos) {
        if (pos.getZ() < 0 || pos.getZ() > 15) {
            return null;
        }

        int chunkX = pos.getX() >> 4;
        int cubePosition = chunkX * 16;

        for (SpecterCube cube : cubes.values()) {
            if (cube.getPosition() == cubePosition) {
                int minX = cubePosition;
                int maxX = cubePosition + 15;
                int minY = 0;
                int maxY = cube.getHeight() + 1;

                if (pos.getX() >= minX && pos.getX() <= maxX &&
                        pos.getY() >= minY && pos.getY() <= maxY) {
                    return cube;
                }
            }
        }

        return null;
    }

    public void teleportPlayerToCube(ServerPlayer player) {
        // Save return position
        CompoundTag playerData = player.getPersistentData();
        playerData.putDouble("specterReturnX", player.getX());
        playerData.putDouble("specterReturnY", player.getY());
        playerData.putDouble("specterReturnZ", player.getZ());
        playerData.putString("specterReturnDim", player.level().dimension().location().toString());

        UUID playerUUID = player.getUUID();
        SpecterCube cube = getOrCreateCube(playerUUID);

        if (player.level().dimension() != DimKeys.SPECTER) {
            ServerLevel specterWorld = player.server.getLevel(DimKeys.SPECTER);
            if (specterWorld != null) {
                player.changeDimension(specterWorld);
                BlockPos spawn = cube.getSpawnBlock();
                player.teleportTo(specterWorld, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
                return;
            }
        }

        BlockPos spawn = cube.getSpawnBlock();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
    }

    public void teleportPlayerBack(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();

        if (playerData.contains("specterReturnX")) {
            double x = playerData.getDouble("specterReturnX");
            double y = playerData.getDouble("specterReturnY");
            double z = playerData.getDouble("specterReturnZ");
            String dimString = playerData.getString("specterReturnDim");

            ResourceLocation dimLocation = new ResourceLocation(dimString);
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLocation);
            ServerLevel targetLevel = player.server.getLevel(dimKey);

            if (targetLevel != null && player.level().dimension() != dimKey) {
                player.changeDimension(targetLevel);
                player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
            } else {
                player.teleportTo(x, y, z);
            }
        }
    }

    public void checkPlayerPosition(ServerPlayer player) {
        if (player.isCreative()) {
            return;
        }

        SpecterCube cubeAtPos = getCubeAtPosition(player.blockPosition());
        UUID playerUUID = player.getUUID();

        if (cubeAtPos == null || !cubeAtPos.isPlayerAllowed(playerUUID)) {
            SpecterCube playerCube = getCube(playerUUID);
            if (playerCube != null) {
                BlockPos spawn = playerCube.getSpawnBlock();
                player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
            } else {
                teleportPlayerBack(player);
            }
        }
    }

    public void unloadPlayerCube(UUID playerUUID) {
        SpecterCube cube = getCube(playerUUID);
        if (cube != null) {
            int chunkX = cube.getPosition() / 16;
            world.setChunkForced(chunkX, 0, false);
        }
    }
}
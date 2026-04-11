package com.judicius.bcdimensions.spectral;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectralHandler extends SavedData {
    private static final String DATA_NAME = "bc_spectral_handler";

    private final Map<UUID, com.judicius.bcdimensions.spectral.SpectralCube> cubes = new HashMap<>();
    private int positionCounter = 0;
    private final ServerLevel world;

    public SpectralHandler(ServerLevel world) {
        this.world = world;
    }

    public static SpectralHandler get(ServerLevel world) {
        DimensionDataStorage storage = world.getDataStorage();
        return storage.computeIfAbsent(
                tag -> load(world, tag),
                () -> new SpectralHandler(world),
                DATA_NAME
        );
    }

    public static SpectralHandler load(ServerLevel world, CompoundTag tag) {
        SpectralHandler handler = new SpectralHandler(world);
        handler.positionCounter = tag.getInt("positionCounter");

        ListTag cubeList = tag.getList("cubes", Tag.TAG_COMPOUND);
        for (int i = 0; i < cubeList.size(); i++) {
            CompoundTag cubeTag = cubeList.getCompound(i);
            com.judicius.bcdimensions.spectral.SpectralCube cube = com.judicius.bcdimensions.spectral.SpectralCube.readFromNBT(handler, cubeTag);
            handler.cubes.put(cube.getOwner(), cube);
        }

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("positionCounter", positionCounter);

        ListTag cubeList = new ListTag();
        for (com.judicius.bcdimensions.spectral.SpectralCube cube : cubes.values()) {
            CompoundTag cubeTag = new CompoundTag();
            cube.writeToNBT(cubeTag);
            cubeList.add(cubeTag);
        }
        tag.put("cubes", cubeList);

        return tag;
    }

    public com.judicius.bcdimensions.spectral.SpectralCube getOrCreateCube(UUID playerUUID) {
        return cubes.computeIfAbsent(playerUUID, uuid -> {
            com.judicius.bcdimensions.spectral.SpectralCube cube = new com.judicius.bcdimensions.spectral.SpectralCube(this, uuid, positionCounter);
            positionCounter += 16;
            cube.generate(world);
            setDirty();
            return cube;
        });
    }

    public com.judicius.bcdimensions.spectral.SpectralCube getCube(UUID playerUUID) {
        return cubes.get(playerUUID);
    }

    public com.judicius.bcdimensions.spectral.SpectralCube getCubeAtPosition(BlockPos pos) {
        if (pos.getZ() < 0 || pos.getZ() > 15) {
            return null;
        }

        int chunkX = pos.getX() >> 4;
        int cubePosition = chunkX * 16;

        for (com.judicius.bcdimensions.spectral.SpectralCube cube : cubes.values()) {
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
        playerData.putDouble("spectralReturnX", player.getX());
        playerData.putDouble("spectralReturnY", player.getY());
        playerData.putDouble("spectralReturnZ", player.getZ());
        playerData.putString("spectralReturnDim", player.level().dimension().location().toString());

        UUID playerUUID = player.getUUID();
        com.judicius.bcdimensions.spectral.SpectralCube cube = getOrCreateCube(playerUUID);

        if (player.level().dimension() != DimKeys.SPECTRAL) {
            ServerLevel spectralWorld = player.server.getLevel(DimKeys.SPECTRAL);
            if (spectralWorld != null) {
                player.changeDimension(spectralWorld);
                BlockPos spawn = cube.getSpawnBlock();
                player.teleportTo(spectralWorld, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
                return;
            }
        }

        BlockPos spawn = cube.getSpawnBlock();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
    }

    public void teleportPlayerBack(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();

        if (playerData.contains("spectralReturnX")) {
            double x = playerData.getDouble("spectralReturnX");
            double y = playerData.getDouble("spectralReturnY");
            double z = playerData.getDouble("spectralReturnZ");
            String dimString = playerData.getString("spectralReturnDim");

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

        com.judicius.bcdimensions.spectral.SpectralCube cubeAtPos = getCubeAtPosition(player.blockPosition());
        UUID playerUUID = player.getUUID();

        if (cubeAtPos == null || !cubeAtPos.isPlayerAllowed(playerUUID)) {
            com.judicius.bcdimensions.spectral.SpectralCube playerCube = getCube(playerUUID);
            if (playerCube != null) {
                BlockPos spawn = playerCube.getSpawnBlock();
                player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
            } else {
                teleportPlayerBack(player);
            }
        }
    }

    public void unloadPlayerCube(UUID playerUUID) {
        com.judicius.bcdimensions.spectral.SpectralCube cube = getCube(playerUUID);
        if (cube != null) {
            int chunkX = cube.getPosition() / 16;
            world.setChunkForced(chunkX, 0, false);
        }
    }
}
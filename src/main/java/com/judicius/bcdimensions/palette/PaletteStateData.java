package com.judicius.bcdimensions.palette;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaletteStateData extends SavedData {

    private static final String DATA_NAME = "bc_additions_palette_state";

    // Map of player UUID -> their palette state
    private final Map<UUID, PlayerPaletteState> playerStates = new HashMap<>();

    public static class PlayerPaletteState {
        public boolean isInside;
        public boolean hasSnapshot;
        public CompoundTag snapshot; // Saved player inventory/data

        // PERSISTENT return location (NOT in HashMap anymore)
        public String returnDimension;
        public int returnX;
        public int returnY;
        public int returnZ;
        public float returnYaw;
        public float returnPitch;

        public PlayerPaletteState() {
            this.isInside = false;
            this.hasSnapshot = false;
            this.snapshot = new CompoundTag();
            this.returnDimension = "";
            this.returnX = 0;
            this.returnY = 0;
            this.returnZ = 0;
            this.returnYaw = 0;
            this.returnPitch = 0;
        }

        public void saveSnapshot(Player player) {
            this.snapshot = new CompoundTag();
            player.saveWithoutId(this.snapshot);
            this.hasSnapshot = true;
        }

        public void restoreSnapshot(Player player) {
            if (this.hasSnapshot && this.snapshot != null) {
                player.load(this.snapshot);
            }
        }

        public void saveReturnLocation(String dimension, int x, int y, int z, float yaw, float pitch) {
            this.returnDimension = dimension;
            this.returnX = x;
            this.returnY = y;
            this.returnZ = z;
            this.returnYaw = yaw;
            this.returnPitch = pitch;
        }

        public boolean hasReturnLocation() {
            return !this.returnDimension.isEmpty();
        }

        public void clear() {
            this.isInside = false;
            this.hasSnapshot = false;
            this.snapshot = new CompoundTag();
            this.returnDimension = "";
            this.returnX = 0;
            this.returnY = 0;
            this.returnZ = 0;
            this.returnYaw = 0;
            this.returnPitch = 0;
        }
    }

    // Get or create the saved data for a server level
    public static PaletteStateData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(PaletteStateData::load, PaletteStateData::new, DATA_NAME);
    }

    // Get state for a player (creates if doesn't exist)
    public PlayerPaletteState getState(UUID playerId) {
        return playerStates.computeIfAbsent(playerId, k -> new PlayerPaletteState());
    }

    // Mark data as dirty when modified
    public void markDirty() {
        this.setDirty();
    }

    // Save to NBT
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();

        for (Map.Entry<UUID, PlayerPaletteState> entry : playerStates.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            playerTag.putBoolean("isInside", entry.getValue().isInside);
            playerTag.putBoolean("hasSnapshot", entry.getValue().hasSnapshot);
            playerTag.put("snapshot", entry.getValue().snapshot);

            // Save return location
            playerTag.putString("returnDimension", entry.getValue().returnDimension);
            playerTag.putInt("returnX", entry.getValue().returnX);
            playerTag.putInt("returnY", entry.getValue().returnY);
            playerTag.putInt("returnZ", entry.getValue().returnZ);
            playerTag.putFloat("returnYaw", entry.getValue().returnYaw);
            playerTag.putFloat("returnPitch", entry.getValue().returnPitch);

            playerList.add(playerTag);
        }

        tag.put("players", playerList);
        return tag;
    }

    // Load from NBT
    public static PaletteStateData load(CompoundTag tag) {
        PaletteStateData data = new PaletteStateData();

        ListTag playerList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            UUID uuid = playerTag.getUUID("uuid");

            PlayerPaletteState state = new PlayerPaletteState();
            state.isInside = playerTag.getBoolean("isInside");
            state.hasSnapshot = playerTag.getBoolean("hasSnapshot");
            state.snapshot = playerTag.getCompound("snapshot");

            // Load return location
            state.returnDimension = playerTag.getString("returnDimension");
            state.returnX = playerTag.getInt("returnX");
            state.returnY = playerTag.getInt("returnY");
            state.returnZ = playerTag.getInt("returnZ");
            state.returnYaw = playerTag.getFloat("returnYaw");
            state.returnPitch = playerTag.getFloat("returnPitch");

            data.playerStates.put(uuid, state);
        }

        return data;
    }
}
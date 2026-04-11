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

    private final Map<UUID, PlayerPaletteState> playerStates = new HashMap<>();

    public static class PlayerPaletteState {
        public boolean isInside;
        public boolean hasSnapshot;
        public CompoundTag snapshot;
        public String returnDim;
        public int returnX;
        public int returnY;
        public int returnZ;
        public float returnYaw;
        public float returnPitch;

        public PlayerPaletteState() {
            this.isInside = false;
            this.hasSnapshot = false;
            this.snapshot = new CompoundTag();
            this.returnDim = "";
            this.returnX = 0;
            this.returnY = 64;
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

        public void clear() {
            this.isInside = false;
            this.hasSnapshot = false;
            this.snapshot = new CompoundTag();
            this.returnDim = "";
            this.returnX = 0;
            this.returnY = 64;
            this.returnZ = 0;
            this.returnYaw = 0;
            this.returnPitch = 0;
        }
    }

    public static PaletteStateData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(PaletteStateData::load, PaletteStateData::new, DATA_NAME);
    }

    public PlayerPaletteState getState(UUID playerId) {
        return playerStates.computeIfAbsent(playerId, k -> new PlayerPaletteState());
    }

    public void markDirty() {
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();

        for (Map.Entry<UUID, PlayerPaletteState> entry : playerStates.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            playerTag.putBoolean("isInside", entry.getValue().isInside);
            playerTag.putBoolean("hasSnapshot", entry.getValue().hasSnapshot);
            playerTag.put("snapshot", entry.getValue().snapshot);
            playerTag.putString("returnDim", entry.getValue().returnDim);
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
            state.returnDim = playerTag.getString("returnDim");
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

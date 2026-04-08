package com.judicius.bcdimensions.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaletteProtectionData extends SavedData {

    private static final String DATA_NAME = "bc_dimensions_palette_protection";

    private final Map<BlockPos, UUID> protectedBlocks = new HashMap<>();

    public static PaletteProtectionData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(PaletteProtectionData::load, PaletteProtectionData::new, DATA_NAME);
    }

    public boolean isProtected(BlockPos pos) {
        return protectedBlocks.containsKey(pos);
    }

    public UUID getOwner(BlockPos pos) {
        return protectedBlocks.get(pos);
    }

    public void protect(BlockPos pos, UUID owner) {
        protectedBlocks.put(pos, owner);
        this.setDirty();
    }

    public void unprotect(BlockPos pos) {
        protectedBlocks.remove(pos);
        this.setDirty();
    }

    public Map<BlockPos, UUID> getAll() {
        return protectedBlocks;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, UUID> entry : protectedBlocks.entrySet()) {
            CompoundTag entry_tag = new CompoundTag();
            entry_tag.putLong("pos", entry.getKey().asLong());
            entry_tag.putUUID("owner", entry.getValue());
            list.add(entry_tag);
        }

        tag.put("protected", list);
        return tag;
    }

    public static PaletteProtectionData load(CompoundTag tag) {
        PaletteProtectionData data = new PaletteProtectionData();

        ListTag list = tag.getList("protected", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry_tag = list.getCompound(i);
            BlockPos pos = BlockPos.of(entry_tag.getLong("pos"));
            UUID owner = entry_tag.getUUID("owner");
            data.protectedBlocks.put(pos, owner);
        }

        return data;
    }
}
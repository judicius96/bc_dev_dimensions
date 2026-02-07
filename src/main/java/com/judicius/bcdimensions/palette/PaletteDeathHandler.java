package com.judicius.bcdimensions.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "bc_dimensions")
public class PaletteDeathHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isEndConquered()) return; // Ignore end conquest respawns

        PaletteStateData paletteData = PaletteStateData.get(player.serverLevel());
        PaletteStateData.PlayerPaletteState state = paletteData.getState(player.getUUID());

        // If player is in a Palette session, respawn them in Palette near death location
        if (state.isInside) {
            // Get death location from player data (set by vanilla on death)
            BlockPos deathPos = player.getLastDeathLocation()
                    .map(dl -> dl.pos())
                    .orElse(new BlockPos(0, -63, 0));

            // Spawn within 5 blocks on X and Z, SAME Y (keep on floor)
            BlockPos spawnPos = new BlockPos(
                    deathPos.getX() + (player.getRandom().nextInt(11) - 5), // -5 to +5
                    deathPos.getY(), // SAME Y - don't fall through floor
                    deathPos.getZ() + (player.getRandom().nextInt(11) - 5)  // -5 to +5
            );

            // Teleport to Palette dimension at spawn pos
            var paletteLevel = player.getServer().getLevel(
                    net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.DIMENSION,
                            new ResourceLocation("bc_dimensions", "palette")
                    )
            );

            if (paletteLevel != null) {
                player.teleportTo(paletteLevel,
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        player.getYRot(),
                        player.getXRot());

                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§eYou died in the Palette Dimension. Use /palette exit to return safely."));
            }
        }
    }
}
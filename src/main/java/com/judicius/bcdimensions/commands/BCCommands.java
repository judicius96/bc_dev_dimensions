package com.judicius.bcdimensions.commands;

import com.judicius.bcdimensions.BCDimensions;
import com.judicius.bcdimensions.spectral.SpectralHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.Level;

@Mod.EventBusSubscriber(modid = BCDimensions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BCCommands {

    // Dimension keys
    private static final ResourceKey<Level> SAND_DIM =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(BCDimensions.MODID, "sand_dimension")
            );

    private static final ResourceKey<Level> MINING_DIM =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(BCDimensions.MODID, "mining_dimension")
            );

    private static final ResourceKey<Level> SPECTRAL_DIM =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(BCDimensions.MODID, "spectral_dimension")
            );

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        // /bc_sand
        d.register(Commands.literal("bc_sand")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    ServerLevel target = server.getLevel(SAND_DIM);
                    if (target == null) return 0;
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.teleportTo(target, sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot());
                    return 1;
                }));

        // /bc_mining
        d.register(Commands.literal("bc_mining")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    ServerLevel target = server.getLevel(MINING_DIM);
                    if (target == null) return 0;
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.teleportTo(target, sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot());
                    return 1;
                }));

        // /bc_spectral - teleports to player's personal spectral cube
        d.register(Commands.literal("bc_spectral")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    ServerLevel target = server.getLevel(SPECTRAL_DIM);
                    if (target == null) return 0;
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();

                    SpectralHandler handler = SpectralHandler.get(target);
                    handler.teleportPlayerToCube(sp);
                    return 1;
                }));
    }
}
package com.judicius.bcdimensions;

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

    private static final ResourceKey<Level> RU_MIRROR_DIM =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(BCDimensions.MODID, "ru_mirror_dimension")
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
        // /bc_ru
        d.register(Commands.literal("bc_ru")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    ServerLevel target = server.getLevel(RU_MIRROR_DIM);
                    if (target == null) return 0;
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.teleportTo(target, sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot());
                    return 1;
                }));

    }
}

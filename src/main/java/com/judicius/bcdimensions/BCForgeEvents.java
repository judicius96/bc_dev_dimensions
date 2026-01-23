package com.judicius.bcdimensions;

import com.judicius.bcdimensions.command.PaletteCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BCDimensions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BCForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PaletteCommand.register(event.getDispatcher());
    }
}
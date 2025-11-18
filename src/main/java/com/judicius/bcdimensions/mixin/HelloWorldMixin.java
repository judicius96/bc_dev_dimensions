package com.judicius.bcdimensions.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class HelloWorldMixin {

    private static final Logger LOGGER = LogManager.getLogger("BCDimensions|HelloMixin");

    static {
        LOGGER.error("*******************************");
        LOGGER.error("*  HELLO FROM MIXIN LOADER!   *");
        LOGGER.error("*******************************");
    }
}

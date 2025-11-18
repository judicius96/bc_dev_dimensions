package com.judicius.bcdimensions.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;

@Mixin(Regions.class)
public abstract class RegionsRegisterMixin {

    @Inject(
            method = "register(Lnet/minecraft/resources/ResourceLocation;Lterrablender/api/Region;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void bc_dimensions$filterRegions(ResourceLocation key,
                                                    Region region,
                                                    CallbackInfo ci) {
        // Safety: don't touch anything if either is null
        if (key == null || region == null) {
            return;
        }

        RegionType type = region.getType();
        String ns = key.getNamespace();

        // Hard filter: RU/BWG may NOT register OVERWORLD regions
        if (type == RegionType.OVERWORLD &&
                ("regions_unexplored".equals(ns) ||
                        "biomeswevegone".equals(ns))) {
            ci.cancel();
        }
    }
}

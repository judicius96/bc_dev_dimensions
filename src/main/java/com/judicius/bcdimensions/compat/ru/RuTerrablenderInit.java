package com.judicius.bcdimensions.compat.ru;

import terrablender.api.Regions;

public final class RuTerrablenderInit {

    private RuTerrablenderInit() {
        // utility class
    }

    public static void register() {
        Regions.register(new RuDimPrimaryRegion());
        Regions.register(new RuDimSecondaryRegion());
    }
}

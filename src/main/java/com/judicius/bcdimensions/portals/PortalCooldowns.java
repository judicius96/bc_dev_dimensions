package com.judicius.bcdimensions.portals;

import net.minecraft.server.level.ServerPlayer;
import java.util.WeakHashMap;

final class PortalCooldowns {
    private static final WeakHashMap<ServerPlayer, Long> until = new WeakHashMap<>();

    static boolean isCooling(ServerPlayer p, long now) {
        Long t = until.get(p);
        return t != null && now < t;
    }
    static void mark(ServerPlayer p, long now, int ticks) {
        until.put(p, now + ticks);
    }
}

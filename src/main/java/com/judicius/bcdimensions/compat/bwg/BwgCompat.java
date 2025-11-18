package com.judicius.bcdimensions.compat.bwg;

import net.minecraft.world.level.levelgen.SurfaceRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrablender.api.SurfaceRuleManager;
import terrablender.api.SurfaceRuleManager.RuleCategory;

/**
 * Optional integration with Biomes We've Gone (BWG).
 * If BWG is present, we grab its overworld surface rules and register them
 * so our BWG-only dimension can render like BWG intended.
 */
public final class BwgCompat {
    private static final Logger LOGGER = LogManager.getLogger("bc_dimensions");
    private static final String MODID = "bc_dimensions";

    // this is the class you pulled from the BWG jar
    private static final String BWG_SURFACE_RULES_CLASS =
            "net.potionstudios.biomeswevegone.world.level.levelgen.biome.BWGOverworldSurfaceRules";

    private BwgCompat() {}

    public static void hookSurfaceRules() {
        try {
            // load BWG's surface-rules class
            Class<?> clazz = Class.forName(BWG_SURFACE_RULES_CLASS);
            // public static SurfaceRules.RuleSource makeRules()
            var method = clazz.getMethod("makeRules");
            Object result = method.invoke(null);

            SurfaceRuleManager.addSurfaceRules(
                    RuleCategory.OVERWORLD,
                    MODID, // TerraBlender wants a string mod id here
                    (SurfaceRules.RuleSource) result
            );

            LOGGER.info("BC Dimensions: BWG detected, surface rules hooked.");
        } catch (ClassNotFoundException e) {
            // BWG not present in this environment – that's fine
            LOGGER.info("BC Dimensions: BWG not on classpath, skipping BWG surface rules.");
        } catch (Exception e) {
            LOGGER.error("BC Dimensions: failed to hook BWG surface rules", e);
        }
    }
}

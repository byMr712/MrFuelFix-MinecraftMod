package com.mr712.furnacefuelfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FurnaceFuelFixMod implements ModInitializer {
    public static final String MOD_ID = "furnacefuelfix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[MrFuelFix] Initialized successfully. Smart fuel shift-click logic enabled.");
    }
}

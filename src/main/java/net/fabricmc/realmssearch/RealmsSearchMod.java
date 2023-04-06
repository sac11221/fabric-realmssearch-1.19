package net.fabricmc.realmssearch;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealmsSearchMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("realmssearch");
	@Override
	public void onInitialize() {
		LOGGER.info("Realms Minigame Search bar initialized.");
	}
}

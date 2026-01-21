package github.xpncvr.autocomplete;

import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("betterautocomplete");
    public static final CommandPredictor PREDICTOR = new CommandPredictor(MinecraftClient.getInstance().runDirectory.toPath());

	@Override
	public void onInitialize() {
		LOGGER.info("Better autocomplete initialised");
        PREDICTOR.decayWeights();
    }
}
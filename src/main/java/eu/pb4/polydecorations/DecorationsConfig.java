package eu.pb4.polydecorations;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.EnumSet;
import java.util.Set;

/**
 * Reads config/polydecorations.json. It's read on first use, which happens either in the mixin plugin
 * or before any block is registered, so it can't reference any Minecraft classes.
 */
public final class DecorationsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("PolyDecorations");
    private static final Set<DecorationsFeature> DISABLED = load();

    private DecorationsConfig() {}

    public static boolean isEnabled(DecorationsFeature feature) {
        return !DISABLED.contains(feature);
    }

    public static boolean isAnyDisabled() {
        return !DISABLED.isEmpty();
    }

    private static Set<DecorationsFeature> load() {
        var disabled = EnumSet.noneOf(DecorationsFeature.class);
        // Data generation always needs everything
        if (System.getProperty("fabric-api.datagen") != null) {
            return disabled;
        }

        var path = FabricLoader.getInstance().getConfigDir().resolve("polydecorations.json");
        var features = new JsonObject();
        var shouldWrite = true;
        try {
            if (Files.exists(path)) {
                var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (json.has("features")) {
                    features = json.getAsJsonObject("features");
                }
                shouldWrite = false;
            }

            for (var key : features.keySet()) {
                if (DecorationsFeature.byKey(key) == null) {
                    LOGGER.warn("Unknown feature '{}' in config/polydecorations.json!", key);
                }
            }

            // Missing features are enabled and added to the file
            for (var feature : DecorationsFeature.values()) {
                if (!features.has(feature.key())) {
                    features.addProperty(feature.key(), true);
                    shouldWrite = true;
                } else if (!features.get(feature.key()).getAsBoolean()) {
                    disabled.add(feature);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to read config/polydecorations.json! Using default values (all features enabled).", e);
            return EnumSet.noneOf(DecorationsFeature.class);
        }

        if (shouldWrite) {
            try {
                var json = new JsonObject();
                json.add("features", features);
                Files.createDirectories(path.getParent());
                Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json));
            } catch (Throwable e) {
                LOGGER.warn("Failed to write config/polydecorations.json!", e);
            }
        }

        if (!disabled.isEmpty()) {
            LOGGER.info("Disabled features: {}", disabled.stream().map(DecorationsFeature::key).toList());
        }
        return disabled;
    }
}

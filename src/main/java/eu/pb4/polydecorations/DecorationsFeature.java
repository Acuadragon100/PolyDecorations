package eu.pb4.polydecorations;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Features that can be switched off in config/polydecorations.json.
 * A disabled feature doesn't register its blocks, items, block entities and entities,
 * requests no polymer block states and adds nothing to the resource pack.
 * <p>
 * Asset patterns match asset names (paths without type folder and extension, for example
 * "oak_bench_left", "statue/oak/head" or "sgui/shelf"), recipe / loot table / advancement names
 * and translation keys. Names that match no feature are shared and always kept.
 * <p>
 * This class is loaded by the mixin plugin, so it can't reference any Minecraft classes.
 */
public enum DecorationsFeature {
    CANVAS("canvas", "canvas_*", "font/canvas", "sgui/pixel"),
    MAILBOX("*_mailbox", "base_mailbox", "mailbox_*", "sgui/mailbox_select", "sgui/shelf_2", "palettes/wood/*"),
    SIGN_POST("*_sign_post", "base_sign_post", "sign_post", "sign_post_*", "palettes/wood/*"),
    ROPE("rope", "rope_*", "rope/*"),
    HAMMER("hammer"),
    TROWEL("trowel"),
    WALL_LANTERNS("*wall_lantern", "lantern_support", "lantern_support/*"),
    FENCE_LEADS("first_leash_fence_knot"),
    SHELF("*_shelf", "*_shelf_top", "*_shelf_double", "base_shelf*", "sgui/shelf", "sgui/shelf_2"),
    BENCH("*_bench", "*_bench_*", "base_bench*", "seat"),
    TABLE("*_table", "*_table_*", "base_table*"),
    TOOL_RACK("*_tool_rack", "base_tool_rack"),
    STUMP("*_stump", "*_stump_tall", "*_stump_top", "base_stump*", "seat"),
    SLEEPING_BAG("*sleeping_bag*", "palettes/bed_color/*"),
    BRAZIER("*brazier*", "empty2", "copper_campfire_fire", "copper_campfire_log_lit", "palette/copper_bars/*"),
    COPPER_CAMPFIRE("copper_campfire*"),
    GLOBE("globe*", "tiny_potato"),
    DISPLAY_CASE("display_case*"),
    FLOWER_POTS("large_flower_pot", "long_flower_pot", "long_flower_pot/*", "empty"),
    GHOST_LIGHTS("*ghost_light"),
    TRASHCAN("trashcan*", "trashcan/*", "sgui/trashcan"),
    BASKET("basket*"),
    CARDBOARD_BOX("cardboard_box*"),
    WIND_CHIME("wind_chime*", "wind_chime/*"),
    STATUES("statue", "*_statue", "statue/*"),
    TIED_CONTAINERS("tied", "tie_container");

    private final String key;
    private final Pattern assets;

    DecorationsFeature(String... assetPatterns) {
        this.key = this.name().toLowerCase(Locale.ROOT);
        var regex = new StringBuilder();
        for (var pattern : assetPatterns) {
            if (!regex.isEmpty()) {
                regex.append('|');
            }
            regex.append(Pattern.quote(pattern).replace("*", "\\E.*\\Q"));
        }
        this.assets = Pattern.compile(regex.toString());
    }

    public String key() {
        return this.key;
    }

    public boolean isEnabled() {
        return DecorationsConfig.isEnabled(this);
    }

    public static boolean anyEnabled(DecorationsFeature... features) {
        for (var feature : features) {
            if (feature.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Features owning given asset / data name. Empty set means it's shared.
     */
    public static Set<DecorationsFeature> owners(String name) {
        var set = EnumSet.noneOf(DecorationsFeature.class);
        for (var feature : values()) {
            if (feature.assets.matcher(name).matches()) {
                set.add(feature);
            }
        }
        return set;
    }

    /**
     * Whatever asset / data with given name is used by any enabled feature (or is shared).
     */
    public static boolean isUsed(String name) {
        var owners = owners(name);
        if (owners.isEmpty()) {
            return true;
        }
        for (var feature : owners) {
            if (feature.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public static DecorationsFeature byKey(String key) {
        for (var feature : values()) {
            if (feature.key.equals(key)) {
                return feature;
            }
        }
        return null;
    }
}

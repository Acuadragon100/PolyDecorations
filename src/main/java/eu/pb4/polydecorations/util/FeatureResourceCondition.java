package eu.pb4.polydecorations.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import eu.pb4.polydecorations.DecorationsFeature;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

import static eu.pb4.polydecorations.ModInit.id;

/**
 * Loads data (recipes, loot tables, advancements) only if given feature is enabled.
 * Format: {"condition": "polydecorations:feature", "feature": "bench"}
 */
public record FeatureResourceCondition(DecorationsFeature feature) implements ResourceCondition {
    private static final Codec<DecorationsFeature> FEATURE_CODEC = Codec.STRING.comapFlatMap(key -> {
        var feature = DecorationsFeature.byKey(key);
        return feature != null ? DataResult.success(feature) : DataResult.error(() -> "Unknown feature '" + key + "'");
    }, DecorationsFeature::key);

    public static final MapCodec<FeatureResourceCondition> CODEC = FEATURE_CODEC.fieldOf("feature").xmap(FeatureResourceCondition::new, FeatureResourceCondition::feature);
    public static final ResourceConditionType<FeatureResourceCondition> TYPE = ResourceConditionType.create(id("feature"), CODEC);

    public static void register() {
        ResourceConditions.register(TYPE);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return this.feature.isEnabled();
    }
}

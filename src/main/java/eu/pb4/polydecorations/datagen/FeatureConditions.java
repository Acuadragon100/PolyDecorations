package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.util.FeatureResourceCondition;
import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Makes generated data of a feature (recipes, their advancements and loot tables) load only if it's enabled.
 * The feature is found by the name of the entry, the same way as for assets.
 */
class FeatureConditions {
    // Recipes using items of other features
    private static final Map<String, List<DecorationsFeature>> EXTRA_FEATURES = Map.of(
            "basket", List.of(DecorationsFeature.ROPE),
            "copper_brazier", List.of(DecorationsFeature.COPPER_CAMPFIRE)
    );

    public static ResourceCondition[] of(Identifier id) {
        var name = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        var owners = DecorationsFeature.owners(name);
        if (owners.isEmpty()) {
            throw new IllegalStateException("No feature found for '" + id + "'! Add it to DecorationsFeature.");
        }

        var list = new ArrayList<ResourceCondition>();
        list.add(owners.size() == 1
                ? new FeatureResourceCondition(owners.iterator().next())
                : ResourceConditions.or(owners.stream().map(FeatureResourceCondition::new).toArray(ResourceCondition[]::new)));
        for (var feature : EXTRA_FEATURES.getOrDefault(name, List.of())) {
            list.add(new FeatureResourceCondition(feature));
        }
        return list.toArray(ResourceCondition[]::new);
    }

    /**
     * Adds feature conditions to all entries registered to the context.
     */
    public static <T> BootstrapContext<T> of(BootstrapContext<T> context) {
        return new ConditionalContext<>(context);
    }

    /**
     * Tag entries of this mod become optional, as they might not exist if their feature is disabled.
     */
    public static <T> TagAppender<T> optionalEntries(TagAppender<T> appender) {
        return new TagAppender<>() {
            @Override
            public TagAppender<T> add(ResourceKey<T> key) {
                if (key.identifier().getNamespace().equals(ModInit.ID)) {
                    appender.addOptional(key);
                } else {
                    appender.add(key);
                }
                return this;
            }

            @Override
            public TagAppender<T> addOptional(ResourceKey<T> key) {
                appender.addOptional(key);
                return this;
            }

            @Override
            public TagAppender<T> addTag(TagKey<T> tag) {
                appender.addTag(tag);
                return this;
            }

            @Override
            public TagAppender<T> addOptionalTag(TagKey<T> tag) {
                appender.addOptionalTag(tag);
                return this;
            }
        };
    }

    // Implements FabricRecipeOutput, as fabric uses it to get recipe ids
    private record ConditionalContext<T>(BootstrapContext<T> context) implements BootstrapContext<T>, FabricRecipeOutput {
        @Override
        public Holder.Reference<T> register(ResourceKey<T> key, T value) {
            FabricDataGenHelper.addConditions(value, FeatureConditions.of(key.identifier()));
            return this.context.register(key, value);
        }

        @Override
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
            return this.context.lookup(key);
        }

        @Override
        public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
            return this.context.listContextElements(key);
        }

        @Override
        public Identifier getRecipeIdentifier(Identifier recipeId) {
            return this.context instanceof FabricRecipeOutput output ? output.getRecipeIdentifier(recipeId) : recipeId;
        }
    }
}

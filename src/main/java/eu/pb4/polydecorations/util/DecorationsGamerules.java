package eu.pb4.polydecorations.util;

import eu.pb4.polydecorations.DecorationsFeature;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public interface DecorationsGamerules {
    // Used by seats of benches and stumps
    static GameRule<Integer> SEAT_USE_COOLDOWN = DecorationsFeature.anyEnabled(DecorationsFeature.BENCH, DecorationsFeature.STUMP) ? GameRuleBuilder.forInteger(20).range(0, 200)
            .category(GameRuleCategory.PLAYER).buildAndRegister(Identifier.parse("polydecorations:seat_use_cooldown")) : null;


    static void register() {}
}

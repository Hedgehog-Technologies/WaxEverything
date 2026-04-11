package org.hedgetech.itemframelock.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class FabricPlayerMixin {

    @Shadow
    public abstract Abilities getAbilities();

    @ModifyExpressionValue(
            method = "getDestroySpeed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z")
    )
    private boolean allowNormalBreakWhileFlying(boolean original) {
        var abilities = getAbilities();

        if (abilities.flying && !original) {
            return true;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "getDestroySpeed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean allowNormalBreakWhileSwimming(boolean original) {
        var abilities = getAbilities();

        if (abilities.flying && original) {
            return false;
        }

        return original;
    }
}

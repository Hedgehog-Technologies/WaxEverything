package org.hedgetech.waxeverything.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.hedgetech.waxeverything.network.WaxNetworkHelper;
import org.hedgetech.waxeverything.saveddata.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(method = "removeBlock", at = @At("HEAD"))
    private void waxeverything$removeBlock(BlockPos pos, boolean movedByPiston, CallbackInfoReturnable<Boolean> cir) {
        if (movedByPiston) return;
        if (!((Object) this instanceof ServerLevel serverLevel)) return;
        if (!WaxManager.isWaxed(serverLevel, pos)) return;
        WaxManager.unwax(serverLevel, pos);
        WaxNetworkHelper.sendWaxUpdate(serverLevel, pos, false);
    }

}

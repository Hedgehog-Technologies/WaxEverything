package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.waxtracking.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void waxeverything$getMenuProvider(Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir) {
        if (WaxManager.isWaxed(level, pos)) {
            Constants.LOG.debug("Block at {} is waxed, preventing menu provider from being returned.", pos);
            cir.setReturnValue(null);
        }
    }
}

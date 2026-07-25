package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.waxtracking.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @Inject(method = "fall", at = @At("RETURN"))
    private static void waxeverything$fall(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir) {
        if (level.isClientSide()) return;

        var entity = cir.getReturnValue();
        if (entity != null) {
            if (WaxManager.isWaxed(level, pos)) {
                WaxManager.unwax((ServerLevel) level, pos);
                if (entity.blockData == null) {
                    entity.blockData = new CompoundTag();
                }
                entity.blockData.putBoolean(Constants.WAXED_TAG_NAME, true);
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private void waxeverything$tick(CallbackInfo ci) {
        var entity = (FallingBlockEntity) (Object) this;
        var level = entity.level();

        if (level.isClientSide()) return;

        if (entity.blockData != null &&entity.blockData.getBooleanOr(Constants.WAXED_TAG_NAME, false)) {
            WaxManager.wax((ServerLevel) level, entity.blockPosition());
        }
    }
}

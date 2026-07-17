package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.hedgetech.waxeverything.WaxEverything;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void waxeverything$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (WaxEverything.isWaxed(level, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "getDirectSignal", at = @At("HEAD"), cancellable = true)
    private void waxeverything$getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        if (WaxEverything.isWaxed((ServerLevel) level, pos)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true)
    private void waxeverything$getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        if (WaxEverything.isWaxed((ServerLevel) level, pos)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void waxeverything$animateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (WaxEverything.isWaxed(level, pos)) {
            ci.cancel();
        }
    }
}

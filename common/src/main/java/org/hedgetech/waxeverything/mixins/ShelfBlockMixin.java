package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShelfBlock.class)
public class ShelfBlockMixin {

    @Unique
    boolean waxeverything$isWaxed = false;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void waxeverything$useItemOn(
            ItemStack itemStack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (WaxEverything.isWaxed(level, pos)) {
            Constants.LOG.debug("Shelf at {} is waxed, preventing interaction.", pos);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    private void waxeverything$hasNeighborSignal(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston, CallbackInfo ci) {
        waxeverything$isWaxed = WaxEverything.isWaxed(level, pos);
    }

    @ModifyVariable(
            method = "neighborChanged",
            name = "signal",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean waxeverything$modifySignal(boolean signal) {
        return !this.waxeverything$isWaxed && signal;
    }
}

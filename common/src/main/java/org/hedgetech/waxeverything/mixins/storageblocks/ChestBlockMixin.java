package org.hedgetech.waxeverything.mixins.storageblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.config.WaxEverythingConfig;
import org.hedgetech.waxeverything.saveddata.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {

    @Shadow
    public static Direction getConnectedDirection(BlockState state) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void waxeverything$useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.waxeverything$waxActsAsLock() && WaxEverything.isWaxed(level, pos)) {
            Constants.LOG.debug("Chest is waxed and locked, preventing interaction.");
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void waxeverything$getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        var returnState = cir.getReturnValue();
        var level = context.getLevel();
        var pos = context.getClickedPos();

        if (returnState.hasProperty(ChestBlock.TYPE) &&  returnState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            var facingDirection = context.getHorizontalDirection().getOpposite();
            var neighborPos = returnState.getValue(ChestBlock.TYPE) == ChestType.LEFT
                    ? pos.relative(facingDirection.getClockWise())
                    : pos.relative(facingDirection.getCounterClockWise());

            if (this.waxeverything$waxPreventsCombine() && WaxEverything.isWaxed(level, neighborPos)) {
                cir.setReturnValue(returnState.setValue(ChestBlock.TYPE, ChestType.SINGLE));
            } else if (level instanceof ServerLevel serverLevel && WaxEverything.isWaxed(level, neighborPos)) {
                WaxManager.wax(serverLevel, pos);
                serverLevel.levelEvent(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, pos, 0);
            }
        }
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void waxeverything$updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        var returnState = cir.getReturnValue();

        if (returnState.getValue(ChestBlock.TYPE) != ChestType.SINGLE && getConnectedDirection(returnState) == directionToNeighbour) {
            if (this.waxeverything$waxPreventsCombine() && WaxEverything.isWaxed((Level) level, pos)) {
                Constants.LOG.debug("Chest is waxed and cannot combine with neighbors, preventing combination.");
                cir.setReturnValue(returnState.setValue(ChestBlock.TYPE, ChestType.SINGLE));
            }
        }
    }

    @Unique
    private boolean waxeverything$waxActsAsLock() {
        var chest = (ChestBlock) (Object) this;

        if (chest instanceof CopperChestBlock) {
            Constants.LOG.trace("Checking if Copper Chest is locked when waxed: {}", WaxEverythingConfig.CONFIG.copperChestLockedWhenWaxed);
            return WaxEverythingConfig.CONFIG.copperChestLockedWhenWaxed;
        } else if (chest instanceof TrappedChestBlock) {
            Constants.LOG.trace("Checking if Trapped Chest is locked when waxed: {}", WaxEverythingConfig.CONFIG.trappedChestLockedWhenWaxed);
            return WaxEverythingConfig.CONFIG.trappedChestLockedWhenWaxed;
        }

        Constants.LOG.trace("Checking if Chest is locked when waxed: {}", WaxEverythingConfig.CONFIG.chestLockedWhenWaxed);
        return WaxEverythingConfig.CONFIG.chestLockedWhenWaxed;
    }

    @Unique
    private boolean waxeverything$waxPreventsCombine() {
        var chest = (ChestBlock) (Object) this;

        if (chest instanceof CopperChestBlock) {
            Constants.LOG.trace("Checking if Copper Chest prevents combine when waxed: {}", WaxEverythingConfig.CONFIG.waxPreventsCopperChestCombine);
            return WaxEverythingConfig.CONFIG.waxPreventsCopperChestCombine;
        }

        Constants.LOG.trace("Checking if Chest prevents combine when waxed: {}", WaxEverythingConfig.CONFIG.waxPreventsChestCombine);
        return WaxEverythingConfig.CONFIG.waxPreventsChestCombine;
    }
}

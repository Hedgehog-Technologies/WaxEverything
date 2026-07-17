package org.hedgetech.waxeverything.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.hedgetech.waxeverything.saveddata.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Unique
    private static final ThreadLocal<List<BlockPos>> PENDING_MOVES =
            ThreadLocal.withInitial(ArrayList::new);

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void waxeverything$capturePreMove(
            Level level, BlockPos pistonPos, Direction direction, boolean extending,
            CallbackInfoReturnable<Boolean> cir,
            @Local(name = "resolver") PistonStructureResolver resolver
    ) {
        List<BlockPos> pending = PENDING_MOVES.get();
        pending.clear();
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (BlockPos movedPos : resolver.getToPush()) {
            if (WaxManager.isWaxed(serverLevel, movedPos)) {
                pending.add(movedPos.immutable());
            }
        }
    }

    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private void waxeverything$applyPostMove(
            Level level, BlockPos pistonPos, Direction direction, boolean extending,
            CallbackInfoReturnable<Boolean> cir
    ) {
        List<BlockPos> pending = PENDING_MOVES.get();

        try {
            if (pending.isEmpty()) return;
            if (!cir.getReturnValue()) return;
            if (!(level instanceof ServerLevel serverLevel)) return;

            Direction moveDir = extending ? direction : direction.getOpposite();
            for (BlockPos oldPos : pending) {
                BlockPos newPos = oldPos.relative(moveDir);
                WaxManager.unwax(serverLevel, oldPos);
                WaxManager.wax(serverLevel, newPos);
            }
        } finally {
            pending.clear();
        }
    }
}

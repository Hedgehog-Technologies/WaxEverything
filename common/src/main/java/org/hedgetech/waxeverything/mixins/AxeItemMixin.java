package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.mixins.invokers.ShelfBlockInvoker;
import org.hedgetech.waxeverything.waxtracking.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    private void waxeverything$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);

        var isWaxed = WaxManager.isWaxed(level, pos);
        if (!isWaxed) return;

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            waxeverything$removeWax(serverLevel, pos);

            var block = state.getBlock();
            switch (block) {
                case ShelfBlock sb ->
                        ((ShelfBlockInvoker) sb).waxeverything$invokeNeighborChanged(state, level, pos, sb, null, false);
                case RedstoneTorchBlock _ -> level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
                case ChestBlock _ -> {
                    var chestType = state.getValue(ChestBlock.TYPE);
                    if (chestType != ChestType.SINGLE) {
                        var neighborPos = chestType == ChestType.LEFT
                                ? pos.relative(state.getValue(ChestBlock.FACING).getClockWise())
                                : pos.relative(state.getValue(ChestBlock.FACING).getCounterClockWise());

                        waxeverything$removeWax(serverLevel, neighborPos);
                    }
                }
                case PistonBaseBlock _, PistonHeadBlock _ -> {
                    BlockPos neighborPos;
                    if (block instanceof PistonBaseBlock) {
                        neighborPos = pos.relative(state.getValue(PistonBaseBlock.FACING));
                    } else {
                        neighborPos = pos.relative(state.getValue(PistonHeadBlock.FACING).getOpposite());
                    }

                    waxeverything$removeWax(serverLevel, neighborPos);
                }
                default -> { }
            }

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild
                    && waxeverything$isNotVanillaWaxable(state.getBlock())
            ) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            }
        }

        InteractionResult result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        cir.setReturnValue(result);
    }

    @Unique
    private void waxeverything$removeWax(ServerLevel level, BlockPos pos) {
        WaxManager.unwax(level, pos);

        if (waxeverything$isNotVanillaWaxable(level.getBlockState(pos).getBlock())) {
            level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(LevelEvent.PARTICLES_WAX_OFF, pos, 0);
        }
    }

    @Unique
    private boolean waxeverything$isNotVanillaWaxable(Block block) {
        return !HoneycombItem.WAXABLES.get().containsKey(block)
                && !HoneycombItem.WAXABLES.get().containsValue(block);
    }
}

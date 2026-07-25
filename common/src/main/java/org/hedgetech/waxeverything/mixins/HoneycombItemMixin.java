package org.hedgetech.waxeverything.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
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

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    private void waxEverything$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.MOVING_PISTON)) return;
        if (WaxManager.isWaxed(level, pos)) return;

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            waxeverything$applyWax(serverLevel, pos);

            var block = state.getBlock();
            switch (block) {
                case ShelfBlock sb ->
                        ((ShelfBlockInvoker) sb).waxeverything$invokeNeighborChanged(state, level, pos, sb, null, false);
                case RedstoneTorchBlock _ ->
                        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
                case ChestBlock _ -> {
                    if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                        var neighborPos = state.getValue(ChestBlock.TYPE) == ChestType.LEFT
                                ? pos.relative(state.getValue(ChestBlock.FACING).getClockWise())
                                : pos.relative(state.getValue(ChestBlock.FACING).getCounterClockWise());
                        waxeverything$applyWax(serverLevel, neighborPos);
                    }
                }
                case PistonBaseBlock _, PistonHeadBlock _ -> {
                    BlockPos neighborPos;
                    if (block instanceof PistonBaseBlock) {
                        neighborPos = pos.relative(state.getValue(PistonBaseBlock.FACING));
                    } else {
                        neighborPos = pos.relative(state.getValue(PistonHeadBlock.FACING).getOpposite());
                    }

                    waxeverything$applyWax(serverLevel, neighborPos);
                }
                default -> { }
            }

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild
                    && waxeverything$isNotVanillaWaxable(state.getBlock())
            ) {
                context.getItemInHand().shrink(1);
            }
        }

        InteractionResult result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        cir.setReturnValue(result);
    }

    @Unique
    private void waxeverything$applyWax(ServerLevel level, BlockPos pos) {
        WaxManager.wax(level, pos);

        if (waxeverything$isNotVanillaWaxable(level.getBlockState(pos).getBlock())) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, pos, 0);
        }
    }

    @Unique
    private boolean waxeverything$isNotVanillaWaxable(Block block) {
        return !HoneycombItem.WAXABLES.get().containsKey(block)
                && !HoneycombItem.WAXABLES.get().containsValue(block);
    }
}

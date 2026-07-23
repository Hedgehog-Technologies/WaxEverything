package org.hedgetech.waxeverything.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.mixins.invokers.ShelfBlockInvoker;
import org.hedgetech.waxeverything.saveddata.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    private void waxeverything$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.PASS) return;

        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);

        var isWaxed = WaxEverything.isWaxed(level, pos);
        if (!isWaxed) return;

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            WaxManager.unwax(serverLevel, pos);

            serverLevel.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.levelEvent(LevelEvent.PARTICLES_WAX_OFF, pos, 0);

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

                        WaxManager.unwax(serverLevel, neighborPos);
                        serverLevel.playSound(null, neighborPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                        serverLevel.levelEvent(LevelEvent.PARTICLES_WAX_OFF, neighborPos, 0);
                    }
                }
                default -> {
                }
            }

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            }
        }

        InteractionResult result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        cir.setReturnValue(result);
    }
}

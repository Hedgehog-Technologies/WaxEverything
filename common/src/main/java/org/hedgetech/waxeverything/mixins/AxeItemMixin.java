package org.hedgetech.waxeverything.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
            if (block instanceof ShelfBlock sb) {
                ((ShelfBlockInvoker) sb).waxeverything$invokeNeighborChanged(state, level, pos, sb, null, false);
            } else if (block instanceof RedstoneTorchBlock rtb) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
            }

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            }
        }

        InteractionResult result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        cir.setReturnValue(result);
    }
}

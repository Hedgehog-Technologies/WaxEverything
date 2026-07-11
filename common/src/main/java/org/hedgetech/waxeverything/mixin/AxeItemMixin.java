package org.hedgetech.waxeverything.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.hedgetech.waxeverything.saveddata.ClientWaxRegistry;
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

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        boolean isWaxed = level.isClientSide()
                ? ClientWaxRegistry.isWaxed(pos)
                : WaxManager.isWaxed((ServerLevel) level, pos);
        if (!isWaxed) return;

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            WaxManager.unwax(serverLevel, pos);

            serverLevel.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.levelEvent(LevelEvent.PARTICLES_WAX_OFF, pos, 0);

            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            }
        }

        InteractionResult result = level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        cir.setReturnValue(result);
    }
}

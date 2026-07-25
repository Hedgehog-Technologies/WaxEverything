package org.hedgetech.waxeverything.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.waxtracking.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameMixin {
    @Shadow
    private boolean fixed;

    // TODO - add a check on entity remove to unwax

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void waxeverything$onInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        Constants.LOG.info("Interacting with item frame at {}, fixed state: {}, held item: {}", ((ItemFrame) (Object) this).getPos(), this.fixed, player.getItemInHand(hand));
        var level = player.level();
        if (level.isClientSide()) return;

        var heldItem = player.getItemInHand(hand);
        var frame = (ItemFrame) (Object) this;
        var framePos = frame.getPos();
        var isEmpty = frame.getItem().isEmpty();
        Constants.LOG.info("Item frame position: {}; Supporting block position: {}; relative: {}", framePos, frame.getOnPos(), framePos.relative(frame.getDirection().getOpposite()));

        if (isEmpty) return;

        Constants.LOG.info("Interacting with item frame at {}, fixed state: {}, held item: {}", framePos, this.fixed, heldItem);
        if (this.fixed && WaxManager.isWaxed(level, frame)) {
            if (player.isCrouching()) return;

            if (heldItem.getItem() instanceof AxeItem) {
                WaxManager.unwax(level, frame);
                frame.playSound(SoundEvents.AXE_WAX_OFF, 1.0F, 1.0F);
                level.levelEvent(LevelEvent.PARTICLES_WAX_OFF, framePos, 0);

                if (!player.getAbilities().instabuild) {
                    heldItem.hurtAndBreak(1, player, hand);
                }

                this.fixed = false;

                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            var forwarded = waxeverything$interactWithTargetBehindLockedFrame(player, frame, hand, heldItem);
            cir.setReturnValue(forwarded.consumesAction() ? forwarded : InteractionResult.SUCCESS);
            return;
        }

        Constants.LOG.info("Interacting with item frame at {}, fixed state: {}, held item: {}", framePos, this.fixed, heldItem);
        if (!this.fixed && !WaxManager.isWaxed(level, frame) && heldItem.is(Items.HONEYCOMB)) {
            WaxManager.wax(level, frame);
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, framePos, 0);

            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            this.fixed = true;
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Unique
    private static InteractionResult waxeverything$interactWithTargetBehindLockedFrame(Player player, ItemFrame frame, InteractionHand hand, ItemStack heldItem) {
        var eye = player.getEyePosition();
        var look = player.getLookAngle().normalize();
        var entityRange = player.entityInteractionRange();
        var blockRange = player.blockInteractionRange();

        var farFromFrame = eye.add(look.scale(blockRange));
        var start = frame.getBoundingBox()
                .inflate(1.0E-4)
                .clip(eye, farFromFrame)
                .map(hit -> hit.add(look.scale(0.05)))
                .orElseGet(() -> frame.position().add(look.scale(Math.max(frame.getBbWidth(), frame.getBbHeight()) * 0.5 + 0.05)));

        var blockEnd = eye.add(look.scale(blockRange));
        var level = player.level();
        var blockHit = level.clip(new ClipContext(
                start, blockEnd, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player
        ));

        var hasBlock = blockHit.getType() == HitResult.Type.BLOCK
                && eye.distanceToSqr(blockHit.getLocation()) <= (blockRange * blockRange);

        EntityHitResult entityHit = null;
        var entityEnd = eye.add(look.scale(entityRange));

        if (eye.distanceToSqr(start) <= (entityRange * entityRange)) {
            var searchBox = new AABB(start, entityEnd).inflate(1.0);
            entityHit = ProjectileUtil.getEntityHitResult(
                    player, start, entityEnd, searchBox,
                    entity -> entity.isPickable() && entity != player && entity != frame,
                    entityRange * entityRange
            );
        }

        var hasEntity = entityHit != null
                && eye.distanceToSqr(entityHit.getLocation()) <= (entityRange * entityRange);

        var blockDist = hasBlock ? eye.distanceToSqr(blockHit.getLocation()) : Double.POSITIVE_INFINITY;
        var entityDist = hasEntity ? eye.distanceToSqr(entityHit.getLocation()) : Double.POSITIVE_INFINITY;

        if (entityDist < blockDist) {
            var target = entityHit.getEntity();
            var localHit = entityHit.getLocation().subtract(target.position());
            return target.interact(player, hand, localHit);
        }

        if (hasBlock) {
            var pos = blockHit.getBlockPos();
            var state = level.getBlockState(pos);

            var result = state.useWithoutItem(level, player, blockHit);
            if (!result.consumesAction() && !heldItem.isEmpty()) {
                result = state.useItemOn(heldItem, level, player, hand, blockHit);
            }

            return result;
        }

        return InteractionResult.PASS;
    }
}

package org.hedgetech.itemframelock.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.hedgetech.itemframelock.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameEntityMixin {
    @Shadow
    private boolean fixed;

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        // Only run this logic on the server
        if (player.level().isClientSide()) return;

        ItemStack heldItem = player.getItemInHand(hand);
        ItemFrame frame = ((ItemFrame) (Object) this);
        boolean isEmpty = frame.getItem().isEmpty();

        // Nothing is in the frame, so we don't care about setting fixed state
        if (isEmpty) return;

        Constants.LOG.info("Interacting with item frame at {}, fixed state: {}, held item: {}", frame.getPos(), this.fixed, heldItem);
        Constants.LOG.info("Item frame position: {}; Supporting block position: {}; relative: {}", frame.getPos(), frame.getOnPos(), frame.getPos().relative(frame.getDirection().getOpposite()));
        Constants.LOG.info("Item frame opposite direction: {}; step: {}", frame.getDirection().getOpposite(), frame.getDirection().getOpposite().step());

        // If the item frame has already been waxed, and we are interacting with an axe, remove wax / fixed state
        if (this.fixed) {
            // If player is crouching, act natural!
            if (player.isCrouching()) return;

            if (heldItem.getItem() instanceof AxeItem) {
                frame.playSound(SoundEvents.AXE_WAX_OFF, 1.0F, 1.0F);
                frame.level().levelEvent(LevelEvent.PARTICLES_WAX_OFF, frame.getPos(), 0);

                this.fixed = false;

                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            // If the frame is fixed, we want to attempt to interact with the first reachable target beyond the frame
            InteractionResult forwarded = interactFirstTargetBeyondLockedFrame(player, frame, hand, heldItem);
            cir.setReturnValue(forwarded.consumesAction() ? forwarded : InteractionResult.SUCCESS);
            return;
        }

        if (!this.fixed && heldItem.is(Items.HONEYCOMB)) {
            heldItem.shrink(1);
            frame.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1.0F, 1.0F);
            frame.level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_WAX_ON, frame.getPos(), 0);

            this.fixed = true;

            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    private static InteractionResult interactFirstTargetBeyondLockedFrame(Player player, ItemFrame frame, InteractionHand hand, ItemStack heldItem) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        double entityRange = player.entityInteractionRange();
        double blockRange = player.blockInteractionRange();

        // Find where the current look ray intersects the lock frame; start just beyond it.
        Vec3 farFromFrame = eye.add(look.scale(blockRange));
        Vec3 start = frame.getBoundingBox()
                .inflate(1.0E-4)
                .clip(eye, farFromFrame)
                .map(hit -> hit.add(look.scale(0.05)))
                .orElseGet(() -> frame.position().add(look.scale(Math.max(frame.getBbWidth(), frame.getBbHeight()) * 0.5 + 0.05)));

        // Block candidate (within block range)
        Vec3 blockEnd = eye.add(look.scale(blockRange));
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                start,
                blockEnd,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        boolean hasBlock = blockHit.getType() == HitResult.Type.BLOCK
                && eye.distanceToSqr(blockHit.getLocation()) <= (blockRange * blockRange);

        // Entity Candidate (within entity range)
        EntityHitResult entityHit = null;
        Vec3 entityEnd = eye.add(look.scale(entityRange));

        // If start is already beyond entity reach, there can be no valid entity target.
        if (eye.distanceToSqr(start) <= (entityRange * entityRange)) {
            AABB searchBox = new AABB(start, entityEnd).inflate(1.0);
            entityHit = ProjectileUtil.getEntityHitResult(
                    player,
                    start,
                    entityEnd,
                    searchBox,
                    entity -> entity.isPickable() && entity != player && entity != frame,
                    entityRange * entityRange
            );
        }

        boolean hasEntity = entityHit != null
                && eye.distanceToSqr(entityHit.getLocation()) <= (entityRange * entityRange);

        double blockDist = hasBlock ? eye.distanceToSqr(blockHit.getLocation()) : Double.POSITIVE_INFINITY;
        double entityDist = hasEntity ? eye.distanceToSqr(entityHit.getLocation()) : Double.POSITIVE_INFINITY;

        // First visible target beyond frame: entity if closer, otherwise block
        if (entityDist < blockDist) {
            Entity target = entityHit.getEntity();
            Vec3 localHit = entityHit.getLocation().subtract(target.position());

            return target.interact(player, hand, localHit);
        }

        if (hasBlock) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = player.level().getBlockState(pos);

            InteractionResult result = state.useWithoutItem(player.level(), player, blockHit);
            if (!result.consumesAction() && !heldItem.isEmpty()) {
                result = state.useItemOn(heldItem, player.level(), player, hand, blockHit);
            }

            return result;
        }

        return InteractionResult.PASS;
    }
}

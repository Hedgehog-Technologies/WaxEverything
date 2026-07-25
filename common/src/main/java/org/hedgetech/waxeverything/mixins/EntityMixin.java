package org.hedgetech.waxeverything.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.hedgetech.waxeverything.WaxEverything;
import org.hedgetech.waxeverything.waxtracking.WaxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "setRemoved", at = @At("RETURN"))
    private void waxeverything$setRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        var entity = (Entity) (Object) this;
        var level = entity.level();
        var entityPos = entity.blockPosition();

        if (level.isClientSide()) return;

        if ((reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)
            && WaxManager.isWaxed(level, entity)
        ) {
            WaxManager.unwax(level, entityPos);
        }
    }
}

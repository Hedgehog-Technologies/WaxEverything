package org.hedgetech.waxeverything.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.waxtracking.ClientWaxRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @WrapOperation(
            method="doAnimateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;trySpawnDripParticles(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/particles/ParticleOptions;Z)V"
            )
    )
    private void waxeverything$preventDropOnWaxed(
            ClientLevel instance,
            BlockPos pos,
            BlockState state,
            ParticleOptions dripParticle,
            boolean isTopSolid,
            Operation<Void> original
    ) {
        var waxed = ClientWaxRegistry.isWaxed(pos);

        if (waxed) {
            Constants.LOG.trace("Waxed Pos: {}", pos);
            return;
        }

        original.call(instance, pos, state, dripParticle, isTopSolid);
    }
}

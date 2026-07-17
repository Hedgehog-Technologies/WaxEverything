package org.hedgetech.waxeverything.mixins.invokers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShelfBlock.class)
public interface ShelfBlockInvoker {
    @Invoker("neighborChanged")
    void waxeverything$invokeNeighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston);
}

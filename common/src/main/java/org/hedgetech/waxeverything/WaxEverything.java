package org.hedgetech.waxeverything;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.hedgetech.waxeverything.saveddata.ClientWaxRegistry;
import org.hedgetech.waxeverything.saveddata.WaxManager;

public class WaxEverything {
    public static boolean isWaxed(Level level, BlockPos pos) {
        return level.isClientSide()
                ? ClientWaxRegistry.isWaxed(pos)
                : WaxManager.isWaxed((ServerLevel) level, pos);
    }
}

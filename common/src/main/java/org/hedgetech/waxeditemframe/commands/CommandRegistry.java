package org.hedgetech.waxeditemframe.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * Command Registry for centralizing command registration
 */
public final class CommandRegistry {
    /**
     * Register commands
     * @param dispatcher Dispatcher for registering commands
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

    }

    private CommandRegistry() { }
}

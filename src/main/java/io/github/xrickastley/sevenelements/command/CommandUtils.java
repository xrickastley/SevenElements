package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandUtils {
	public static int sendError(CommandContext<CommandSourceStack> context, Component text) {
		context
			.getSource()
			.sendFailure(text);

		return 0;
	}

	public static int sendFeedback(CommandContext<CommandSourceStack> context, Component text, boolean broadcastToOps) {
		return sendFeedback(context, text, broadcastToOps, 1);
	}

	public static int sendFeedback(CommandContext<CommandSourceStack> context, Component text, boolean broadcastToOps, int value) {
		context
			.getSource()
			.sendSuccess(() -> text, broadcastToOps);

		return value;
	}

	public static <T> T getOrDefault(CommandContext<CommandSourceStack> context, String name, Class<T> clazz, T fallback) {
		try {
			return context.getArgument(name, clazz);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}
}

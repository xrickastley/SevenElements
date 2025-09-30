package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandUtils {
	public static int sendError(CommandContext<ServerCommandSource> context, Text text) {
		context
			.getSource()
			.sendError(text);

		return 0;
	}

	public static int sendFeedback(CommandContext<ServerCommandSource> context, Text text, boolean broadcastToOps) {
		return sendFeedback(context, text, broadcastToOps, 1);
	}

	public static int sendFeedback(CommandContext<ServerCommandSource> context, Text text, boolean broadcastToOps, int value) {
		context
			.getSource()
			.sendFeedback(() -> text, broadcastToOps);

		return value;
	}

	public static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String name, Class<T> clazz, T fallback) {
		try {
			return context.getArgument(name, clazz);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}
}

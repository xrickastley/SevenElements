package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BossBarCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			CommandManager
				.literal("bossbar")
				.requires(cs -> cs.hasPermissionLevel(2))
				.then(
					CommandManager
						.literal("set")
						.then(
							CommandManager
								.argument("id", IdentifierArgumentType.identifier())
								.suggests(net.minecraft.server.command.BossBarCommand.SUGGESTION_PROVIDER)
								.then(
									CommandManager
										.literal("entity")
										.then(
											CommandManager
												.argument("entity", EntityArgumentType.entity())
												.executes(c -> BossBarCommand.setEntity(c, net.minecraft.server.command.BossBarCommand.getBossBar(c)))
										)
								)
						)
				)
				.then(
					CommandManager
						.literal("get")
						.then(
							CommandManager
								.argument("id", IdentifierArgumentType.identifier())
								.suggests(net.minecraft.server.command.BossBarCommand.SUGGESTION_PROVIDER)
								.then(
									CommandManager
										.literal("entity")
										.executes(c -> BossBarCommand.getEntity(c, net.minecraft.server.command.BossBarCommand.getBossBar(c)))
								)
						)
				)
		);
	}

	private static int setEntity(CommandContext<ServerCommandSource> context, CommandBossBar bossBar) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendError(Text.translatable("commands.element.failed.entity", entity.getDisplayName()).formatted(Formatting.RED));

			return 0;
		}

		bossBar.sevenelements$setEntity(target);

		context
			.getSource()
			.sendFeedback(() -> Text.translatable("commands.bossbar.set.entity.success", bossBar.toHoverableText(), target.getDisplayName()), true);

		return 1;
	}

	private static int getEntity(CommandContext<ServerCommandSource> context, CommandBossBar bossBar) throws CommandSyntaxException {
		final LivingEntity entity = bossBar.sevenelements$getEntity();

		if (entity != null && entity.isDead()) bossBar.sevenelements$setEntity(null);

		context
			.getSource()
			.sendFeedback(
				() -> entity != null
					? Text.translatable("commands.bossbar.get.entity.success", bossBar.toHoverableText(), entity.getDisplayName())
					: Text.translatable("commands.bossbar.get.entity.none", bossBar.toHoverableText()),
				true
			);

		return entity != null ? 1 : 0;
	}
}

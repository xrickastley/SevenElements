package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BossBarCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands
				.literal("bossbar")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(
					Commands
						.literal("set")
						.then(
							Commands
								.argument("id", IdentifierArgument.id())
								.suggests(net.minecraft.server.commands.BossBarCommands.SUGGEST_BOSS_BAR)
								.then(
									Commands
										.literal("entity")
										.then(
											Commands
												.argument("entity", EntityArgument.entity())
												.executes(c -> BossBarCommand.setEntity(c, net.minecraft.server.commands.BossBarCommands.getBossBar(c)))
										)
								)
						)
				)
				.then(
					Commands
						.literal("get")
						.then(
							Commands
								.argument("id", IdentifierArgument.id())
								.suggests(net.minecraft.server.commands.BossBarCommands.SUGGEST_BOSS_BAR)
								.then(
									Commands
										.literal("entity")
										.executes(c -> BossBarCommand.getEntity(c, net.minecraft.server.commands.BossBarCommands.getBossBar(c)))
								)
						)
				)
		);
	}

	private static int setEntity(CommandContext<CommandSourceStack> context, CustomBossEvent bossBar) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity target)) {
			context
				.getSource()
				.sendFailure(Component.translatable("commands.element.failed.entity", entity.getDisplayName()).withStyle(ChatFormatting.RED));

			return 0;
		}

		bossBar.sevenelements$setEntity(target);

		context
			.getSource()
			.sendSuccess(() -> Component.translatable("commands.bossbar.set.entity.success", bossBar.getDisplayName(), target.getDisplayName()), true);

		return 1;
	}

	private static int getEntity(CommandContext<CommandSourceStack> context, CustomBossEvent bossBar) throws CommandSyntaxException {
		final LivingEntity entity = bossBar.sevenelements$getEntity();

		if (entity != null && entity.isDeadOrDying()) bossBar.sevenelements$setEntity(null);

		context
			.getSource()
			.sendSuccess(
				() -> entity != null
					? Component.translatable("commands.bossbar.get.entity.success", bossBar.getDisplayName(), entity.getDisplayName())
					: Component.translatable("commands.bossbar.get.entity.none", bossBar.getDisplayName()),
				true
			);

		return entity != null ? 1 : 0;
	}
}

package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DamageCommand {
	private static final SimpleCommandExceptionType INVULNERABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.damage.invulnerable"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		final CommandNode<ServerCommandSource> elementalDamageNode =
			CommandManager
				.literal("element")
				.then(
					argument("element", ElementArgumentType.element())
					.then(
						argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
						.then(
							argument("tag", InternalCooldownTagType.tag())
							.then(
								argument("type", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
								.executes(context -> execute(context, EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), context.getSource().getWorld().getDamageSources().generic()))
								.then(
									literal("by")
									.then(
										argument("entity", EntityArgumentType.entity())
										.executes(context -> execute(context, EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity"))))
										.then(
											literal("from")
											.then(
												argument("cause", EntityArgumentType.entity())
												.executes(context -> execute(context, EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity"), EntityArgumentType.getEntity(context, "cause"))))
											)
										)
									)
								)
							)
						)
					)
				)
				.build();

		dispatcher
			.getRoot()
			.getChild("damage")
			.getChild("target")
			.getChild("amount")
			.getChild("damageType")
			.addChild(elementalDamageNode);
	}

	private static int execute(CommandContext<ServerCommandSource> context, Entity target, float amount, DamageSource damageSource) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final InternalCooldownTag tag = InternalCooldownTagType.getTag(context, "tag");
		final InternalCooldownType type = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "type", SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE).value();

		if (!(target instanceof final LivingEntity livingTarget))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", target.getDisplayName()));

		final ElementalDamageSource eds = new ElementalDamageSource(
			damageSource,
			ElementalApplications.gaugeUnits(livingTarget, element, gaugeUnits, false),
			InternalCooldownContext.ofType(damageSource.getAttacker(), tag, type)
		).shouldInfuse(false);

		final Text icdText = Text.empty()
			.append(tag.getText())
			.append("/")
			.append(type.getText());

		if (target.damage(eds, amount)) {
			context
				.getSource()
				.sendFeedback(() -> Text.translatable("commands.seven-elements.damage.success", amount, ElementalApplications.gaugeUnits(livingTarget, element, gaugeUnits, false).getText(), icdText, target.getDisplayName()), true);

			return 1;
		} else {
			throw INVULNERABLE_EXCEPTION.create();
		}
	}
}

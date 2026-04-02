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

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DamageCommand {
	private static final SimpleCommandExceptionType INVULNERABLE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
		final CommandNode<CommandSourceStack> elementalDamageNode =
			Commands
				.literal("element")
				.then(
					argument("element", ElementArgumentType.element())
					.then(
						argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
						.then(
							argument("tag", InternalCooldownTagType.tag())
							.then(
								argument("type", ResourceArgument.resource(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
								.executes(context -> execute(context, EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), context.getSource().getLevel().damageSources().generic()))
								.then(
									literal("by")
									.then(
										argument("entity", EntityArgument.entity())
										.executes(context -> execute(context, EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(ResourceArgument.getResource(context, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(context, "entity"))))
										.then(
											literal("from")
											.then(
												argument("cause", EntityArgument.entity())
												.executes(context -> execute(context, EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(ResourceArgument.getResource(context, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(context, "entity"), EntityArgument.getEntity(context, "cause"))))
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

	private static int execute(CommandContext<CommandSourceStack> context, Entity target, float amount, DamageSource damageSource) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final InternalCooldownTag tag = InternalCooldownTagType.getTag(context, "tag");
		final InternalCooldownType type = ResourceArgument.getResource(context, "type", SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE).value();

		if (!(target instanceof final LivingEntity livingTarget))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", target.getDisplayName()));

		final ElementalDamageSource eds = new ElementalDamageSource(
			damageSource,
			ElementalApplications.gaugeUnits(livingTarget, element, gaugeUnits, false),
			InternalCooldownContext.ofType(damageSource.getEntity(), tag, type)
		).shouldInfuse(false);

		final Component icdText = Component.empty()
			.append(tag.getText())
			.append("/")
			.append(type.getText());

		if (target.hurtServer(context.getSource().getLevel(), eds, amount)) {
			context
				.getSource()
				.sendSuccess(() -> Component.translatable("commands.seven-elements.damage.success", amount, ElementalApplications.gaugeUnits(livingTarget, element, gaugeUnits, false).getText(), icdText, target.getDisplayName()), true);

			return 1;
		} else {
			throw INVULNERABLE_EXCEPTION.create();
		}
	}
}

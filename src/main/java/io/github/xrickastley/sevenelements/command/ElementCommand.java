package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistryKeys;
import io.github.xrickastley.sevenelements.util.Array;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ElementCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(
			Commands
				.literal("element")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(
					literal("apply")
					.then(
						argument("target", EntityArgument.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.then(
								argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
								.executes(ElementCommand::applyGaugeUnit)
								.then(
									literal("gaugeUnit")
									.executes(ElementCommand::applyGaugeUnit)
									.then(
										argument("isAura", BoolArgumentType.bool())
										.executes(ElementCommand::applyGaugeUnit)
									)
								)
								.then(
									literal("duration")
									.then(
										argument("duration", IntegerArgumentType.integer(0))
										.executes(ElementCommand::applyDuration)
									)
								)
							)
						)
					)
				)
				.then(
					literal("remove")
					.then(
						argument("target", EntityArgument.entity())
						.executes(ElementCommand::removeAllElements)
						.then(
							argument("element", ElementArgumentType.element())
							.executes(ElementCommand::removeElement)
						)
					)
				)
				.then(
					literal("reduce")
					.then(
						argument("target", EntityArgument.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.then(
								argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
								.executes(ElementCommand::reduceElement)
							)
						)
					)
				)
				.then(
					literal("query")
					.then(
						argument("target", EntityArgument.entity())
						.executes(ElementCommand::queryElements)
						.then(
							argument("element", ElementArgumentType.element())
							.executes(ElementCommand::queryElement)
						)
					)
				)
				.then(
					literal("infusion")
					.then(
						literal("apply")
						.then(
							argument("entity", EntityArgument.entity())
							.then(
								argument("element", ElementArgumentType.element())
								.then(
									argument("gaugeUnits", DoubleArgumentType.doubleArg(0))
									.executes(ElementCommand::infuseGaugeUnit)
									.then(
										literal("gaugeUnit")
										.executes(ElementCommand::infuseGaugeUnit)
										.then(
											argument("tag", InternalCooldownTagType.tag())
											.executes(ElementCommand::infuseGaugeUnit)
											.then(
												argument("type", ResourceArgument.resource(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
												.executes(ElementCommand::infuseGaugeUnit)
											)
										)
									)
									.then(
										literal("duration")
										.then(
											argument("duration", IntegerArgumentType.integer(0))
											.executes(ElementCommand::infuseDuration)
											.then(
												argument("tag", InternalCooldownTagType.tag())
												.executes(ElementCommand::infuseDuration)
												.then(
													argument("type", ResourceArgument.resource(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
													.executes(ElementCommand::infuseDuration)
												)
											)
										)
									)
								)
							)
						)
					)
					.then(
						literal("remove")
						.then(
							argument("entity", EntityArgument.entity())
							.executes(ElementCommand::infuseRemove)
						)
					)
				)
		);
	}

	private static int applyGaugeUnit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final boolean aura = CommandUtils.getOrDefault(context, "isAura", Boolean.class, true);

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementalApplication application = ElementalApplications.gaugeUnits(target, element, gaugeUnits, aura);
		final List<ElementalReaction> reactions = component.addElementalApplication(application, InternalCooldownContext.ofNone());

		return reactions.isEmpty()
			? CommandUtils.sendFeedback(context, Component.translatable("commands.element.apply", application.getText(), entity.getDisplayName()), true)
			: CommandUtils.sendFeedback(context, Component.translatable("commands.element.apply.reactions", application.getText(), entity.getDisplayName(), ComponentUtils.formatList(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Component::literal))), true);
	}

	private static int applyDuration(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementalApplication application = ElementalApplications.duration(target, element, gaugeUnits, duration);
		final List<ElementalReaction> reactions = component.addElementalApplication(application, InternalCooldownContext.ofNone());

		return reactions.isEmpty()
			? CommandUtils.sendFeedback(context, Component.translatable("commands.element.apply", element.getText(true), entity.getDisplayName()), true)
			: CommandUtils.sendFeedback(context, Component.translatable("commands.element.apply.reactions", element.getText(true), entity.getDisplayName(), ComponentUtils.formatList(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Component::literal))), true);
	}

	private static int removeAllElements(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final int removedElements = component
			.getAppliedElements()
			.stream()
			.map(Functions.compose(ElementalApplication::getElement, component::getElementHolder))
			.peek(Functions.withArgument(ElementHolder::setElementalApplication, null))
			// Apparently Stream#count can choose to NOT traverse the Stream elements.
			.collect(Collectors.summingInt(h -> 1));

		ElementComponent.sync(target);

		return removedElements > 0
			? CommandUtils.sendFeedback(context, Component.translatable("commands.element.remove.multiple.success", entity.getDisplayName(), removedElements), true)
			: CommandUtils.sendError(context, Component.translatable("commands.element.remove.multiple.none", entity.getDisplayName()));
	}

	private static int removeElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication())
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.none", entity.getDisplayName(), element.getText(true)));

		holder.setElementalApplication(null);

		ElementComponent.sync(entity);

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.remove", element.getText(true), entity.getDisplayName()), true);
	}

	private static int reduceElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication())
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.none", entity.getDisplayName(), element.getText(true)));

		final double reducedGauge = holder
			.getElementalApplication()
			.reduceGauge(gaugeUnits);

		ElementComponent.sync(entity);

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.reduce", entity.getDisplayName(), element.getText(true), reducedGauge), true);
	}

	private static int queryElements(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final Array<ElementalApplication> appliedElements = component.getAppliedElements();

		if (appliedElements.isEmpty())
			return CommandUtils.sendError(context, Component.translatable("commands.element.query.multiple.none", entity.getDisplayName()));

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.query.multiple.success", entity.getDisplayName(), ComponentUtils.formatList(appliedElements, ElementalApplications::getTimerText)), true);
	}

	private static int queryElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Component.translatable("commands.element.failed.entity", entity.getDisplayName()));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final @Nullable ElementalApplication application = component.getElementHolder(element).getElementalApplication();

		if (application == null)
			return CommandUtils.sendError(context, Component.translatable("commands.element.query.single.none", entity.getDisplayName(), element.getText(true)));

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.query.single.success", entity.getDisplayName(), ElementalApplications.getTimerText(application)), true);
	}

	private static int infuseGaugeUnit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(CommandUtils.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = EntityArgument.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandItem();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.GAUGE_UNIT)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setAsAura(false);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder);

		final Component elementText = ElementalApplication.Builder.getText(infusionBuilder);
		final Component icdText = Component.empty()
			.append(tag.getText(ChatFormatting.WHITE))
			.append("/")
			.append(type.getText());

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.infuse.apply.success", elementText, icdText, entity.getDisplayName()), true);
	}

	private static int infuseDuration(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(CommandUtils.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = EntityArgument.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandItem();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.DURATION)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setDuration(duration);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder);

		final Component elementText = ElementalApplication.Builder.getText(infusionBuilder);
		final Component icdText = Component.empty()
			.append(tag.getText())
			.append("/")
			.append(type.getText());

		return CommandUtils.sendFeedback(context, Component.translatable("commands.element.infuse.apply.success", elementText, icdText, entity.getDisplayName()), true);
	}

	private static int infuseRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final Entity entity = EntityArgument.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandItem();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Component.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		return ElementalInfusionComponent.removeInfusion(stack)
			? CommandUtils.sendFeedback(context, Component.translatable("commands.element.infuse.remove.success", entity.getDisplayName()), true)
			: CommandUtils.sendError(context, Component.translatable("commands.element.infuse.remove.none", entity.getDisplayName()));
	}
}

package io.github.xrickastley.sevenelements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;

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

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ElementCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(
			CommandManager
				.literal("element")
				.requires(cs -> cs.hasPermissionLevel(2))
				.then(
					literal("apply")
					.then(
						argument("target", EntityArgumentType.entity())
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
						argument("target", EntityArgumentType.entity())
						.then(
							argument("element", ElementArgumentType.element())
							.executes(ElementCommand::removeElement)
						)
					)
				)
				.then(
					literal("reduce")
					.then(
						argument("target", EntityArgumentType.entity())
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
						argument("target", EntityArgumentType.entity())
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
							argument("entity", EntityArgumentType.entity())
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
												argument("type", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
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
													argument("type", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, SevenElementsRegistryKeys.INTERNAL_COOLDOWN_TYPE))
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
							argument("entity", EntityArgumentType.entity())
							.executes(ElementCommand::infuseRemove)
						)
					)
				)
		);
	}

	private static int applyGaugeUnit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final boolean aura = CommandUtils.getOrDefault(context, "isAura", Boolean.class, true);

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementalApplication application = ElementalApplications.gaugeUnits(target, element, gaugeUnits, aura);
		final List<ElementalReaction> reactions = component.addElementalApplication(application, InternalCooldownContext.ofNone());

		if (reactions.isEmpty())
			return CommandUtils.sendFeedback(context, Text.translatable("commands.element.apply", application.getText(), entity.getDisplayName()), true);
		else
			return CommandUtils.sendFeedback(context, Text.translatable("commands.element.apply.reactions", application.getText(), entity.getDisplayName(), Texts.join(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Text::literal))), true);
	}

	private static int applyDuration(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementalApplication application = ElementalApplications.duration(target, element, gaugeUnits, duration);
		final List<ElementalReaction> reactions = component.addElementalApplication(application, InternalCooldownContext.ofNone());

		if (reactions.isEmpty())
			return CommandUtils.sendFeedback(context, Text.translatable("commands.element.apply", element.getText(), entity.getDisplayName()), true);
		else
			return CommandUtils.sendFeedback(context, Text.translatable("commands.element.apply.reactions", element.getText(), entity.getDisplayName(), Texts.join(reactions, Functions.compose(ElementalReaction::getId, Identifier::toString, Text::literal))), true);
	}

	private static int removeElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication())
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.none", entity.getDisplayName(), element.getText()));

		holder.setElementalApplication(null);

		ElementComponent.sync(entity);

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.remove", element.getText(), entity), true);
	}

	private static int reduceElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final ElementHolder holder = component.getElementHolder(element);

		if (!holder.hasElementalApplication())
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.none", entity.getDisplayName(), element.getText()));

		final double reducedGauge = holder
			.getElementalApplication()
			.reduceGauge(gaugeUnits);

		ElementComponent.sync(entity);

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.reduce", element.getText(), reducedGauge), true);
	}

	private static int queryElements(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final Array<ElementalApplication> appliedElements = component.getAppliedElements();

		if (appliedElements.isEmpty())
			return CommandUtils.sendError(context, Text.translatable("commands.element.query.multiple.none", entity));

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.query.multiple.success", entity.getDisplayName(), Texts.join(appliedElements, ElementalApplications::getTimerText)), true);
	}

	private static int queryElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "target");
		final Element element = ElementArgumentType.getElement(context, "element");

		if (!(entity instanceof final LivingEntity target))
			return CommandUtils.sendError(context, Text.translatable("commands.element.failed.entity", entity));

		final ElementComponent component = ElementComponent.KEY.get(target);
		final @Nullable ElementalApplication application = component.getElementHolder(element).getElementalApplication();

		if (application == null)
			return CommandUtils.sendError(context, Text.translatable("commands.element.query.single.none", entity.getDisplayName(), element.getText()));

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.query.single.success", entity.getDisplayName(), ElementalApplications.getTimerText(application)), true);
	}

	private static int infuseGaugeUnit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(CommandUtils.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = EntityArgumentType.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.GAUGE_UNIT)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setAsAura(false);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder);

		final Text elementText = ElementalApplication.Builder.getText(infusionBuilder);
		final Text icdText = Text.empty()
			.append(tag.getText(Formatting.WHITE))
			.append("/")
			.append(type.getText());

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.infuse.apply.success", elementText, icdText, entity.getDisplayName()), true);
	}

	private static int infuseDuration(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Element element = ElementArgumentType.getElement(context, "element");
		final double gaugeUnits = DoubleArgumentType.getDouble(context, "gaugeUnits");
		final int duration = IntegerArgumentType.getInteger(context, "duration");
		final InternalCooldownTag tag = InternalCooldownTagType.getTagOrDefault(context, "tag", InternalCooldownTag.NONE);

		final @Nullable Reference<InternalCooldownType> typeRef = ClassInstanceUtil.cast(CommandUtils.getOrDefault(context, "type", Reference.class, null));
		final InternalCooldownType type = JavaScriptUtil.nullishCoalesing(
			ClassInstanceUtil.mapOrNull(typeRef, Reference::value),
			InternalCooldownType.DEFAULT
		);

		final Entity entity = EntityArgumentType.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		final ElementalApplication.Builder infusionBuilder = ElementalApplications.builder()
			.setType(ElementalApplication.Type.DURATION)
			.setElement(element)
			.setGaugeUnits(gaugeUnits)
			.setDuration(duration);

		final InternalCooldownContext.Builder icdBuilder = InternalCooldownContext.builder()
			.setTag(tag)
			.setType(type);

		ElementalInfusionComponent.applyInfusion(stack, infusionBuilder, icdBuilder);

		final Text elementText = ElementalApplication.Builder.getText(infusionBuilder);
		final Text icdText = Text.empty()
			.append(tag.getText())
			.append("/")
			.append(type.getText());

		return CommandUtils.sendFeedback(context, Text.translatable("commands.element.infuse.apply.success", elementText, icdText, entity.getDisplayName()), true);
	}

	private static int infuseRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final Entity entity = EntityArgumentType.getEntity(context, "entity");

		if (!(entity instanceof final LivingEntity livingEntity))
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.entity", entity.getDisplayName()));

		final ItemStack stack = livingEntity.getMainHandStack();

		if (stack.isEmpty())
			return CommandUtils.sendError(context, Text.translatable("commands.enchant.failed.itemless", entity.getDisplayName()));

		return ElementalInfusionComponent.removeInfusion(stack)
			? CommandUtils.sendFeedback(context, Text.translatable("commands.element.infuse.remove.success", entity.getDisplayName()), true)
			: CommandUtils.sendError(context, Text.translatable("commands.element.infuse.remove.none", entity.getDisplayName()));
	}
}

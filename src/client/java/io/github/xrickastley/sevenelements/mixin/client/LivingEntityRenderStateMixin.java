package io.github.xrickastley.sevenelements.mixin.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.FrozenEffectComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.interfaces.SevenElementsLivingEntityRenderState;
import io.github.xrickastley.sevenelements.renderer.genshin.ElementRenderer;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements SevenElementsLivingEntityRenderState {
	public boolean sevenelements$isDead;
	public boolean sevenelements$isClientPlayer;
	public Vec3 sevenelements$boundingBoxLength;
	public boolean sevenelements$isFrozen;
	public @Nullable List<ElementRenderer.ElementState> sevenelements$elementStates;
	public @Nullable List<ElementRenderer.ElementGaugeState> sevenelements$gaugeStates;
	public @Nullable Element sevenelements$crystallizeShieldElement;

	@Unique
	@Override
	public void sevenelements$fillRenderState(LivingEntity entity, float tickDelta) {
		final Minecraft client = Minecraft.getInstance();

		final AABB box = entity.getBoundingBox();

		this.sevenelements$isDead = entity.isDeadOrDying();
		this.sevenelements$isClientPlayer = entity == client.player;
		this.sevenelements$boundingBoxLength = new Vec3(box.getXsize(), box.getYsize(), box.getZsize());
		this.sevenelements$fillElementRenderState(entity, tickDelta);
		this.sevenelements$fillFrozenEffectRenderState(entity);
	}

	@Unique
	@Override
	public boolean sevenelements$isDead() {
		return this.sevenelements$isDead;
	}

	@Unique
	@Override
	public boolean sevenelements$isClientPlayer() {
		return this.sevenelements$isClientPlayer;
	}

	@Unique
	@Override
	public boolean sevenelements$isFrozen() {
		return this.sevenelements$isFrozen;
	}

	@Unique
	@Override
	public Vec3 sevenelements$getBoundingBoxLength() {
		return this.sevenelements$boundingBoxLength;
	}

	@Unique
	@Override
	public List<ElementRenderer.ElementState> sevenelements$getElementStates() {
		return this.sevenelements$elementStates == null
			? Collections.emptyList()
			: Collections.unmodifiableList(this.sevenelements$elementStates);
	}

	@Unique
	@Override
	public List<ElementRenderer.ElementGaugeState> sevenelements$getGaugeStates() {
		return this.sevenelements$elementStates == null
			? Collections.emptyList()
			: Collections.unmodifiableList(this.sevenelements$gaugeStates);
	}

	@Unique
	@Override
	public @Nullable Element sevenelements$getCrystallizeShieldElement() {
		return this.sevenelements$crystallizeShieldElement;
	}

	@Unique
	private void sevenelements$fillElementRenderState(LivingEntity entity, float tickDelta) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		this.sevenelements$crystallizeShieldElement = Optional.ofNullable(component.getCrystallizeShield())
			.map(Tuple::getA)
			.orElse(null);



		this.sevenelements$gaugeStates = component
			.getAppliedElements()
			.stream()
			.sorted(Comparator.comparingInt(application -> application.getElement().getPriority()))
			.map(Functions.withArgument(ElementRenderer::gaugeState, tickDelta))
			.toList();



		this.sevenelements$elementStates = new ArrayList<>();

		if (component.hasValidLastReaction()) {
			final ElementalReaction reaction = component.getLastReaction().getA();
			final long reactionAt = component.getLastReaction().getB();

			reaction
				.getReactionDisplayOrder()
				.forEach(element -> this.sevenelements$elementStates.add(new ElementRenderer.ElementState(element, 60.0, reactionAt, tickDelta)));
		} else {
			if (component.getAppliedElements().isEmpty()) return;

			final Optional<Integer> priority = component.getHighestElementPriority();

			if (priority.isEmpty()) return;

			this.sevenelements$elementStates.addAll(
				component
					.getAppliedElements()
					.filter(application -> application.getElement().getPriority() == priority.get())
					.map(Functions.withArgument(ElementRenderer::elementState, tickDelta))
			);
		}

		this.sevenelements$elementStates = this.sevenelements$elementStates
			.stream()
			.filter(Util.distinctKeyed(Functions.compose(ElementRenderer.ElementState::element, Element::getTexture)))
			.toList();
	}

	@Unique
	private void sevenelements$fillFrozenEffectRenderState(LivingEntity entity) {
		final FrozenEffectComponent component = FrozenEffectComponent.KEY.get(entity);

		this.sevenelements$isFrozen = component.isFrozen();
	}
}

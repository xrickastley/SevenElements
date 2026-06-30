package io.github.xrickastley.sevenelements.mixin.priority;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.effect.ElementalStatusEffect;
import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReactions;
import io.github.xrickastley.sevenelements.element.reaction.base.DamageModifyingReaction.Phase;
import io.github.xrickastley.sevenelements.element.reaction.base.DamageModifyingReaction;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.FilteredIterator;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = Integer.MIN_VALUE)
public abstract class LivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	@Shadow
	protected float lastDamageTaken;
	@Shadow
	@Final
	private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects;
	@Shadow
	protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

	@Shadow
	public abstract boolean isDead();

	@Shadow
	public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

	@Unique
	private List<ElementalReaction> sevenelements$reactions = new ArrayList<>();
	@Unique
	private @Nullable Entity sevenelements$plannedAttacker;
	@Unique
	private @Nullable DamageSource sevenelements$plannedDamageSource;

	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Unique
	@Override
	public @Nullable Entity sevenelements$getPlannedAttacker() {
		return this.sevenelements$plannedAttacker;
	}

	@Unique
	@Override
	public @Nullable DamageSource sevenelements$getPlannedDamageSource() {
		return this.sevenelements$plannedDamageSource;
	}

	@Inject(
		method = "travelControlled",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsEntityControl(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfo ci) {
		if (this.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "canHaveStatusEffect",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void forceElementEffects(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
		if (ElementalStatusEffect.isElementalEffect(effect.getEffectType())) cir.setReturnValue(true);
	}

	@Inject(
		method = "removeStatusEffectInternal",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void preventElementEffectRemoval(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
		if (this.isDead() || !ElementalStatusEffect.isElementalEffect(effect)) return;

		final ElementalStatusEffect elementEffect = (ElementalStatusEffect) effect.value();
		final ElementComponent component = ElementComponent.KEY.get(this);

		if (component.hasElementalApplication(elementEffect.getElement())) cir.setReturnValue(null);
	}

	@ModifyVariable(
		method = "clearStatusEffects",
		at = @At("STORE"),
		ordinal = 0,
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private Iterator<StatusEffectInstance> persistElementEffectsOnClear(Iterator<StatusEffectInstance> value) {
		if (this.isDead()) return value;

		final ElementComponent component = ElementComponent.KEY.get(this);

		return FilteredIterator.of(value,
			v -> ElementalStatusEffect
				.asElementEffect(v.getEffectType())
				.map(Functions.compose(ElementalStatusEffect::getElement, component::hasElementalApplication, b -> !b))
				.orElse(false)
		);
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		order = Integer.MIN_VALUE // The planned attacker should be set as early as possible.
	)
	private void setPlannedAttacker(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.sevenelements$plannedAttacker = source.getAttacker();
		this.sevenelements$plannedDamageSource = source;
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** prevent the entity from acting.
	)
	private void preventDamageWhenFrozen(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getAttacker() instanceof final LivingEntity entity && entity.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			cir.setReturnValue(false);
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Infusions need to be considered before other DMG effects.
	)
	private DamageSource applyElementalInfusions(DamageSource source) {
		return ElementComponent.applyElementalInfusions(source, ClassInstanceUtil.cast(this));
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier, should be applied ASAP.
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		// do **not** apply an element **if** DMG cannot be applied.
		if (
			this.isInvulnerableTo(source)
			|| this.getWorld().isClient
			|| this.isDead()
			|| (source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
			|| this.timeUntilRegen > 10.0F && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN) && amount <= this.lastDamageTaken
		) return amount;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(ClassInstanceUtil.cast(this), Element.PHYSICAL, 0.00), InternalCooldownContext.ofNone(source.getAttacker()));

		final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(this);
		this.sevenelements$reactions = new ArrayList<>(component.applyFromDamageSource(eds));

		final @Nullable ElementalReaction lastReaction = this.sevenelements$reactions.isEmpty()
			? null
			: this.sevenelements$reactions.getLast();

		final boolean doShatter = !this.sevenelements$reactions.contains(ElementalReactions.GEO_SHATTER)
			&& !this.sevenelements$reactions.contains(ElementalReactions.SHATTER)
			&& ElementalReactions.SHATTER.isTriggerable(this)
			&& (lastReaction == null || !lastReaction.preventsReaction(ElementalReactions.SHATTER));

		if (doShatter) {
			this.sevenelements$reactions.add(ElementalReactions.SHATTER);

			component.setLastReaction(new Pair<>(ElementalReactions.SHATTER, this.getWorld().getTime()));

			ElementalReactions.SHATTER.trigger(ClassInstanceUtil.cast(this), ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class));
		}

		amount = this.sevenelements$reactions
			.stream()
			.<DamageModifyingReaction>mapMulti((reaction, mapper) ->
				ClassInstanceUtil.ifInstanceOfAnd(reaction, DamageModifyingReaction.class, Functions.composePredicate(DamageModifyingReaction::getPhase, Phase.BASE::equals), mapper)
			)
			.reduce(amount, (acc, reaction) -> reaction.modifyDamage(source.getAttacker(), this.getWorld(), acc), Float::sum);

		return SevenElementsAttributes.modifyDamage(ClassInstanceUtil.cast(this), eds, amount);
	}

	@ModifyVariable(
		method = "modifyAppliedDamage",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),
		argsOnly = true,
		order = Integer.MAX_VALUE // Amplifying DMG Bonus is a Total DMG multiplier, should be applied as late as possible. Here due to keeping sevenelements$reactions private.
	)
	private float applyReactionAmplifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		return this.sevenelements$reactions
			.stream()
			.<DamageModifyingReaction>mapMulti((reaction, mapper) ->
				// Yes this does make multiple amplifying reactions multiplicative instead of additive with each other, but that's unknown information at this point (and unused) so it won't really matter implementation wise.
				ClassInstanceUtil.ifInstanceOfAnd(reaction, DamageModifyingReaction.class, Functions.composePredicate(DamageModifyingReaction::getPhase, Phase.TOTAL::equals), mapper)
			)
			.reduce(amount, (acc, reaction) -> reaction.modifyDamage(source.getAttacker(), this.getWorld(), acc), Float::sum);
	}
}

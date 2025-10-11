package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
import io.github.xrickastley.sevenelements.element.reaction.AdditiveElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.AmplifyingElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReactions;
import io.github.xrickastley.sevenelements.factory.SevenElementsAttributes;
import io.github.xrickastley.sevenelements.interfaces.ILivingEntity;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = Integer.MAX_VALUE - 1000)
public abstract class PrioritizedLivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	@Shadow
	protected float lastDamageTaken;
	@Shadow
	@Final
	private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects;

	@Shadow
	public abstract boolean isDead();

	@Shadow
	public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

	@Shadow
	public abstract boolean removeStatusEffect(RegistryEntry<StatusEffect> effect);

	@Unique
	private List<ElementalReaction> sevenelements$reactions = new ArrayList<>();
	@Unique
	private @Nullable Entity sevenelements$plannedAttacker;
	@Unique
	private @Nullable DamageSource sevenelements$plannedDamageSource;

	public PrioritizedLivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
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

	@Inject(
		method = "onDeath",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"
		)
	)
	private void removeForcedEffectsOnDeath(DamageSource damageSource, CallbackInfo ci) {
		ElementalStatusEffect
			.getElementEffects()
			.forEach(this::removeStatusEffect);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeExpiredElementEffects(CallbackInfo ci) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		ElementalStatusEffect
			.getElementEffects()
			.stream()
			.filter(this::hasStatusEffect)
			.filter(Predicate.not(
				Functions.composePredicate(RegistryEntry::value, ElementalStatusEffect.class::cast, ElementalStatusEffect::getElement, component::hasElementalApplication)
			))
			.forEach(this::removeStatusEffect);
	}

	@ModifyExpressionValue(
		method = "clearStatusEffects",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;",
			remap = false
		)
	)
	// Frozen and Cryo **must** persist while their respective elements are applied.
	private HashMap<RegistryEntry<StatusEffect>, StatusEffectInstance> persistElementEffectsOnClearStart(HashMap<RegistryEntry<StatusEffect>, StatusEffectInstance> value, @Share(value = "savedElements", namespace = "seven-elements") LocalRef<List<StatusEffectInstance>> savedEffects) {
		if (this.isDead()) return value;

		final ElementComponent component = ElementComponent.KEY.get(this);

		savedEffects.set(new ArrayList<>());

		value
			.keySet()
			.stream()
			.filter(
				e -> ElementalStatusEffect
					.asElementEffect(e)
					.map(Functions.compose(ElementalStatusEffect::getElement, component::hasElementalApplication, b -> !b))
					.orElse(false)
			)
			.peek(Functions.composeConsumer(value::get, savedEffects.get()::add))
			.forEach(value::remove);

		return value;
	}

	@Inject(
		method = "clearStatusEffects",
		at = @At(
			value = "RETURN",
			ordinal = 2
		),
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void persistElementEffectsOnClearEnd(CallbackInfoReturnable<Boolean> cir, @Share(value = "savedElements", namespace = "seven-elements") LocalRef<List<StatusEffectInstance>> savedEffects) {
		for (final StatusEffectInstance effect : savedEffects.get())
			this.activeStatusEffects.put(effect.getEffectType(), effect);
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		order = Integer.MIN_VALUE // The planned attacker should be set as early as possible.
	)
	private void setPlannedAttacker(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.sevenelements$plannedAttacker = source.getAttacker();
		this.sevenelements$plannedDamageSource = source;
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE
	)
	private void preventDamageWhenFrozen(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
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
		return ElementComponent.applyElementalInfusions(source, (LivingEntity)(Entity) this);
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier, should be applied ASAP.
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source, @Local(argsOnly = true) ServerWorld world) {
		final boolean fireResistance = source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE);
		final boolean damageCooldown = this.timeUntilRegen > 10.0F && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN) && amount <= this.lastDamageTaken;

		// do **not** apply an element **if** DMG cannot be applied.
		if (this.isInvulnerable() || this.getWorld().isClient || this.isDead() || fireResistance || damageCooldown) return amount;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0.00), InternalCooldownContext.ofNone(source.getAttacker()));

		final ElementComponent component = ElementComponent.KEY.get(this);
		this.sevenelements$reactions = new ArrayList<>(component.applyFromDamageSource(eds));

		final @Nullable ElementalReaction lastReaction = this.sevenelements$reactions.isEmpty()
			? null
			: this.sevenelements$reactions.get(this.sevenelements$reactions.size() - 1);

		final boolean doShatter = !this.sevenelements$reactions.contains(ElementalReactions.GEO_SHATTER)
			&& !this.sevenelements$reactions.contains(ElementalReactions.SHATTER)
			&& ElementalReactions.SHATTER.isTriggerable(this)
			&& (lastReaction == null || !lastReaction.preventsReaction(ElementalReactions.SHATTER));

		if (doShatter) {
			this.sevenelements$reactions.add(ElementalReactions.SHATTER);
			((ElementComponentImpl) component).setLastReaction(new Pair<>(ElementalReactions.SHATTER, this.getWorld().getTime()));

			ElementalReactions.SHATTER.trigger((LivingEntity)(Entity) this, ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class));
		}

		float additive = this.sevenelements$reactions != null && !this.sevenelements$reactions.isEmpty()
			? Math.max(
				this.sevenelements$reactions
					.stream()
					.filter(reaction -> reaction instanceof AdditiveElementalReaction)
					.map(reaction -> ((AdditiveElementalReaction) reaction))
					.reduce(0.0f, (acc, reaction) -> acc + (float) reaction.getDamageBonus(world), Float::sum),
				0.0f
			)
			: 0.0f;

		return SevenElementsAttributes.modifyDamage((LivingEntity)(Entity) this, eds, amount + additive);
	}

	@ModifyVariable(
		method = "modifyAppliedDamage",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),
		argsOnly = true,
		order = Integer.MAX_VALUE // Amplifying DMG Bonus is a Total DMG multiplier, should be applied as late as possible.
	)
	private float applyReactionAmplifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		double amplifier = this.sevenelements$reactions != null && !this.sevenelements$reactions.isEmpty()
			? Math.max(
				this.sevenelements$reactions
					.stream()
					.filter(reaction -> reaction instanceof AmplifyingElementalReaction)
					.map(reaction -> ((AmplifyingElementalReaction) reaction))
					.reduce(0.0, (acc, reaction) -> acc + reaction.getAmplifier(), Double::sum),
				1.0
			)
			: 1.0;

		return amount * (float) amplifier;
	}
}

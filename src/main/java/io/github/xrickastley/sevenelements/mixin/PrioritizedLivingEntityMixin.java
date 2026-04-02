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
import io.github.xrickastley.sevenelements.element.ElementHolder;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
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

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(value = LivingEntity.class, priority = Integer.MAX_VALUE - 1000)
public abstract class PrioritizedLivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	@Shadow
	protected float lastHurt;
	@Shadow
	@Final
	private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

	@Shadow
	public abstract boolean isDeadOrDying();

	@Shadow
	public abstract boolean hasEffect(Holder<MobEffect> effect);

	@Shadow
	public abstract boolean removeEffect(Holder<MobEffect> effect);

	@Unique
	private List<ElementalReaction> sevenelements$reactions = new ArrayList<>();
	@Unique
	private @Nullable Entity sevenelements$plannedAttacker;
	@Unique
	private @Nullable DamageSource sevenelements$plannedDamageSource;

	public PrioritizedLivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final Level world) {
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
		method = "canBeAffected",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void forceElementEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
		if (ElementalStatusEffect.isElementalEffect(effect.getEffect())) cir.setReturnValue(true);
	}

	@Inject(
		method = "removeEffectNoUpdate",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void preventElementEffectRemoval(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
		if (this.isDeadOrDying() || !ElementalStatusEffect.isElementalEffect(effect)) return;

		final ElementalStatusEffect elementEffect = (ElementalStatusEffect) effect.value();
		final ElementComponent component = ElementComponent.KEY.get(this);

		if (component.hasElementalApplication(elementEffect.getElement())) cir.setReturnValue(null);
	}

	@Inject(
		method = "die",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setPose(Lnet/minecraft/world/entity/Pose;)V"
		)
	)
	private void applyOnDeathEffects(DamageSource damageSource, CallbackInfo ci) {
		ElementalStatusEffect
			.getElementEffects()
			.forEach(this::removeEffect);

		final ElementComponent component = ElementComponent.KEY.get(this);

		component
			.getAppliedElements()
			.stream()
			.map(Functions.compose(ElementalApplication::getElement, component::getElementHolder))
			.forEach(ElementHolder::reset);
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
			.filter(this::hasEffect)
			.filter(Predicate.not(
				Functions.composePredicate(Holder::value, ElementalStatusEffect.class::cast, ElementalStatusEffect::getElement, component::hasElementalApplication)
			))
			.forEach(this::removeEffect);
	}

	@ModifyExpressionValue(
		method = "removeAllEffects",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;",
			remap = false
		)
	)
	// Frozen and Cryo **must** persist while their respective elements are applied.
	private HashMap<Holder<MobEffect>, MobEffectInstance> persistElementEffectsOnClearStart(HashMap<Holder<MobEffect>, MobEffectInstance> value, @Share(value = "savedElements", namespace = "seven-elements") LocalRef<List<MobEffectInstance>> savedEffects) {
		if (this.isDeadOrDying()) return value;

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
		method = "removeAllEffects",
		at = @At(
			value = "RETURN",
			ordinal = 2
		),
		order = Integer.MIN_VALUE // Frozen and Cryo **must** persist while their respective elements are applied.
	)
	private void persistElementEffectsOnClearEnd(CallbackInfoReturnable<Boolean> cir, @Share(value = "savedElements", namespace = "seven-elements") LocalRef<List<MobEffectInstance>> savedEffects) {
		for (final MobEffectInstance effect : savedEffects.get())
			this.activeEffects.put(effect.getEffect(), effect);
	}

	@Inject(
		method = "hurtServer",
		at = @At("HEAD"),
		order = Integer.MIN_VALUE // The planned attacker should be set as early as possible.
	)
	private void setPlannedAttacker(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.sevenelements$plannedAttacker = source.getEntity();
		this.sevenelements$plannedDamageSource = source;
	}

	@Inject(
		method = "hurtServer",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE
	)
	private void preventDamageWhenFrozen(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getEntity() instanceof final LivingEntity entity && entity.hasEffect(SevenElementsStatusEffects.FROZEN))
			cir.setReturnValue(false);
	}

	@ModifyVariable(
		method = "hurtServer",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Infusions need to be considered before other DMG effects.
	)
	private DamageSource applyElementalInfusions(DamageSource source) {
		return ElementComponent.applyElementalInfusions(source, (LivingEntity)(Entity) this);
	}

	@ModifyVariable(
		method = "hurtServer",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier, should be applied ASAP.
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source, @Local(argsOnly = true) ServerLevel world) {
		final boolean fireResistance = source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE);
		final boolean damageCooldown = this.invulnerableTime > 10.0F && !source.is(DamageTypeTags.BYPASSES_COOLDOWN) && amount <= this.lastHurt;

		// do **not** apply an element **if** DMG cannot be applied.
		if (this.isInvulnerable() || this.level().isClientSide() || this.isDeadOrDying() || fireResistance || damageCooldown) return amount;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0.00), InternalCooldownContext.ofNone(source.getEntity()));

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
			((ElementComponentImpl) component).setLastReaction(new Tuple<>(ElementalReactions.SHATTER, this.level().getGameTime()));

			ElementalReactions.SHATTER.trigger((LivingEntity)(Entity) this, ClassInstanceUtil.castOrNull(source.getEntity(), LivingEntity.class));
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
		method = "getDamageAfterMagicAbsorb",
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

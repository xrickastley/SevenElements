package io.github.xrickastley.sevenelements.mixin.priority;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(value = LivingEntity.class, priority = Integer.MIN_VALUE)
public abstract class LivingEntityMixin
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
	public abstract boolean isInvulnerableTo(ServerLevel world, DamageSource source);

	@Unique
	private List<ElementalReaction> sevenelements$reactions = new ArrayList<>();
	@Unique
	private @Nullable Entity sevenelements$plannedAttacker;
	@Unique
	private @Nullable DamageSource sevenelements$plannedDamageSource;

	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final Level world) {
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
		method = "travelRidden",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsEntityControl(Player controllingPlayer, Vec3 movementInput, CallbackInfo ci) {
		if (this.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
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
		order = Integer.MIN_VALUE // Frozen **must** prevent the entity from acting.
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
		return ElementComponent.applyElementalInfusions(source, ClassInstanceUtil.cast(this));
	}

	@ModifyVariable(
		method = "hurtServer",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier, should be applied ASAP.
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) DamageSource source) {
		// do **not** apply an element **if** DMG cannot be applied.
		if (
			this.isInvulnerableTo(world, source)
			|| this.level().isClientSide()
			|| this.isDeadOrDying()
			|| (source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE))
			|| this.invulnerableTime > 10.0F && !source.is(DamageTypeTags.BYPASSES_COOLDOWN) && amount <= this.lastHurt
		) return amount;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits(ClassInstanceUtil.cast(this), Element.PHYSICAL, 0.00), InternalCooldownContext.ofNone(source.getEntity()));

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

			component.setLastReaction(new Tuple<>(ElementalReactions.SHATTER, this.level().getGameTime()));

			ElementalReactions.SHATTER.trigger(ClassInstanceUtil.cast(this), ClassInstanceUtil.castOrNull(source.getEntity(), LivingEntity.class));
		}

		amount = this.sevenelements$reactions
			.stream()
			.<DamageModifyingReaction>mapMulti((reaction, mapper) ->
				ClassInstanceUtil.ifInstanceOfAnd(reaction, DamageModifyingReaction.class, Functions.composePredicate(DamageModifyingReaction::getPhase, Phase.BASE::equals), mapper)
			)
			.reduce(amount, (acc, reaction) -> reaction.modifyDamage(source.getEntity(), world, acc), Float::sum);

		return SevenElementsAttributes.modifyDamage(ClassInstanceUtil.cast(this), eds, amount);
	}

	@ModifyVariable(
		method = "getDamageAfterMagicAbsorb",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),
		argsOnly = true,
		order = Integer.MAX_VALUE // Amplifying DMG Bonus is a Total DMG multiplier, should be applied as late as possible. Here due to keeping sevenelements$reactions private.
	)
	private float applyReactionAmplifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		return this.level() instanceof final ServerLevel world
			? this.sevenelements$reactions
				.stream()
				.<DamageModifyingReaction>mapMulti((reaction, mapper) ->
					// Yes this does make multiple amplifying reactions multiplicative instead of additive with each other, but that's unknown information at this point (and unused) so it won't really matter implementation wise.
					ClassInstanceUtil.ifInstanceOfAnd(reaction, DamageModifyingReaction.class, Functions.composePredicate(DamageModifyingReaction::getPhase, Phase.TOTAL::equals), mapper)
				)
				.reduce(amount, (acc, reaction) -> reaction.modifyDamage(source.getEntity(), world, acc), Float::sum)
			: amount;
	}
}

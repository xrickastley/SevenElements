package io.github.xrickastley.sevenelements.element;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.interfaces.DamageSourceWrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

/**
 * A version of {@link ElementalDamageSource} used when the target can't quite be determined
 * during the creation of the {@link DamageSource}. <br> <br>
 *
 * When a {@code PartialElementalDamageSource} is dealt to an entity, it is resolved via the
 * {@link ElementComponentImpl}
 */
public final class PartialElementalDamageSource
	extends DamageSource
	implements DamageSourceWrapper
{
	private final @Nullable DamageSource original;
	private final ElementalApplication.Builder application;
	private final InternalCooldownContext icdContext;
	private boolean applyDMGBonus = true;
	private boolean applyRES = true;
	private boolean shouldInfuse = true;

	/**
	 * Creates an {@link PartialElementalDamageSource} from an already existing {@link DamageSource}.
	 * @param source The {@code DamageSource} to turn into an {@code PartialElementalDamageSource}, using
	 * its source and attacker values. For positions, use {@link #PartialElementalDamageSource(RegistryEntry, Vec3d, ElementalApplication.Builder, InternalCooldownContext)} instead.
	 * @param application The Elemental Application of this {@code PartialElementalDamageSource}. This is
	 * the Elemental Application that will be applied to the target entity, if possible.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code PartialElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public PartialElementalDamageSource(final DamageSource source, final ElementalApplication.Builder application, final InternalCooldownContext icdContext) {
		super(source.getTypeRegistryEntry(), source.getSource(), source.getAttacker());

		this.original = source;
		this.application = application;
		this.icdContext = icdContext;
	}

	/**
	 * Creates an {@link PartialElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param source The source entity of this {@code DamageSource}. This is the entity that dealt
	 * the DMG. (ex. arrow, fireball)
	 * @param attacker The attacker this {@code DamageSource} originated from. This is the entity
	 * that attacked. (ex. Skeleton, Ghast)
	 * @param application The Elemental Application of this {@code PartialElementalDamageSource}. This is
	 * the Elemental Application that will be applied to the target entity, if possible.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code PartialElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public PartialElementalDamageSource(final RegistryEntry<DamageType> type, @Nullable final Entity source, @Nullable final Entity attacker, final ElementalApplication.Builder application, final InternalCooldownContext icdContext) {
		super(type, source, attacker);

		this.original = null;
		this.application = application;
		this.icdContext = icdContext;
	}

	/**
	 * Creates an {@link PartialElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param position The position this {@code DamageSource} originated from.
	 * @param application The Elemental Application of this {@code PartialElementalDamageSource}. This is
	 * the Elemental Application that will be applied to the target entity, if possible.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code PartialElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public PartialElementalDamageSource(final RegistryEntry<DamageType> type, final Vec3d position, final ElementalApplication.Builder application, final InternalCooldownContext icdContext) {
		super(type, position);

		this.original = null;
		this.application = application;
		this.icdContext = icdContext;
	}

	/**
	 * Creates an {@link PartialElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param attacker The attacker this {@code DamageSource} originated from. This is the entity
	 * that attacked. (ex. Zombie, Creeper)
	 * @param application The Elemental Application of this {@code PartialElementalDamageSource}. This is
	 * the Elemental Application that will be applied to the target entity, if possible.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code PartialElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public PartialElementalDamageSource(final RegistryEntry<DamageType> type, @Nullable final Entity attacker, final ElementalApplication.Builder application, final InternalCooldownContext icdContext) {
		super(type, attacker, attacker);

		this.original = null;
		this.application = application;
		this.icdContext = icdContext;
	}

	/**
	 * Creates an {@link PartialElementalDamageSource}.
	 * @param type The damage type of this {@code DamageSource}.
	 * @param application The Elemental Application of this {@code PartialElementalDamageSource}. This is
	 * the Elemental Application that will be applied to the target entity, if possible.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code PartialElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public PartialElementalDamageSource(final RegistryEntry<DamageType> type, final ElementalApplication.Builder application, final InternalCooldownContext icdContext) {
		super(type);

		this.original = null;
		this.application = application;
		this.icdContext = icdContext;
	}

	/**
	 * Sets whether the Elemental DMG Bonus% should be included in the DMG calculation for this
	 * {@code PartialElementalDamageSource}.
	 */
	public PartialElementalDamageSource shouldApplyDMGBonus(boolean dmgBonus) {
		this.applyDMGBonus = dmgBonus;

		return this;
	}

	/**
	 * Sets whether the Elemental RES% should be included in the DMG calculation for this
	 * {@code PartialElementalDamageSource}.
	 */
	public PartialElementalDamageSource shouldApplyRES(boolean res) {
		this.applyRES = res;

		return this;
	}

	/**
	 * Sets whether this {@code PartialElementalDamageSource} should be infusable with another Element,
	 * given that its Element is of the {@link Element#PHYSICAL} element.
	 */
	public PartialElementalDamageSource shouldInfuse(boolean infusion) {
		this.shouldInfuse = infusion;

		return this;
	}

	public ElementalApplication.Builder getElementalApplication() {
		return this.application;
	}

	public InternalCooldownContext getIcdContext() {
		return this.icdContext;
	}

	/**
	 * Returns the {@code DamageSource} that was used to create this {@code PartialElementalDamageSource},
	 * or {@code null} if a {@code DamageSource} wasn't used.
	 */
	@Override
	public @Nullable DamageSource getOriginalSource() {
		return this.original;
	}

	public boolean applyDMGBonus() {
		return this.applyDMGBonus;
	}

	public boolean applyRES() {
		return this.applyRES;
	}

	public boolean shouldInfuse() {
		return this.shouldInfuse;
	}

	public ElementalDamageSource resolve(LivingEntity target) {
		return new ElementalDamageSource(
			this.original,
			this.application.build(target),
			this.icdContext
		);
	}
}

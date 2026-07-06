# Developer Changelog - Version 1.1

This page serves as the changelog for Seven Elements v1.1 from v1.0, to be mainly used by addon developers or for reference.

Unlike the standard [changelog](../changelog.md), this page contains a more in-depth view of major code changes and migration suggestions.

## Elemental Reactions and Elemental Mastery

With the introduction of the Elemental Mastery attribute, Elemental Reactions no longer rely on the static helper method `ElementalReaction.getReactionDamage()`, as damage will now differ for each entity triggering the reaction, the reaction itself, and whether an entity triggered the reaction or not.

Instead, call `ElementalReaction#getReactionStrength()` or one of its overloads and use its result as the amount of damage dealt. To set the reaction's `reactionMultiplier` from the previous `getReactionDamage()` method, call `ElementalReaction.Settings#setReactionMultiplier()` with the value of `reactionMultiplier`.

In addition to this, how Elemental Mastery affects the value of `getReactionStrength()` is determined by the reaction's type, which defaults to `Type.TRANSFORMATIVE`. `AdditiveElementalReaction` sets this to `Type.ADDITIVE`, while `AmplifyingElementalReaction` sets this to `Type.AMPLIFYING`.

If you have a non-transformative reaction that doesn't extend `AdditiveElementalReaction` or `AmplifyingElementalReaction`, set the reaction's type in its settings via `ElementalReaction.Settings#setType()` for Elemental Mastery calculations to apply correctly.

#### Example

```java
public final class RimegrassElementalReaction extends ElementalReaction {
	public RimegrassElementalReaction() {
		super(
			new ElementalReaction.Settings("Rimegrass", Identifier.of("tutorial", "rimegrass"), TextHelper.reaction("reaction.tutorial.rimegrass", "#c6f7b4"))
				.setType(Type.TRANSFORMATIVE) // [!code ++]
				.setReactionCoefficient(1.0)
				.setReactionMultiplier(2.25) // [!code ++]
				.setAuraElement(Element.CRYO, 5)
				.setTriggeringElement(Element.DENDRO, 5)
				.reversable(true)
		);
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final float damage = ElementalReaction.getReactionDamage(entity, 2.25) // [!code --]
		final float damage = this.getReactionStrength(origin, entity.getWorld()) // [!code ++]
		final ElementalDamageSource source = new ElementalDamageSource(
			entity
				.getDamageSources()
				.create(TutorialDamageTypes.RIMEGRASS, entity, origin),
			ElementalApplications.gaugeUnits(entity, Element.CRYO, 0),
			InternalCooldownContext.ofNone(origin)
		).shouldApplyDMGBonus(false);

		entity.damage(source, damage);
	}
}
```

### Additive and Amplifying Reactions

For reactions that extend `AdditiveElementalReaction` or `AmplifyingElementalReaction`, the previous constructor is still available, but will be removed in the next minor version of Seven Elements. Like the previous change, set the reaction's `amplifier` via `setReactionMultiplier()` with the value of `amplifier` from the deprecated constructor.

#### Example

```java
public final class FrostflashElementalReaction extends AmplifyingElementalReaction {
	public FrostflashElementalReaction() {
		super(
			new ElementalReaction.Settings("Frostflash", Identifier.of("tutorial", "frostflash"), TextHelper.reaction("reaction.tutorial.frostflash", "#c6f7b4"))
				.setReactionCoefficient(1.0)
				.setReactionMultiplier(1.75) // [!code ++]
				.setAuraElement(Element.FREEZE)
				.setTriggeringElement(Element.PHYSICAL, 0)
				.reversable(true),
			1.75 // [!code --]
		);
	}
}
```

## Infusable projectiles

Projectiles can now apply their own elemental infusion if the item shooting the projectile doesn't have any elemental infusion.

Seven Elements handles this automatically, but if you have custom projectile handling, or your custom projectiles aren't automatically covered by Seven Elements, you may set the projectile stack via `ProjectileEntity#sevenelements$setProjectileStack()`.

## True item names

Seven Elements will modify the item's name, appending the infusion text at the end. For mods that **need** the true name, whether for renaming or any other purpose, Seven Elements exposes it via the `ItemStack#sevenelements$getTrueName()`.

Do note that the `Text` returned by this method is only uninclusive of changes made by Seven Elements. If other mods edit the item name, they are likely reflected in the returned `Text`.
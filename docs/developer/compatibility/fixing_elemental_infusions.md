---
prev: false
---

# Fixing Elemental Infusions

::: tip
This compatibility layer only applies to mods! If you are a data pack developer, you may freely skip this page.
:::

From [Indirect Elemental Damage](../../guide/elements/elemental_combat.md#indirect-elemental-damage), you may have seen that supported items that shoot projectiles like the [Bow](https://minecraft.wiki/w/Bow), [Crossbow](https://minecraft.wiki/w/Crossbow) and [Trident](https://minecraft.wiki/w/Trident) will have their damage be infused with the same Element as the item.

From [Direct Elemental Damage](../../guide/elements/elemental_combat.md#direct-elemental-damage), you may have seen that if damage is dealt directly, it would be infused with the same Element as the item.

If your mod adds an item that does this, does "direct" *indirect* or "indirect" *direct* elemental damage, you can test this by infusing the item with an Element and dealing damage with it.

When the damage isn't of the expected Element, that means that the elemental infusion isn't properly applied.

## Fixing projectile infusions

An easy way to fix projectile infusions not working is by calling `sevenelements$setOriginStack` on the created instance of `ProjectileEntity` with the stack that has to be infused in order for the projectiles to be infused as well.

Seven Elements handles saving and loading the elemental infusion, so you don't need to keep track of the origin stack in-between world saves.

### Automatic projectile infusions

In versions of Seven Elements for the later Minecraft versions, this will work out of the box **if** your custom projectile is a subclass of `PersistentProjectileEntity` **and** you used either the second or third constructor overload to create the entity:

```java
public abstract class PersistentProjectileEntity extends ProjectileEntity {
	protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world, ItemStack stack, @Nullable ItemStack weapon) {
		// ...
	}

	protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, @Nullable ItemStack shotFrom) {
		// ...
	}
}
```

For the second overload, the infusion will originate from the `@Nullable ItemStack weapon` argument, given that it is not `null`.

For the third overload, the infusion will originate from the `@Nullable ItemStack shotFrom` argument, given that it is not `null`.

## Other methods

Damage is only considered *indirect* **if** the `source` argument is provided in a `DamageSource`. If a `source` is not needed, simply leave it as `null`, and infusions should work properly, assuming the `attacker` is set properly.

Likewise, damage is only considered *direct* **if** the `source` argument is **not** provided in a `DamageSource`. If you have an ability that summons something, like a tornado, AoE damage field, magic spells or other things, it would be best if you:
- a. Use the same method as indirect elemental damage: store the item used to summon the ability and use it to create the `ElementalDamageSource`, or
- b. Consider the ability as indirect and ignore item infusions directly.

This heavily depends on your use case: **a.** is best for "closely-linked abilities" (tornados, fields, etc.), while **b.** is best for a summoning-type ability seperate from the item ("summons to fight for you" type abilities, etc.). Of course, this isn't an absolute guide; the implementation is purely up to how **you**, as the mod developer, want it to be.
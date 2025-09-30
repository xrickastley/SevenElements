package io.github.xrickastley.sevenelements.element.reaction;

import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.registry.Registry;

public class ElementalReactions {
	// Also known as Reverse Melt, amplifies Cryo DMG by 1.5x
	public static final ElementalReaction CRYO_MELT = new CryoMeltElementalReaction();
	// Also known as Forward Vaporize, amplifies Hydro DMG by 2x
	public static final ElementalReaction HYDRO_VAPORIZE = new HydroVaporizeElementalReaction();
	// Also known as Forward Melt, with the Cryo aura. Amplifies Pyro DMG by 2x
	public static final ElementalReaction PYRO_CRYO_MELT = new PyroCryoMeltElementalReaction();
	// Also known as Forward Melt, with the Frozen aura. Amplifies Pyro DMG by 2x
	public static final ElementalReaction PYRO_FROZEN_MELT = new PyroFrozenMeltElementalReaction();
	// Also known as Reverse Vaporize, amplifies Pyro DMG by 1.5x
	public static final ElementalReaction PYRO_VAPORIZE = new PyroVaporizeElementalReaction();
	// Keeps the entity in place, preventing movement, attacks and actions.
	public static final ElementalReaction FROZEN = new FrozenElementalReaction();
	// Deals DoT Electro DMG to the target and nearby entities.
	public static final ElementalReaction ELECTRO_CHARGED = new ElectroChargedElementalReaction();
	// Creates an Explosion at the source, damaging all nearby "enemy" entities.
	public static final ElementalReaction OVERLOADED = new OverloadedElementalReaction();
	// Decreases Physical RES% by -40%.
	public static final ElementalReaction SUPERCONDUCT = new SuperconductElementalReaction();
	public static final ElementalReaction FROZEN_SUPERCONDUCT = new FrozenSuperconductElementalReaction();
	// Shatters the Frozen aura on the target, dealing damage.
	public static final ElementalReaction GEO_SHATTER = new GeoShatterElementalReaction();
	public static final ElementalReaction SHATTER = new HeavyShatterElementalReaction();
	// Applies the Quicken aura to the target.
	public static final ElementalReaction QUICKEN = new QuickenElementalReaction();
	// Applies a 125% Level Multipler Additive Base DMG Bonus to the damage dealt.
	public static final ElementalReaction SPREAD = new SpreadElementalReaction();
	// Applies a 115% Level Multipler Additive Base DMG Bonus to the damage dealt.
	public static final ElementalReaction AGGRAVATE = new AggravateElementalReaction();
	// Applies the Burning aura to the target, dealing Pyro DMG every 5 ticks (0.25s)
	public static final ElementalReaction BURNING = new BurningElementalReaction();
	public static final ElementalReaction QUICKEN_BURNING = new QuickenBurningElementalReaction();
	// Creates a Dendro Core at the source.
	public static final ElementalReaction DENDRO_BLOOM = new DendroBloomElementalReaction();
	public static final ElementalReaction HYDRO_BLOOM = new HydroBloomElementalReaction();
	public static final ElementalReaction QUICKEN_BLOOM = new QuickenBloomElementalReaction();
	// Affects the applied swirlable element to all other opponents.
	public static final ElementalReaction PYRO_SWIRL = new PyroSwirlElementalReaction();
	public static final ElementalReaction HYDRO_SWIRL = new HydroSwirlElementalReaction();
	public static final ElementalReaction ELECTRO_SWIRL = new ElectroSwirlElementalReaction();
	public static final ElementalReaction CRYO_SWIRL = new CryoSwirlElementalReaction();
	public static final ElementalReaction FROZEN_SWIRL = new FrozenSwirlElementalReaction();
	// Creates a Crystallize Shard in front of the target.
	public static final ElementalReaction PYRO_CRYSTALLIZE = new PyroCrystallizeElementalReaction();
	public static final ElementalReaction HYDRO_CRYSTALLIZE = new HydroCrystallizeElementalReaction();
	public static final ElementalReaction ELECTRO_CRYSTALLIZE = new ElectroCrystallizeElementalReaction();
	public static final ElementalReaction CRYO_CRYSTALLIZE = new CryoCrystallizeElementalReaction();
	public static final ElementalReaction FROZEN_CRYSTALLIZE = new FrozenCrystallizeElementalReaction();
	// Dendro Core Reaction: Creates a Sprawling Shot that homes in on the nearest enemy.
	public static final ElementalReaction HYPERBLOOM = new HyperbloomElementalReaction();
	// Dendro Core Reaction: Explodes the Dendro Core and deals increased AoE Dendro DMG.
	public static final ElementalReaction BURGEON = new BurgeonElementalReaction();

	public static void register() {
		register(ElementalReactions.CRYO_MELT);
		register(ElementalReactions.HYDRO_VAPORIZE);
		register(ElementalReactions.PYRO_CRYO_MELT);
		register(ElementalReactions.PYRO_FROZEN_MELT);
		register(ElementalReactions.PYRO_VAPORIZE);
		register(ElementalReactions.FROZEN);
		register(ElementalReactions.ELECTRO_CHARGED);
		register(ElementalReactions.OVERLOADED);
		register(ElementalReactions.SUPERCONDUCT);
		register(ElementalReactions.FROZEN_SUPERCONDUCT);
		register(ElementalReactions.GEO_SHATTER);
		register(ElementalReactions.SHATTER);
		register(ElementalReactions.QUICKEN);
		register(ElementalReactions.SPREAD);
		register(ElementalReactions.AGGRAVATE);
		register(ElementalReactions.BURNING);
		register(ElementalReactions.QUICKEN_BURNING);
		register(ElementalReactions.DENDRO_BLOOM);
		register(ElementalReactions.HYDRO_BLOOM);
		register(ElementalReactions.QUICKEN_BLOOM);
		register(ElementalReactions.PYRO_SWIRL);
		register(ElementalReactions.HYDRO_SWIRL);
		register(ElementalReactions.ELECTRO_SWIRL);
		register(ElementalReactions.CRYO_SWIRL);
		register(ElementalReactions.FROZEN_SWIRL);
		register(ElementalReactions.PYRO_CRYSTALLIZE);
		register(ElementalReactions.HYDRO_CRYSTALLIZE);
		register(ElementalReactions.ELECTRO_CRYSTALLIZE);
		register(ElementalReactions.CRYO_CRYSTALLIZE);
		register(ElementalReactions.FROZEN_CRYSTALLIZE);
		register(ElementalReactions.HYPERBLOOM);
		register(ElementalReactions.BURGEON);
	}

	private static void register(ElementalReaction reaction) {
		Registry.register(SevenElementsRegistries.ELEMENTAL_REACTION, reaction.getId(), reaction);
	}
}

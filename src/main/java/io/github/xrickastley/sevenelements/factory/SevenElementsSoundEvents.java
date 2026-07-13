package io.github.xrickastley.sevenelements.factory;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SevenElementsSoundEvents {
	public static final SoundEvent REACTION = register("reaction");
	public static final SoundEvent DENDRO_CORE_EXPLOSION = register("dendro_core_explosion");
	public static final SoundEvent SPRAWLING_SHOT_HIT = register("sprawling_shot_hit");
	public static final SoundEvent CRYSTALLIZE_SHIELD = register("crystallize_shield");
	public static final SoundEvent CRYSTALLIZE_SHIELD_HIT = register("crystallize_shield.hit");
	public static final SoundEvent CRYSTALLIZE_SHIELD_BREAK = register("crystallize_shield.break");
	public static final SoundEvent ITEM_INFUSION_APPLY = register("item_infusion.apply");
	public static final SoundEvent ITEM_INFUSION_REMOVE = register("item_infusion.remove");

	// Initializes the class upon call by SevenElements.
	public static void register() {}

	private static SoundEvent register(String id) {
		return register(SevenElements.identifier(id));
	}

	private static SoundEvent register(Identifier id) {
		return register(id, id);
	}

	private static SoundEvent register(Identifier id, Identifier soundId) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(soundId));
	}
}

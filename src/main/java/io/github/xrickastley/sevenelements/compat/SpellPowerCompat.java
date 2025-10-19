package io.github.xrickastley.sevenelements.compat;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.PartialElementalDamageSource;
import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;

public class SpellPowerCompat {
	private static final Map<Identifier, Entry> SPELL_INFUSIONS = new HashMap<>();

	/**
	 * Registers a simple elemental infusion for a Spell Power.
	 * @param spellSchoolId
	 * @param element
	 */
	public static void registerInfusion(Identifier spellSchoolId, Element element) {
		SpellPowerCompat.registerInfusion(
			spellSchoolId, 
			ElementalApplications.builder()
				.setElement(element)
				.setType(Type.GAUGE_UNIT)
				.setGaugeUnits(1.0)
		);
	}

	public static void registerInfusion(Identifier spellSchoolId, ElementalApplication.Builder infusionBuilder) {
		SpellPowerCompat.registerInfusion(spellSchoolId, infusionBuilder, InternalCooldownContext.Builder.ofNone());
	}

	public static void registerInfusion(Identifier spellSchoolId, ElementalApplication.Builder infusionBuilder, InternalCooldownContext.Builder icdBuilder) {
		if (SpellPowerCompat.SPELL_INFUSIONS.containsKey(spellSchoolId))
			throw new IllegalStateException("The provided Spell Power id: " + spellSchoolId + " has already been registered!");

		SpellPowerCompat.SPELL_INFUSIONS.put(spellSchoolId, new Entry(infusionBuilder, icdBuilder));
	}

	@ApiStatus.Internal
	public static DamageSource create(LivingEntity target, DamageSource source, Identifier spellSchoolId) {
		if (!FabricLoader.getInstance().isModLoaded("spell_power")) return source;

		return SpellPowerCompat.SPELL_INFUSIONS.containsKey(spellSchoolId)
			? SpellPowerCompat.SPELL_INFUSIONS.get(spellSchoolId).create(target, source)
			: source;
	}

	@ApiStatus.Internal
	public static DamageSource create(DamageSource source, Identifier spellSchoolId) {
		if (!FabricLoader.getInstance().isModLoaded("spell_power")) return source;

		return SpellPowerCompat.SPELL_INFUSIONS.containsKey(spellSchoolId)
			? SpellPowerCompat.SPELL_INFUSIONS.get(spellSchoolId).create(source)
			: source;
	}

	@ApiStatus.Internal
	private static final record Entry(ElementalApplication.Builder infusionBuilder, InternalCooldownContext.Builder icdBuilder) {
		public ElementalDamageSource create(LivingEntity target, DamageSource source) {
			return new ElementalDamageSource(
				source, 
				infusionBuilder.build(target), 
				icdBuilder.build(ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class))
			);
		}

		public PartialElementalDamageSource create(DamageSource source) {
			return new PartialElementalDamageSource(
				source, 
				infusionBuilder, 
				icdBuilder.build(ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class))
			);
		}
	}

	// Uses Identifiers to reduces the chance for "cannot load class xxxx" errors when Spell Engine/Spell Power is missing.
	static {
		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "fire"),
			Element.PYRO
		);

		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "water"),
			Element.HYDRO
		);

		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "air"),
			Element.ANEMO
		);

		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "lightning"),
			Element.ELECTRO
		);

		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "frost"),
			Element.CRYO
		);

		SpellPowerCompat.registerInfusion(
			Identifier.of("spell_power", "earth"),
			Element.GEO
		);
	}
}

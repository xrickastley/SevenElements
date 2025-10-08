package io.github.xrickastley.sevenelements.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsAttributes {
	private static final List<EntityAttribute> ADDED_ATTRIBUTES = new ArrayList<>();
	private static final Map<Element, ConcurrentHashMap<ModifierType, EntityAttribute>> LINKS = new ConcurrentHashMap<>();
	private static boolean registered = false;

	public static final EntityAttribute PHYSICAL_DMG_BONUS = register("physical_dmg_bonus", createAttribute("Physical DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute PYRO_DMG_BONUS = register("pyro_dmg_bonus", createAttribute("Pyro DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute HYDRO_DMG_BONUS = register("hydro_dmg_bonus", createAttribute("Hydro DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute ANEMO_DMG_BONUS = register("anemo_dmg_bonus", createAttribute("Anemo DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute ELECTRO_DMG_BONUS = register("electro_dmg_bonus", createAttribute("Electro DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute DENDRO_DMG_BONUS = register("dendro_dmg_bonus", createAttribute("Dendro DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute CRYO_DMG_BONUS = register("cryo_dmg_bonus", createAttribute("Cryo DMG Bonus%", 0, 0, 400));
	public static final EntityAttribute GEO_DMG_BONUS = register("geo_dmg_bonus", createAttribute("Geo DMG Bonus%", 0, 0, 400));

	public static final EntityAttribute PHYSICAL_RES = register("physical_res", createAttribute("Physical RES%", 0, -200, 100));
	public static final EntityAttribute PYRO_RES = register("pyro_res", createAttribute("Pyro RES%", 0, -200, 100));
	public static final EntityAttribute HYDRO_RES = register("hydro_res", createAttribute("Hydro RES%", 0, -200, 100));
	public static final EntityAttribute ANEMO_RES = register("anemo_res", createAttribute("Anemo RES%", 0, -200, 100));
	public static final EntityAttribute ELECTRO_RES = register("electro_res", createAttribute("Electro RES%", 0, -200, 100));
	public static final EntityAttribute DENDRO_RES = register("dendro_res", createAttribute("Dendro RES%", 0, -200, 100));
	public static final EntityAttribute CRYO_RES = register("cryo_res", createAttribute("Cryo RES%", 0, -200, 100));
	public static final EntityAttribute GEO_RES = register("geo_res", createAttribute("Geo RES%", 0, -200, 100));

	public static void register() {
		if (registered) return;

		link(PHYSICAL_DMG_BONUS, Element.PHYSICAL, ModifierType.DMG_BONUS);
		link(PYRO_DMG_BONUS, Element.PYRO, ModifierType.DMG_BONUS);
		link(HYDRO_DMG_BONUS, Element.HYDRO, ModifierType.DMG_BONUS);
		link(ANEMO_DMG_BONUS, Element.ANEMO, ModifierType.DMG_BONUS);
		link(ELECTRO_DMG_BONUS, Element.ELECTRO, ModifierType.DMG_BONUS);
		link(DENDRO_DMG_BONUS, Element.DENDRO, ModifierType.DMG_BONUS);
		link(CRYO_DMG_BONUS, Element.CRYO, ModifierType.DMG_BONUS);
		link(GEO_DMG_BONUS, Element.GEO, ModifierType.DMG_BONUS);

		link(PHYSICAL_RES, Element.PHYSICAL, ModifierType.RES);
		link(PYRO_RES, Element.PYRO, ModifierType.RES);
		link(HYDRO_RES, Element.HYDRO, ModifierType.RES);
		link(ANEMO_RES, Element.ANEMO, ModifierType.RES);
		link(ELECTRO_RES, Element.ELECTRO, ModifierType.RES);
		link(DENDRO_RES, Element.DENDRO, ModifierType.RES);
		link(CRYO_RES, Element.CRYO, ModifierType.RES);
		link(GEO_RES, Element.GEO, ModifierType.RES);

		registered = true;
	}

	/**
	 * Modifies the provided damage, applying Resistances and DMG Bonus to it.
	 * @param target The target that will receive the DMG.
	 * @param source The {@link ElementalDamageSource} to use in modifying the damage.
	 * @param amount The current amount of DMG being dealt.
	 * @return The modified amount of DMG that should be dealt.
	 */
	public static float modifyDamage(LivingEntity target, ElementalDamageSource source, float amount) {
		if (!(source.getAttacker() instanceof final LivingEntity attacker))
			return amount;

		final Element element = source.getElementalApplication().getElement();
		final ConcurrentHashMap<ModifierType, EntityAttribute> modifierMap = SevenElementsAttributes.LINKS.getOrDefault(element, new ConcurrentHashMap<>());

		final EntityAttribute dmgBonusAttribute = modifierMap.get(ModifierType.DMG_BONUS);
		final EntityAttribute resAttribute = modifierMap.get(ModifierType.RES);

		final float dmgBonusMultiplier = 1 + (target.getAttributes().hasAttribute(dmgBonusAttribute) && source.applyDMGBonus()
			? (float) (attacker.getAttributes().getValue(dmgBonusAttribute) / 100)
			: 0);

		final float resMultiplier = target.getAttributes().hasAttribute(resAttribute) && source.applyRES()
			? (float) getRESMultiplier(target, resAttribute)
			: 1;

		return amount * dmgBonusMultiplier * resMultiplier;
	}

	public static DefaultAttributeContainer.Builder apply(DefaultAttributeContainer.Builder builder) {
		// do this since Mod Load (registering) is delayed.
		SevenElementsAttributes.register();
		SevenElementsAttributes.ADDED_ATTRIBUTES.forEach(builder::add);

		return builder;
	}

	private static double getRESMultiplier(LivingEntity target, EntityAttribute resAttribute) {
		final double elementalRes = target.getAttributes().getValue(resAttribute) / 100;

		return elementalRes < 0
			? 1 - (elementalRes / 2)
			: 0 <= elementalRes && elementalRes < 0.75
				? 1 - elementalRes
				: 1 / ((4 * elementalRes) + 1);
	}

	private static void link(EntityAttribute attribute, Element element, ModifierType modifierType) {
		final ConcurrentHashMap<ModifierType, EntityAttribute> modifierMap = SevenElementsAttributes.LINKS.getOrDefault(element, new ConcurrentHashMap<>());

		modifierMap.put(modifierType, attribute);

		SevenElementsAttributes.LINKS.put(element, modifierMap);
	}

	private static EntityAttribute register(String name, EntityAttribute attribute) {
		final EntityAttribute entry = Registry.register(Registries.ATTRIBUTE, SevenElements.identifier(name), attribute);

		SevenElementsAttributes.ADDED_ATTRIBUTES.add(entry);

		return entry;
	}

	private static EntityAttribute createAttribute(final String name, double base, double min, double max) {
		return new ClampedEntityAttribute(name, base, min, max)
			.setTracked(true);
	}

	private static enum ModifierType {
		DMG_BONUS, RES
	}
}

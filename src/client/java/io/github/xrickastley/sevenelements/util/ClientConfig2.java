package io.github.xrickastley.sevenelements.util;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "seven-elements")
public class ClientConfig2 implements ConfigData {
	@ConfigEntry.Gui.CollapsibleObject
	public Renderers renderers = new Renderers();

	@ConfigEntry.Gui.CollapsibleObject
	public Developer developer = new Developer();

	public static class Renderers {
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.BoundedDiscrete(min = 4, max = 48)
		public int sphereResolution = 16;
		@ConfigEntry.Gui.Tooltip
		public double globalTextScale = 1.0;
		public boolean showDamageText = true;
		public double normalDMGScale = 0.65;
		public double critDMGScale = 1.0;
		@ConfigEntry.Gui.Tooltip(count = 4)
		@ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
		public EffectRenderType effectRenderType = EffectRenderType.ALL;
		public boolean displayTooltipAfterInfusion = true;
		@ConfigEntry.BoundedDiscrete(min = 5, max = 50)
		public int tooltipDisplayTicks = 20;
	}

	public static class Developer {
		@ConfigEntry.Gui.Tooltip
		public boolean displayElementalGauges = false;
		@ConfigEntry.Gui.Tooltip
		public boolean displayGaugeRuler = false;
		@ConfigEntry.Gui.Tooltip
		public boolean genshinDamageLim = false;
		@ConfigEntry.Gui.Tooltip
		public boolean commafyDamage = false;
	}

	public static ClientConfig2 get() {
		return AutoConfig
			.getConfigHolder(ClientConfig2.class)
			.get();
	}

	public static EffectRenderType getEffectRenderType() {
		return ClientConfig2.get().renderers.effectRenderType;
	}

	public static enum EffectRenderType {
		NONE(false, false),
		SPECIAL(true, false),
		ALL(true, true);

		private boolean special;
		private boolean normal;

		EffectRenderType(boolean special, boolean normal) {
			this.special = special;
			this.normal = normal;
		}

		public boolean allowsSpecialEffects() {
			return special;
		}

		public boolean allowsNormalEffects() {
			return normal;
		}
	}
}

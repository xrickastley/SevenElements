package io.github.xrickastley.sevenelements;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.xrickastley.sevenelements.util.ClientConfig;

import me.shedaniel.autoconfig.AutoConfig;

public class SevenElementsModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(ClientConfig.class, parent).get();
	}

}

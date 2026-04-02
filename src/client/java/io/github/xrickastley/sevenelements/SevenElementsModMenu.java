package io.github.xrickastley.sevenelements;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.xrickastley.sevenelements.util.ClientConfig;

import me.shedaniel.autoconfig.AutoConfigClient;

public class SevenElementsModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfigClient.getConfigScreen(ClientConfig.class, parent).get();
	}

}

package io.github.xrickastley.sevenelements.interfaces;

import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;

public interface SevenElementsSubmitNodeCollection {
	default SimpleFeatureRenderPhase sevenelements$getElements() {
		return null;
	}

	default SimpleFeatureRenderPhase sevenelements$getElementGauges() {
		return null;
	}

	default SimpleFeatureRenderPhase sevenelements$getCrystallizeShields() {
		return null;
	}
}

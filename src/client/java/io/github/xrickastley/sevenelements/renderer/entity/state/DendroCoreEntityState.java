package io.github.xrickastley.sevenelements.renderer.entity.state;

import io.github.xrickastley.sevenelements.entity.DendroCoreEntity;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;

public class DendroCoreEntityState extends LivingEntityRenderState {
	public boolean normal;
	public boolean hyperbloom;
	public boolean burgeon;

	public boolean isNormal() {
		return this.normal;
	}

	public boolean isHyperbloom() {
		return this.hyperbloom;
	}

	public boolean isBurgeon() {
		return this.burgeon;
	}

	public void apply(final DendroCoreEntity dendroCore) {
		this.normal = dendroCore.isNormal();
		this.hyperbloom = dendroCore.isHyperbloom();
		this.burgeon = dendroCore.isBurgeon();
	}
}

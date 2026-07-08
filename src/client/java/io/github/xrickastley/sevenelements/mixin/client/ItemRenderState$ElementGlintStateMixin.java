package io.github.xrickastley.sevenelements.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer.GlintType;
import io.github.xrickastley.sevenelements.renderer.ElementGlintRenderer;

import net.minecraft.client.render.item.ItemRenderState;

@Mixin({ ItemRenderState.Glint.class, ItemRenderState.LayerRenderState.class })
public class ItemRenderState$ElementGlintStateMixin implements ElementGlintState {
	@Unique
	private Element sevenelements$element;

	@Unique
	private ElementGlintRenderer.GlintType sevenelements$type;

	@Unique
	@Override
	public void sevenelements$resetElementGlintState() {
		this.sevenelements$element = null;
		this.sevenelements$type = null;
	}

	@Unique
	@Override
	public void sevenelements$setElement(Element element) {
		this.sevenelements$element = element;
	}

	@Unique
	@Override
	public void sevenelements$setGlintType(GlintType type) {
		this.sevenelements$type = type;
	}

	@Unique
	@Override
	public @Nullable Element sevenelements$getElement() {
		return this.sevenelements$element;
	}

	@Unique
	@Override
	public boolean sevenelements$hasElementalGlint() {
		return this.sevenelements$type != null
			&& this.sevenelements$element != null;
	}

	@Unique
	@Override
	public boolean sevenelements$hasAttunementGlint() {
		return this.sevenelements$hasElementalGlint()
			&& this.sevenelements$type == ElementGlintRenderer.GlintType.ATTUNEMENT;
	}

	@Unique
	@Override
	public void sevenelements$applyElementGlintState(ElementGlintState other) {
		other.sevenelements$setElement(this.sevenelements$element);
		other.sevenelements$setGlintType(this.sevenelements$type);
	}
}

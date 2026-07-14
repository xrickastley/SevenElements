package io.github.xrickastley.sevenelements.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.function.Function;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ElementGlintRenderer {
	public static final GlintRenderLayer ARMOR_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getArmorEntityElementGlint(), GlintRenderType.ENTITY);
	public static final GlintRenderLayer GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getElementGlint(), GlintRenderType.ITEM);
	public static final GlintRenderLayer STATIC_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getStaticElementGlint(), GlintRenderType.ITEM);
	public static final GlintRenderLayer ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getEntityElementGlint(), GlintRenderType.ENTITY);
	public static final GlintRenderLayer STATIC_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getStaticEntityElementGlint(), GlintRenderType.ENTITY);

	public static RenderType getArmorEntityGlintLayer(ItemStack stack) {
		return ElementGlintRenderer.getArmorEntityGlintLayer(RenderTypes.armorEntityGlint(), stack);
	}

	public static RenderType getArmorEntityGlintLayer(RenderType original, ItemStack stack) {
		return stack.sevenelements$hasAttunementGlint()
			? ElementGlintRenderer.ARMOR_ENTITY_GLINT.getLayer(stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element())
			: original;
	}



	public static class GlintRenderLayer {
		private final Function<Identifier, RenderType> renderLayerFunction;
		private final GlintRenderType glintType;

		private GlintRenderLayer(Function<Identifier, RenderType> renderLayerFunction, GlintRenderType glintType) {
			this.renderLayerFunction = renderLayerFunction;
			this.glintType = glintType;
		}

		public RenderType getLayer(ElementGlintState glintState) {
			return this.getLayer(glintState.sevenelements$getElement());
		}

		public RenderType getLayer(Element element) {
			return renderLayerFunction.apply(glintType.getElementGlintPath(element));
		}

		public VertexConsumer getVertexConsumer(Function<RenderType, VertexConsumer> provider, ElementGlintState glintState) {
			return this.getVertexConsumer(provider, glintState.sevenelements$getElement());
		}

		public VertexConsumer getVertexConsumer(Function<RenderType, VertexConsumer> provider, Element element) {
			return provider.apply(this.getLayer(element));
		}
	}

	private static enum GlintRenderType {
		ITEM("_enchanted_glint_item.png"),
		ENTITY("_enchanted_glint_armor.png");

		private final String suffix;

		private GlintRenderType(String suffix) {
			this.suffix = suffix;
		}

		private Identifier getElementGlintPath(Element element) {
			return SevenElements.identifier("textures/misc/" + element.getId().getPath() + suffix);
		}
	}

	public static enum GlintType {
		INFUSION, ATTUNEMENT;
	}
}

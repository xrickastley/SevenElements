package io.github.xrickastley.sevenelements.renderer;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.interfaces.ElementGlintState;

import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ElementGlintRenderer {
	public static final GlintRenderLayer ARMOR_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getArmorEntityElementGlint(), GlintRenderType.ENTITY);
	public static final GlintRenderLayer GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getElementGlint(), GlintRenderType.ITEM);
	public static final GlintRenderLayer STATIC_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getStaticElementGlint(), GlintRenderType.ITEM);
	public static final GlintRenderLayer ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getEntityElementGlint(), GlintRenderType.ENTITY);
	public static final GlintRenderLayer STATIC_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayers.getStaticEntityElementGlint(), GlintRenderType.ENTITY);

	public static RenderLayer getArmorEntityGlintLayer(ItemStack stack) {
		return ElementGlintRenderer.getArmorEntityGlintLayer(RenderLayers.armorEntityGlint(), stack);
	}

	public static RenderLayer getArmorEntityGlintLayer(RenderLayer original, ItemStack stack) {
		return stack.sevenelements$hasAttunementGlint()
			? ElementGlintRenderer.ARMOR_ENTITY_GLINT.getLayer(stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element())
			: original;
	}

	public static VertexConsumer getSpecialDisplayGlintConsumer(VertexConsumer original, VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry, ElementGlintState glintState) {
		return glintState.sevenelements$hasElementalGlint()
			? ElementGlintRenderer.getCombinedGlintConsumer(
				glintState,
				provider,
				layer,
				original,
				new OverlayVertexConsumer(provider.getBuffer(ElementGlintRenderer.getGlintLayer(glintState, ElementGlintRenderer.STATIC_GLINT, ElementGlintRenderer.GLINT)), entry, 0.0078125F)
			)
			: original;
	}

	public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, ElementGlintState glintState) {
		return ElementGlintRenderer.getItemGlintConsumer(ItemRenderer.getItemGlintConsumer(vertexConsumers, layer, solid, glint), vertexConsumers, layer, solid, glint, glintState);
	}

	public static VertexConsumer getItemGlintConsumer(VertexConsumer original, VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, ElementGlintState glintState) {
		return glintState.sevenelements$hasElementalGlint()
			? ElementGlintRenderer.getCombinedGlintConsumer(
				glintState,
				vertexConsumers,
				layer,
				original,
				solid ? ElementGlintRenderer.STATIC_GLINT : ElementGlintRenderer.STATIC_ENTITY_GLINT,
				solid ? ElementGlintRenderer.GLINT : ElementGlintRenderer.ENTITY_GLINT
			)
			: original;
	}



	@ApiStatus.Internal
	public static VertexConsumer getCombinedGlintConsumer(ElementGlintState glintState, VertexConsumerProvider provider, RenderLayer layer, VertexConsumer glintConsumer, final GlintRenderLayer infusionLayer, final GlintRenderLayer attunementLayer) {
		return ElementGlintRenderer.getCombinedGlintConsumer(
			glintState,
			provider,
			layer,
			glintConsumer,
			provider.getBuffer(ElementGlintRenderer.getGlintLayer(glintState, infusionLayer, attunementLayer))
		);
	}

	@ApiStatus.Internal
	public static VertexConsumer getCombinedGlintConsumer(ElementGlintState glintState, VertexConsumerProvider provider, RenderLayer layer, VertexConsumer glintConsumer, VertexConsumer elementGlintConsumer) {
		return VertexConsumers.union(
			elementGlintConsumer,
			!glintState.sevenelements$hasAttunementGlint()
				? glintConsumer
				: provider.getBuffer(layer)
		);
	}

	@ApiStatus.Internal
	public static RenderLayer getGlintLayer(ElementGlintState glintState, final GlintRenderLayer infusionLayer, final GlintRenderLayer attunementLayer) {
		if (!glintState.sevenelements$hasElementalGlint())
			throw new IllegalArgumentException("The provided item must have an elemental glint!");

		return glintState.sevenelements$hasAttunementGlint()
			? attunementLayer.getLayer(glintState.sevenelements$getElement())
			: infusionLayer.getLayer(glintState.sevenelements$getElement());
	}

	public static class GlintRenderLayer {
		private final Function<Identifier, RenderLayer> renderLayerFunction;
		private final GlintRenderType glintType;

		private GlintRenderLayer(Function<Identifier, RenderLayer> renderLayerFunction, GlintRenderType glintType) {
			this.renderLayerFunction = renderLayerFunction;
			this.glintType = glintType;
		}

		public RenderLayer getLayer(Element element) {
			return renderLayerFunction.apply(glintType.getElementGlintPath(element));
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

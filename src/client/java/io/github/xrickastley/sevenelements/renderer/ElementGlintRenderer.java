package io.github.xrickastley.sevenelements.renderer;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.component.ElementalAttunementComponent;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;

import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ElementGlintRenderer {
	public static final GlintRenderLayer ARMOR_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayer.getArmorEntityElementGlint(), GlintType.ENTITY);
	public static final GlintRenderLayer GLINT = new GlintRenderLayer(SevenElementsRenderLayer.getElementGlint(), GlintType.ITEM);
	public static final GlintRenderLayer STATIC_GLINT = new GlintRenderLayer(SevenElementsRenderLayer.getStaticElementGlint(), GlintType.ITEM);
	public static final GlintRenderLayer ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayer.getEntityElementGlint(), GlintType.ENTITY);
	public static final GlintRenderLayer STATIC_ENTITY_GLINT = new GlintRenderLayer(SevenElementsRenderLayer.getStaticEntityElementGlint(), GlintType.ENTITY);

	public static VertexConsumer getArmorGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, ItemStack stack, boolean glint) {
		return ElementGlintRenderer.getArmorGlintConsumer(ItemRenderer.getArmorGlintConsumer(provider, layer, glint), provider, layer, stack, glint);
	}

	public static VertexConsumer getArmorGlintConsumer(VertexConsumer original, VertexConsumerProvider provider, RenderLayer layer, ItemStack stack, boolean glint) {
		return stack.sevenelements$hasAttunementGlint()
			? VertexConsumers.union(provider.getBuffer(ElementGlintRenderer.ARMOR_ENTITY_GLINT.getLayer(stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element())), provider.getBuffer(layer))
			: original;
	}

	public static VertexConsumer getDynamicDisplayGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry, ItemStack stack) {
		return ElementGlintRenderer.getDynamicDisplayGlintConsumer(ItemRenderer.getDynamicDisplayGlintConsumer(provider, layer, entry), provider, layer, entry, stack);
	}

	public static VertexConsumer getDynamicDisplayGlintConsumer(VertexConsumer original, VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry, ItemStack stack) {
		return stack.sevenelements$hasElementalGlint()
			? ElementGlintRenderer.getCombinedGlintConsumer(
				stack,
				provider,
				layer,
				original,
				new OverlayVertexConsumer(provider.getBuffer(ElementGlintRenderer.getGlintLayer(stack, ElementGlintRenderer.STATIC_GLINT, ElementGlintRenderer.GLINT)), entry, 0.0078125F)
			)
			: original;
	}

	public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, ItemStack stack) {
		return ElementGlintRenderer.getItemGlintConsumer(ItemRenderer.getItemGlintConsumer(vertexConsumers, layer, solid, glint), vertexConsumers, layer, solid, glint, stack);
	}

	public static VertexConsumer getItemGlintConsumer(VertexConsumer original, VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, ItemStack stack) {
		return stack.sevenelements$hasElementalGlint()
			? ElementGlintRenderer.getCombinedGlintConsumer(
				stack,
				vertexConsumers,
				layer,
				original,
				solid ? ElementGlintRenderer.STATIC_GLINT : ElementGlintRenderer.STATIC_ENTITY_GLINT,
				solid ? ElementGlintRenderer.GLINT : ElementGlintRenderer.ENTITY_GLINT
			)
			: original;
	}



	@ApiStatus.Internal
	public static VertexConsumer getCombinedGlintConsumer(ItemStack stack, VertexConsumerProvider provider, RenderLayer layer, VertexConsumer glintConsumer, final GlintRenderLayer infusionLayer, final GlintRenderLayer attunementLayer) {
		return ElementGlintRenderer.getCombinedGlintConsumer(
			stack,
			provider,
			layer,
			glintConsumer,
			provider.getBuffer(ElementGlintRenderer.getGlintLayer(stack, infusionLayer, attunementLayer))
		);
	}

	@ApiStatus.Internal
	public static VertexConsumer getCombinedGlintConsumer(ItemStack stack, VertexConsumerProvider provider, RenderLayer layer, VertexConsumer glintConsumer, VertexConsumer elementGlintConsumer) {
		return VertexConsumers.union(
			elementGlintConsumer,
			!stack.sevenelements$hasAttunementGlint()
				? glintConsumer
				: provider.getBuffer(layer)
		);
	}

	@ApiStatus.Internal
	public static RenderLayer getGlintLayer(ItemStack stack, final GlintRenderLayer infusionLayer, final GlintRenderLayer attunementLayer) {
		final @Nullable ElementalInfusionComponent infusion = stack.get(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
		final @Nullable ElementalAttunementComponent attunement = stack.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT);

		if (infusion == null && attunement == null)
			throw new IllegalArgumentException("The provided item must have either the \"seven-elements:elemental_infusion\" or the \"seven-elements:elemental_attunement\" data component!");

		return stack.sevenelements$hasAttunementGlint()
			? attunementLayer.getLayer(attunement.element())
			: infusionLayer.getLayer(infusion.getElement());
	}

	public static class GlintRenderLayer {
		private final Function<Identifier, RenderLayer> renderLayerFunction;
		private final GlintType glintType;

		private GlintRenderLayer(Function<Identifier, RenderLayer> renderLayerFunction, GlintType glintType) {
			this.renderLayerFunction = renderLayerFunction;
			this.glintType = glintType;
		}

		public RenderLayer getLayer(Element element) {
			return renderLayerFunction.apply(glintType.getElementGlintPath(element));
		}
	}

	private static enum GlintType {
		ITEM("_enchanted_glint_item.png"),
		ENTITY("_enchanted_glint_armor.png");

		private final String suffix;

		private GlintType(String suffix) {
			this.suffix = suffix;
		}

		private Identifier getElementGlintPath(Element element) {
			return SevenElements.identifier("textures/misc/" + element.getId().getPath() + suffix);
		}
	}
}

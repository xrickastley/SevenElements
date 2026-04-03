package io.github.xrickastley.sevenelements.gui.screen.ingame;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ElementalInfusionScreen extends AbstractContainerScreen<ElementalInfusionScreenHandler> {
	private static final Identifier TEXTURE = SevenElements.identifier("textures/gui/container/infusion_table.png");

	private static final Identifier SLOT_DISABLED_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
	private static final Identifier SLOT_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
	private static final Identifier SLOT_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot");

	private static final Identifier LEVEL_DISABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_disabled");
	private static final Identifier LEVEL_ENABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_enabled");

	private static final int LOCK_TICKS = 10;
	private final Player player;
	private boolean locked = false;
	private long lockedAt;
	private long tooltipDisplayedAt;

	public ElementalInfusionScreen(ElementalInfusionScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title, 176, 246);

		this.inventoryLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 94;
		this.player = inventory.player;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		final int x = (width - imageWidth) / 2;
		final int y = (height - imageHeight) / 2;

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

		this.drawElements(graphics, x, y);
		this.drawInfuseButton(graphics, x, y, mouseX, mouseY);

		final Slot slot = this.menu.getResultSlot();

		if (this.displayTooltip() && slot.hasItem())
			graphics.setComponentTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(slot.getItem()), x + slot.x + 16, y + slot.y + 12);
	}

	private void drawElements(GuiGraphicsExtractor graphics, final int x, final int y) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.PYRO.getTexture(), x + 76, y + 18, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.HYDRO.getTexture(), x + 107, y + 33, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.ANEMO.getTexture(), x + 115, y + 63, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.ELECTRO.getTexture(), x + 94, y + 92, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.DENDRO.getTexture(), x + 59, y + 92, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.CRYO.getTexture(), x + 37, y + 63, 0, 0, 24, 24, 24, 24);
		graphics.blit(RenderPipelines.GUI_TEXTURED, Element.GEO.getTexture(), x + 45, y + 33, 0, 0, 24, 24, 24, 24);
	}

	private void drawInfuseButton(GuiGraphicsExtractor graphics, final int x, final int y, final int mouseX, final int mouseY) {
		if (!menu.getResultSlot().hasItem()) return;

		final int x1 = x + 43;
		final int y1 = y + 128;
		final int x2 = x1 + 90;
		final int y2 = y1 + 19;

		final Identifier texture = !this.isEnabled()
			? SLOT_DISABLED_TEXTURE
			: MathHelper2.inRange(mouseX, x1, x2) && MathHelper2.inRange(mouseY, y1, y2)
				? SLOT_HIGHLIGHTED_TEXTURE
				: SLOT_TEXTURE;

		final Identifier expTexture = this.isEnabled()
			? LEVEL_ENABLED_TEXTURE
			: LEVEL_DISABLED_TEXTURE;

		final int color = MathHelper2.inRange(mouseX, x1, x2) && MathHelper2.inRange(mouseY, y1, y2) && this.isEnabled()
			? CommonColors.YELLOW
			: 0xFF685E4A;

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x1, y1, 90, 19);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, expTexture, x2 - 24, y2 - 16, 24, 16);
		graphics.text(this.font, Component.translatable("container.seven-elements.infusion_table.infuse"), x1 + 6, y1 + 6, color, false);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.extractBackground(graphics, mouseX, mouseY, a);
		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected void init() {
		super.init();

		titleLabelX = (imageWidth - font.width(title)) / 2;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		return this.checkMouseClick(click, doubled)
			|| super.mouseClicked(click, doubled);
	}

	private boolean checkMouseClick(MouseButtonEvent click, boolean doubled) {
		final int x = (width - imageWidth) / 2;
		final int y = (height - imageHeight) / 2;

		final int x1 = x + 43;
		final int y1 = y + 128;
		final int x2 = x1 + 90;
		final int y2 = y1 + 19;

		if (!MathHelper2.inRange(click.x(), x1, x2) || !MathHelper2.inRange(click.y(), y1, y2)) return false;

		if (!this.isEnabled()) return false;

		this.lock();
		this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
		this.minecraft.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION, 1f, 1f);

		return true;
	}

	public void finishElementalInfusion(FinishElementalInfusionS2CPayload payload) {
		this.unlock();
	}

	private void lock() {
		this.locked = true;
		this.lockedAt = player.level().getGameTime();
		this.menu.getResultSlot().lock();
	}

	private void unlock() {
		this.locked = false;
		this.menu.getResultSlot().unlock();
		this.tooltipDisplayedAt = player.level().getGameTime();
	}

	private boolean isEnabled() {
		return menu.canInfuse(this.player) && !this.isLocked();
	}

	private boolean isLocked() {
		return locked || this.lockedAt + ElementalInfusionScreen.LOCK_TICKS >= this.player.level().getGameTime();
	}

	private boolean displayTooltip() {
		final ClientConfig config = ClientConfig.get();

		return config.rendering.text.displayTooltipAfterInfusion
			&& this.tooltipDisplayedAt + config.rendering.text.tooltipDisplayTicks >= this.player.level().getGameTime()
			&& (this.hoveredSlot == null || !this.hoveredSlot.hasItem());
	}
}

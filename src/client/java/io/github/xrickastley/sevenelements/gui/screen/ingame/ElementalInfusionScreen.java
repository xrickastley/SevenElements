package io.github.xrickastley.sevenelements.gui.screen.ingame;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.mixin.client.EnchantmentScreenAccessor;
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
import net.minecraft.world.item.ItemStack;

public class ElementalInfusionScreen extends AbstractContainerScreen<ElementalInfusionScreenHandler> {
	private static final Identifier TEXTURE = SevenElements.identifier("textures/gui/container/infusion_table.png");
	private static final Identifier UNINFUSE_TEXTURE = SevenElements.identifier("textures/gui/container/infusion_table_uninfuse.png");

	private static final Identifier SLOT_DISABLED_TEXTURE = EnchantmentScreenAccessor.getEnchantmentSlotDisabledTexture();
	private static final Identifier SLOT_HIGHLIGHTED_TEXTURE = EnchantmentScreenAccessor.getEnchantmentSlotHighlightedTexture();
	private static final Identifier SLOT_TEXTURE = EnchantmentScreenAccessor.getEnchantmentSlotTexture();

	private static final Identifier INFUSE_LEVEL_ENABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_enabled");
	private static final Identifier INFUSE_LEVEL_DISABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_disabled");

	private static final Identifier UNINFUSE_LEVEL_ENABLED_TEXTURE = EnchantmentScreenAccessor.getLevelTextures()[1];
	private static final Identifier UNINFUSE_LEVEL_DISABLED_TEXTURE = EnchantmentScreenAccessor.getLevelDisabledTextures()[1];

	private static final int LOCK_TICKS = 10;
	private final Player player;
	private boolean locked = false;
	private long lockedAt;
	private long tooltipDisplayedAt;

	public ElementalInfusionScreen(ElementalInfusionScreenHandler menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 246);

		this.inventoryLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 94;
		this.player = inventory.player;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);

		final Identifier texture = menu.canPerformUninfuse()
			? UNINFUSE_TEXTURE
			: TEXTURE;

		final int x = (width - imageWidth) / 2;
		final int y = (height - imageHeight) / 2;

		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

		this.drawElements(graphics, x, y);
		this.drawButtons(graphics, x, y, mouseX, mouseY);

		final Slot slot = this.menu.getResultSlot();

		if (this.displayTooltip() && slot.hasItem())
			graphics.setComponentTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(slot.getItem()), x + slot.x + 16, y + slot.y + 12);
	}

	private void drawElements(GuiGraphicsExtractor graphics, final int x, final int y) {
		this.drawElement(Element.PYRO, graphics, x + 76, y + 18);
		this.drawElement(Element.HYDRO, graphics, x + 107, y + 33);
		this.drawElement(Element.ANEMO, graphics, x + 115, y + 63);
		this.drawElement(Element.ELECTRO, graphics, x + 94, y + 92);
		this.drawElement(Element.DENDRO, graphics, x + 59, y + 92);
		this.drawElement(Element.CRYO, graphics, x + 37, y + 63);
		this.drawElement(Element.GEO, graphics, x + 45, y + 33);
	}

	private void drawElement(final Element element, final GuiGraphicsExtractor graphics, final int x, final int y) {
		final ItemStack targetItem = menu.getResultSlot().getItem();
		final boolean cantInfuseElement = targetItem.isEmpty()
			|| (targetItem.has(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			&& targetItem.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element() != element);

		final int color = cantInfuseElement ? 0x7f7f7f7f : 0xffffffff;

		graphics.blit(RenderPipelines.GUI_TEXTURED, element.getTexture(), x, y, 0, 0, 24, 24, 24, 24, color);
	}

	private void drawButtons(GuiGraphicsExtractor graphics, final int x, final int y, final int mouseX, final int mouseY) {
		if (!menu.getResultSlot().hasItem()) return;

		this.drawInfuseButton(graphics, x, y, mouseX, mouseY);
		this.drawUninfuseButton(graphics, x, y, mouseX, mouseY);
	}

	private void drawInfuseButton(GuiGraphicsExtractor graphics, final int x, final int y, final int mouseX, final int mouseY) {
		if (!menu.getResultSlot().hasItem()) return;

		final int xStart = menu.canPerformUninfuse() ? 16 : 43;
		final int yStart = 128;
		final int width = menu.canPerformUninfuse() ? 67 : 90;
		final int height = 19;

		final int x1 = x + xStart;
		final int y1 = y + yStart;
		final int x2 = x1 + width;
		final int y2 = y1 + height;

		final boolean canInfuse = this.canPerformInfuse();
		final boolean isButtonHovered = this.isInRectangle(mouseX, mouseY, xStart, yStart, width, height);

		final Identifier texture = !canInfuse
			? SLOT_DISABLED_TEXTURE
			: isButtonHovered
				? SLOT_HIGHLIGHTED_TEXTURE
				: SLOT_TEXTURE;

		final Identifier expTexture = canInfuse
			? INFUSE_LEVEL_ENABLED_TEXTURE
			: INFUSE_LEVEL_DISABLED_TEXTURE;

		final int color = isButtonHovered && canInfuse
			? CommonColors.YELLOW
			: 0xFF685E4A;

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x1, y1, width, height);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, expTexture, x2 - 24, y2 - 16, 24, 16);
		graphics.text(this.font, Component.translatable("container.seven-elements.infusion_table.infuse"), x1 + 6, y1 + 6, color, false);
	}

	private void drawUninfuseButton(GuiGraphicsExtractor graphics, final int x, final int y, final int mouseX, final int mouseY) {
		if (!menu.canPerformUninfuse()) return;

		final int xStart = 94;
		final int yStart = 128;
		final int width = 67;
		final int height = 19;

		final int x1 = x + xStart;
		final int y1 = y + yStart;
		final int x2 = x1 + width;
		final int y2 = y1 + height;

		final boolean canUninfuse = this.canPerformUninfuse();
		final boolean isButtonHovered = this.isInRectangle(mouseX, mouseY, xStart, yStart, width, height);

		final Identifier texture = !canUninfuse
			? SLOT_DISABLED_TEXTURE
			: isButtonHovered
				? SLOT_HIGHLIGHTED_TEXTURE
				: SLOT_TEXTURE;

		final Identifier expTexture = canUninfuse
			? UNINFUSE_LEVEL_ENABLED_TEXTURE
			: UNINFUSE_LEVEL_DISABLED_TEXTURE;

		final int color = isButtonHovered && canUninfuse
			? CommonColors.RED
			: 0xFF685E4A;

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x1, y1, width, height);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, expTexture, x2 - 18, y2 - 16, 16, 16);
		graphics.text(this.font, Component.translatable("container.seven-elements.infusion_table.uninfuse"), x1 + 6, y1 + 6, color, false);
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
		if (menu.canPerformUninfuse())
			return this.checkMouseClickInfused(click, doubled);
		else
			return this.checkMouseClickUninfused(click, doubled);
	}

	private boolean checkMouseClickUninfused(MouseButtonEvent click, boolean doubled) {
		if (!this.isInRectangle(click, 43, 128, 90, 19))
			return false;

		return this.performInfusion();
	}

	private boolean checkMouseClickInfused(MouseButtonEvent click, boolean doubled) {
		if (this.isInRectangle(click, 16, 128, 67, 19))
			return this.performInfusion();
		else if (this.isInRectangle(click, 94, 128, 67, 19))
			return this.performUninfusion();
		else
			return false;
	}

	private boolean isInRectangle(MouseButtonEvent click, double x, double y, double dx, double dy) {
		return this.isInRectangle(click.x(), click.y(), x, y, dx, dy);
	}

	private boolean isInRectangle(double mouseX, double mouseY, double x, double y, double dx, double dy) {
		final double absX = (int) ((width - imageWidth) / 2) + x;
		final double absY = (int) ((height - imageHeight) / 2) + y;

		return MathHelper2.inRange(mouseX, absX, absX + dx) && MathHelper2.inRange(mouseY, absY, absY + dy);
	}

	private boolean performInfusion() {
		if (!this.canPerformInfuse()) return false;

		this.lock();
		this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
		this.minecraft.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION_APPLY, 1f, 1f);

		return true;
	}

	private boolean performUninfusion() {
		if (!this.canPerformUninfuse()) return false;

		this.lock();
		this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
		this.minecraft.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION_REMOVE, 1f, 1f);

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

	private boolean canPerformInfuse() {
		return !this.isLocked() && menu.canInfuse(this.player);
	}

	private boolean canPerformUninfuse() {
		return !this.isLocked() && menu.canUninfuse(this.player);
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

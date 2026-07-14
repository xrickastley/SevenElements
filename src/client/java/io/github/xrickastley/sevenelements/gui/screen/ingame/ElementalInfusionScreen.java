package io.github.xrickastley.sevenelements.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.mixin.client.EnchantmentScreenAccessor;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class ElementalInfusionScreen extends HandledScreen<ElementalInfusionScreenHandler> {
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
	private final PlayerEntity player;
	private boolean locked = false;
	private long lockedAt;
	private long tooltipDisplayedAt;

	public ElementalInfusionScreen(ElementalInfusionScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);

		this.backgroundHeight = 246;
		this.backgroundWidth = 176;
		this.playerInventoryTitleX = 8;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
		this.player = inventory.player;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		final Identifier texture = handler.canPerformUninfuse()
			? UNINFUSE_TEXTURE
			: TEXTURE;

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);

		final int x = (width - backgroundWidth) / 2;
		final int y = (height - backgroundHeight) / 2;

		context.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight);

		this.drawElements(context, x, y);
		this.drawButtons(context, x, y, mouseX, mouseY);

		final Slot slot = this.handler.getResultSlot();

		if (this.displayTooltip() && slot.hasStack())
			context.drawTooltip(this.textRenderer, this.getTooltipFromItem(slot.getStack()), x + slot.x + 16, y + slot.y + 12);
	}

	private void drawElements(DrawContext context, final int x, final int y) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();

		this.drawElement(Element.PYRO, context, x + 76, y + 18);
		this.drawElement(Element.HYDRO, context, x + 107, y + 33);
		this.drawElement(Element.ANEMO, context, x + 115, y + 63);
		this.drawElement(Element.ELECTRO, context, x + 94, y + 92);
		this.drawElement(Element.DENDRO, context, x + 59, y + 92);
		this.drawElement(Element.CRYO, context, x + 37, y + 63);
		this.drawElement(Element.GEO, context, x + 45, y + 33);

		RenderSystem.disableBlend();
		RenderSystem.disableCull();
	}

	private void drawElement(final Element element, final DrawContext context, final int x, final int y) {
		final ItemStack targetItem = handler.getResultSlot().getStack();
		final boolean cantInfuseElement = targetItem.isEmpty()
			|| (targetItem.contains(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT)
			&& targetItem.get(SevenElementsComponents.ELEMENTAL_ATTUNEMENT_COMPONENT).element() != element);

		if (cantInfuseElement)
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 0.5f);

		context.drawTexture(element.getTexture(), x, y, 24, 24, 0, 0, 24, 24, 24, 24);

		if (cantInfuseElement)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}

	private void drawButtons(DrawContext context, final int x, final int y, final int mouseX, final int mouseY) {
		if (!handler.getResultSlot().hasStack()) return;

		this.drawInfuseButton(context, x, y, mouseX, mouseY);
		this.drawUninfuseButton(context, x, y, mouseX, mouseY);
	}

	private void drawInfuseButton(DrawContext context, final int x, final int y, final int mouseX, final int mouseY) {
		if (!handler.getResultSlot().hasStack()) return;

		final int xStart = handler.canPerformUninfuse() ? 16 : 43;
		final int yStart = 128;
		final int width = handler.canPerformUninfuse() ? 67 : 90;
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
			? Colors.YELLOW
			: 0x685E4A;

		RenderSystem.enableBlend();
		context.drawGuiTexture(texture, x1, y1, width, height);
		context.drawGuiTexture(expTexture, x2 - 24, y2 - 16, 24, 16);
		context.drawText(this.textRenderer, Text.translatable("container.seven-elements.infusion_table.infuse"), x1 + 6, y1 + 6, color, false);
		RenderSystem.disableBlend();
	}

	private void drawUninfuseButton(DrawContext context, final int x, final int y, final int mouseX, final int mouseY) {
		if (!handler.canPerformUninfuse()) return;

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
			? Colors.RED
			: 0x685E4A;

		RenderSystem.enableBlend();
		context.drawGuiTexture(texture, x1, y1, width, height);
		context.drawGuiTexture(expTexture, x2 - 18, y2 - 16, 16, 16);
		context.drawText(this.textRenderer, Text.translatable("container.seven-elements.infusion_table.uninfuse"), x1 + 6, y1 + 6, color, false);
		RenderSystem.disableBlend();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void init() {
		super.init();

		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.checkMouseClick(mouseX, mouseY, button)
			|| super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean checkMouseClick(double mouseX, double mouseY, int button) {
		if (handler.canPerformUninfuse())
			return this.checkMouseClickInfused(mouseX, mouseY, button);
		else
			return this.checkMouseClickUninfused(mouseX, mouseY, button);
	}

	// this is like really bad naming but "uninfused" refers to the current item.
	private boolean checkMouseClickUninfused(double mouseX, double mouseY, int button) {
		if (!this.isInRectangle(mouseX, mouseY, 43, 128, 90, 19))
			return false;

		return this.performInfusion();
	}

	// this is like really bad naming but "infused" refers to the current item.
	private boolean checkMouseClickInfused(double mouseX, double mouseY, int button) {
		if (this.isInRectangle(mouseX, mouseY, 16, 128, 67, 19))
			return this.performInfusion();
		else if (this.isInRectangle(mouseX, mouseY, 94, 128, 67, 19))
			return this.performUninfusion();
		else
			return false;
	}

	private boolean isInRectangle(double mouseX, double mouseY, double x, double y, double dx, double dy) {
		final double absX = (int) ((width - backgroundWidth) / 2) + x;
		final double absY = (int) ((height - backgroundHeight) / 2) + y;

		return MathHelper2.inRange(mouseX, absX, absX + dx) && MathHelper2.inRange(mouseY, absY, absY + dy);
	}

	private boolean performInfusion() {
		if (!this.canPerformInfuse()) return false;

		this.lock();
		this.client.interactionManager.clickButton(handler.syncId, 0);
		this.client.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION_APPLY, 1f, 1f);

		return true;
	}

	private boolean performUninfusion() {
		if (!this.canPerformUninfuse()) return false;

		this.lock();
		this.client.interactionManager.clickButton(handler.syncId, 1);
		this.client.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION_REMOVE, 1f, 1f);

		return true;
	}

	public void finishElementalInfusion(FinishElementalInfusionS2CPayload payload) {
		this.unlock();
	}

	private void lock() {
		this.locked = true;
		this.lockedAt = player.getWorld().getTime();
		this.handler.getResultSlot().lock();
	}

	private void unlock() {
		this.locked = false;
		this.handler.getResultSlot().unlock();
		this.tooltipDisplayedAt = player.getWorld().getTime();
	}

	private boolean canPerformInfuse() {
		return !this.isLocked() && handler.canInfuse(this.player);
	}

	private boolean canPerformUninfuse() {
		return !this.isLocked() && handler.canUninfuse(this.player);
	}

	private boolean isLocked() {
		return locked || this.lockedAt + ElementalInfusionScreen.LOCK_TICKS >= this.player.getWorld().getTime();
	}

	private boolean displayTooltip() {
		final ClientConfig config = ClientConfig.get();

		return config.rendering.text.displayTooltipAfterInfusion
			&& this.tooltipDisplayedAt + config.rendering.text.tooltipDisplayTicks >= this.player.getWorld().getTime()
			&& (this.focusedSlot == null || !this.focusedSlot.hasStack());
	}
}

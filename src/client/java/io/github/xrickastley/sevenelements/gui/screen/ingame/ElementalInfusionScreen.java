package io.github.xrickastley.sevenelements.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsSoundEvents;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.screen.ElementalInfusionScreenHandler;
import io.github.xrickastley.sevenelements.util.ClientConfig;
import io.github.xrickastley.sevenelements.util.MathHelper2;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class ElementalInfusionScreen extends HandledScreen<ElementalInfusionScreenHandler> {
	private static final Identifier TEXTURE = SevenElements.identifier("textures/gui/container/infusion_table.png");

	private static final Identifier SLOT_DISABLED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_disabled");
	private static final Identifier SLOT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_highlighted");
	private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot");

	private static final Identifier LEVEL_DISABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_disabled");
	private static final Identifier LEVEL_ENABLED_TEXTURE = SevenElements.identifier("container/infusion_table/level_enabled");

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
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);

		final int x = (width - backgroundWidth) / 2;
		final int y = (height - backgroundHeight) / 2;

		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

		this.drawElements(context, x, y);
		this.drawInfuseButton(context, x, y, mouseX, mouseY);

		final Slot slot = this.handler.getResultSlot();

		if (this.displayTooltip() && slot.hasStack())
			context.drawTooltip(this.textRenderer, this.getTooltipFromItem(slot.getStack()), x + slot.x + 16, y + slot.y + 12);
	}

	private void drawElements(DrawContext context, final int x, final int y) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();

		context.drawTexture(Element.PYRO.getTexture(), x + 76, y + 18, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.HYDRO.getTexture(), x + 107, y + 33, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.ANEMO.getTexture(), x + 115, y + 63, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.ELECTRO.getTexture(), x + 94, y + 92, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.DENDRO.getTexture(), x + 59, y + 92, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.CRYO.getTexture(), x + 37, y + 63, 24, 24, 0, 0, 24, 24, 24, 24);
		context.drawTexture(Element.GEO.getTexture(), x + 45, y + 33, 24, 24, 0, 0, 24, 24, 24, 24);

		RenderSystem.disableBlend();
		RenderSystem.disableCull();
	}

	private void drawInfuseButton(DrawContext context, final int x, final int y, final int mouseX, final int mouseY) {
		if (!handler.getResultSlot().hasStack()) return;

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
			? Colors.YELLOW
			: 0x685E4A;

		RenderSystem.enableBlend();
		context.drawGuiTexture(texture, x1, y1, 90, 19);
		context.drawGuiTexture(expTexture, x2 - 24, y2 - 16, 24, 16);
		context.drawText(this.textRenderer, Text.translatable("container.seven-elements.infusion_table.infuse"), x1 + 6, y1 + 6, color, false);
		RenderSystem.disableBlend();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
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
		final int x = (width - backgroundWidth) / 2;
		final int y = (height - backgroundHeight) / 2;

		final int x1 = x + 43;
		final int y1 = y + 128;
		final int x2 = x1 + 90;
		final int y2 = y1 + 19;

		if (!MathHelper2.inRange(mouseX, x1, x2) || !MathHelper2.inRange(mouseY, y1, y2)) return false;

		if (!this.isEnabled()) return false;

		this.lock();
		this.client.interactionManager.clickButton(handler.syncId, 0);
		this.client.player.playSound(SevenElementsSoundEvents.ITEM_INFUSION, 1f, 1f);

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

	private boolean isEnabled() {
		return handler.canInfuse(this.player) && !this.isLocked();
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

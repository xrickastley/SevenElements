package io.github.xrickastley.sevenelements.screen;

import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.factory.SevenElementsComponents;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ElementalInfusionScreenHandler extends ScreenHandler {
	private static final int INFUSE_REQUIRED_LEVEL = 10;
	private static final int UNINFUSE_REQUIRED_LEVEL = 2;

	private final ScreenHandlerContext context;
	private final CraftingResultInventory output = new CraftingResultInventory();

	public ElementalInfusionScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	public ElementalInfusionScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(SevenElementsScreenHandlers.ELEMENTAL_INFUSION_SCREEN_HANDLER, syncId);

		this.context = context;

		this.addSlot(new LockableSlot(output, 0, 80, 60));

		int i;
		int j;

		for (i = 0; i < 3; ++i)
			for (j = 0; j < 9; ++j)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 164 + i * 18));

		for (i = 0; i < 9; ++i)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 222));
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotId) {
		final Slot slot = this.slots.get(slotId);

		if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

		ItemStack stack = slot.getStack();

		if (slotId == 0) {
			final ItemStack resultStack = slot.getStack();
			stack = resultStack.copy();

			if (!this.insertItem(resultStack, 1, 37, false))
				return ItemStack.EMPTY;

			slot.onQuickTransfer(resultStack, stack);
		} else {
			if (!this.insertItem(stack, 0, 1, false))
				return ItemStack.EMPTY;
		}

		return stack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return ScreenHandler.canUse(context, player, SevenElementsBlocks.INFUSION_TABLE);
	}

	public boolean canInfuse(PlayerEntity player) {
		return (player.experienceLevel >= INFUSE_REQUIRED_LEVEL || player.isInCreativeMode())
			&& this.getResultSlot().hasStack();
	}

	public boolean canUninfuse(PlayerEntity player) {
		return (player.experienceLevel >= UNINFUSE_REQUIRED_LEVEL || player.isInCreativeMode())
			&& this.getResultSlot().getStack().contains(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
	}

	public boolean canPerformUninfuse() {
		return this.getResultSlot().getStack().contains(SevenElementsComponents.ELEMENTAL_INFUSION_COMPONENT);
	}

	public boolean infuse(PlayerEntity player) {
		if (!this.canInfuse(player) || !(player instanceof final ServerPlayerEntity serverPlayer)) return false;

		final Slot slot = this.slots.get(0);

		if (slot == null || !slot.hasStack()) return false;

		final ItemStack stack = slot.getStack();
		final Element infusedElement = ElementalInfusionComponent.generateAndApplyInfusion(stack, player.getEntityWorld()).getLeft();

		if (!player.isInCreativeMode()) serverPlayer.addExperienceLevels(-INFUSE_REQUIRED_LEVEL);

		slot.setStack(stack);
		slot.markDirty();

		ServerPlayNetworking.send(serverPlayer, new FinishElementalInfusionS2CPayload(this));
		SevenElementsCriteria.ELEMENTAL_INFUSION.trigger(serverPlayer, stack, infusedElement);

		return true;
	}

	public boolean uninfuse(PlayerEntity player) {
		if (!this.canPerformUninfuse() || !(player instanceof final ServerPlayerEntity serverPlayer)) return false;

		final Slot slot = this.slots.get(0);

		if (slot == null || !slot.hasStack()) return false;

		final ItemStack stack = slot.getStack();
		ElementalInfusionComponent.removeInfusion(stack);

		if (!player.isInCreativeMode()) serverPlayer.addExperienceLevels(-UNINFUSE_REQUIRED_LEVEL);

		slot.setStack(stack);
		slot.markDirty();

		ServerPlayNetworking.send(serverPlayer, new FinishElementalInfusionS2CPayload(this));

		return true;
	}

	public LockableSlot getResultSlot() {
		// Should always be not null, if null, something wrong happened.
		return ClassInstanceUtil.castOrNull(this.getSlot(0), LockableSlot.class);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		switch (id) {
			case 0:
				return this.infuse(player);
			case 1:
				return this.uninfuse(player);
			default:
				return false;
		}
	}

	@Override
	public void onClosed(PlayerEntity player) {
		this.dropInventory(player, this.output);
	}
}

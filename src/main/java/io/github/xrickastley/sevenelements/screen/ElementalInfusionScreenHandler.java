package io.github.xrickastley.sevenelements.screen;

import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ElementalInfusionScreenHandler extends AbstractContainerMenu {
	private static final int REQUIRED_LEVEL = 10;

	private final ContainerLevelAccess context;
	private final ResultContainer output = new ResultContainer();

	public ElementalInfusionScreenHandler(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, ContainerLevelAccess.NULL);
	}

	public ElementalInfusionScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
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
	public ItemStack quickMoveStack(Player player, int slotId) {
		final Slot slot = this.slots.get(slotId);

		if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

		ItemStack stack = slot.getItem();

		if (slotId == 0) {
			final ItemStack resultStack = slot.getItem();
			stack = resultStack.copy();

			if (!this.moveItemStackTo(resultStack, 1, 37, false))
				return ItemStack.EMPTY;

			slot.onQuickCraft(resultStack, stack);
		} else {
			if (!this.moveItemStackTo(stack, 0, 1, false))
				return ItemStack.EMPTY;
		}

		return stack;
	}

	@Override
	public boolean stillValid(Player player) {
		return AbstractContainerMenu.stillValid(context, player, SevenElementsBlocks.INFUSION_TABLE);
	}

	public boolean canInfuse(Player player) {
		return (player.experienceLevel >= REQUIRED_LEVEL || player.hasInfiniteMaterials()) && this.getResultSlot().hasItem();
	}

	public boolean infuse(Player player) {
		if (!this.canInfuse(player) || !(player instanceof final ServerPlayer serverPlayer)) return false;

		final Slot slot = this.slots.get(0);

		if (slot == null || !slot.hasItem()) return false;

		final ItemStack stack = slot.getItem();
		final Element infusedElement = ElementalInfusionComponent.generateAndApplyInfusion(stack, player.level()).getA();

		if (!player.hasInfiniteMaterials()) serverPlayer.giveExperienceLevels(-REQUIRED_LEVEL);

		slot.setByPlayer(stack);
		slot.setChanged();

		ServerPlayNetworking.send(serverPlayer, new FinishElementalInfusionS2CPayload(this));
		SevenElementsCriteria.ELEMENTAL_INFUSION.trigger(serverPlayer, stack, infusedElement);

		return true;
	}

	public LockableSlot getResultSlot() {
		// Should always be not null, if null, something wrong happened.
		return ClassInstanceUtil.castOrNull(this.getSlot(0), LockableSlot.class);
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (!this.canInfuse(player)) return false;

		return this.infuse(player);
	}

	@Override
	public void removed(Player player) {
		this.clearContainer(player, this.output);
	}
}

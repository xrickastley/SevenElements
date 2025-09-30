package io.github.xrickastley.sevenelements.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public final class LockableSlot extends Slot {
	private boolean isLocked = false;

	public LockableSlot(Inventory inventory, int index, int x, int y) {
	   super(inventory, index, x, y);
	}

	public boolean lock() {
		if (isLocked) return false;

		return this.isLocked = true;
	}

	public boolean unlock() {
		if (!isLocked) return false;

		this.isLocked = false;

		return true;
	}

	public boolean isLocked() {
		return isLocked;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return super.canInsert(stack) && !isLocked;
	}

	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		return super.canTakeItems(playerEntity) && !isLocked;
	}

	@Override
	public boolean canTakePartial(PlayerEntity player) {
		return super.canTakePartial(player) && !isLocked;
	}

	@Override
	public ItemStack takeStack(int amount) {
		return this.isLocked && amount > 0
			? ItemStack.EMPTY
			: super.takeStack(amount);
	}

	@Override
	public ItemStack insertStack(ItemStack stack, int count) {
		return isLocked
			? stack
			: super.insertStack(stack, count);
	}
}

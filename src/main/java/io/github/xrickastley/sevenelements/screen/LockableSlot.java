package io.github.xrickastley.sevenelements.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class LockableSlot extends Slot {
	private boolean isLocked = false;

	public LockableSlot(Container inventory, int index, int x, int y) {
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
	public boolean mayPlace(ItemStack stack) {
		return super.mayPlace(stack) && !isLocked;
	}

	@Override
	public boolean mayPickup(Player playerEntity) {
		return super.mayPickup(playerEntity) && !isLocked;
	}

	@Override
	public boolean allowModification(Player player) {
		return super.allowModification(player) && !isLocked;
	}

	@Override
	public ItemStack remove(int amount) {
		return this.isLocked && amount > 0
			? ItemStack.EMPTY
			: super.remove(amount);
	}

	@Override
	public ItemStack safeInsert(ItemStack stack, int count) {
		return isLocked
			? stack
			: super.safeInsert(stack, count);
	}
}

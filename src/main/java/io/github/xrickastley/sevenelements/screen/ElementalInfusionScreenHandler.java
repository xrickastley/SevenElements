package io.github.xrickastley.sevenelements.screen;

import java.util.List;

import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.component.ElementalInfusionComponent;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication.Type;
import io.github.xrickastley.sevenelements.element.ElementalApplications;
import io.github.xrickastley.sevenelements.element.InternalCooldownContext;
import io.github.xrickastley.sevenelements.element.InternalCooldownTag;
import io.github.xrickastley.sevenelements.element.InternalCooldownType;
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
import net.minecraft.util.math.random.Random;

public final class ElementalInfusionScreenHandler extends ScreenHandler {
	private static final List<Element> ELEMENTS = List.of(Element.PYRO, Element.HYDRO, Element.ANEMO, Element.ELECTRO, Element.DENDRO, Element.CRYO, Element.GEO);
	private static final List<Double> GAUGE_UNITS = List.of(1.0, 1.5, 2.0);
	private static final int REQUIRED_LEVEL = 10;

	private final ScreenHandlerContext context;
	private final CraftingResultInventory output = new CraftingResultInventory();
	private final Random RANDOM = Random.create();

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
		return (player.experienceLevel >= REQUIRED_LEVEL || player.isCreative()) && this.getResultSlot().hasStack();
	}

	public boolean infuse(PlayerEntity player) {
		if (!this.canInfuse(player) || !(player instanceof final ServerPlayerEntity serverPlayer)) return false;

		final Slot slot = this.slots.get(0);

		if (slot == null || !slot.hasStack()) return false;

		final ItemStack stack = slot.getStack();
		final Element element = ELEMENTS.get(RANDOM.nextInt(ELEMENTS.size()));

		ElementalInfusionComponent.applyInfusion(
			stack,
			ElementalApplications.builder()
				.setType(Type.GAUGE_UNIT)
				.setElement(element)
				.setGaugeUnits(GAUGE_UNITS.get(RANDOM.nextInt(GAUGE_UNITS.size()))),
			InternalCooldownContext.builder()
				.setTag(InternalCooldownTag.of("seven-elements:elemental_infusion"))
				.setType(InternalCooldownType.DEFAULT)
		);

		if (!player.isCreative()) serverPlayer.setExperienceLevel(serverPlayer.experienceLevel - REQUIRED_LEVEL);

		slot.setStack(stack);
		slot.markDirty();

		ServerPlayNetworking.send(serverPlayer, new FinishElementalInfusionS2CPayload(this));
		SevenElementsCriteria.ELEMENTAL_INFUSION.trigger(serverPlayer, stack, element);

		return true;
	}

	public LockableSlot getResultSlot() {
		// Should always be not null, if null, something wrong happened.
		return ClassInstanceUtil.castOrNull(this.getSlot(0), LockableSlot.class);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (!this.canInfuse(player)) return false;

		return this.infuse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		this.dropInventory(player, this.output);
	}
}

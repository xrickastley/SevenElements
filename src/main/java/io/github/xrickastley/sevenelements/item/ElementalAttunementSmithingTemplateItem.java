package io.github.xrickastley.sevenelements.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.registry.SevenElementsBlockTags;
import io.github.xrickastley.sevenelements.util.TextHelper;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ElementalAttunementSmithingTemplateItem extends Item {
	private static final Map<ResourceKey<LootTable>, LootPool.Builder> MODIFIED_LOOT_POOLS = new HashMap<>();

	public static final AttunementPityCounterReference PYRO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.PYRO,
		SevenElements.identifier("pyro_attunement"),
		0.025,
		1_800,
		0.25,
		() -> new ItemStack(SevenElementsItems.PYRO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference HYDRO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.HYDRO,
		SevenElements.identifier("hydro_attunement"),
		0.025,
		900,
		0.25,
		() -> new ItemStack(SevenElementsItems.HYDRO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference ANEMO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.ANEMO,
		SevenElements.identifier("anemo_attunement"),
		0.025,
		1_800,
		0.25,
		() -> new ItemStack(SevenElementsItems.ANEMO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference ELECTRO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.ELECTRO,
		SevenElements.identifier("electro_attunement"),
		0,
		6,
		50,
		() -> new ItemStack(SevenElementsItems.ELECTRO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference DENDRO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.DENDRO,
		SevenElements.identifier("dendro_attunement"),
		0.001,
		2000,
		1.6,
		() -> new ItemStack(SevenElementsItems.DENDRO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference CRYO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.CRYO,
		SevenElements.identifier("cryo_attunement"),
		0.025,
		900,
		0.25,
		() -> new ItemStack(SevenElementsItems.CRYO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	public static final AttunementPityCounterReference GEO_ATTUNEMENT_PITY = new AttunementPityCounterReference(
		Element.GEO,
		SevenElements.identifier("geo_attunement"),
		0.025,
		900,
		0.25,
		() -> new ItemStack(SevenElementsItems.GEO_ATTUNEMENT_SMITHING_TEMPLATE)
	);

	ElementalAttunementSmithingTemplateItem(Item.Properties settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
		super.inventoryTick(stack, world, entity, slot);

		if (entity instanceof final Player player && !world.isClientSide())
			this.playerInventoryTick(stack, world, player);
	}

	private void playerInventoryTick(ItemStack stack, Level world, Player entity) {
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.PYRO_ATTUNEMENT_PITY,
			player -> player.level().dimensionTypeRegistration().is(BuiltinDimensionTypes.NETHER) && player.sevenelements$isFullySubmergedIn(FluidTags.LAVA)
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.HYDRO_ATTUNEMENT_PITY,
			player -> player.getAirSupply() <= 0 && player.sevenelements$isFullySubmergedIn(FluidTags.WATER)
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.ANEMO_ATTUNEMENT_PITY,
			player -> !player.onGround() && player.getInBlockState().isAir()
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.CRYO_ATTUNEMENT_PITY,
			player -> player.getPercentFrozen() >= 1
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.GEO_ATTUNEMENT_PITY,
			player -> player.isInWall() && player.getEyeY() <= 0 && player.level().getBlockState(BlockPos.containing(player.getEyePosition())).is(SevenElementsBlockTags.PROGRESSES_GEO_ATTUNEMENT)
		);
	}

	private void tickAttunementMethod(Player player, AttunementPityCounterReference pityCounter, Predicate<Player> predicate) {
		if (predicate.test(player))
			pityCounter.roll(player);
		else
			pityCounter.reset(player);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
		super.appendHoverText(stack, context, displayComponent, textConsumer, type);

		TextHelper.wrapLines(
			Component.translatable("item.seven-elements.elemental_attunement_smithing_template.tip").withStyle(ChatFormatting.GRAY),
			125
		).forEach(textConsumer::accept);
	}

	private static void registerLootTableModification(ResourceKey<LootTable> lootTableKey, LootPool.Builder lootPool) {
		MODIFIED_LOOT_POOLS.put(lootTableKey, lootPool);
	}

	static void registerLootTableModifications() {
		registerLootTableModification(
			BuiltInLootTables.ANCIENT_CITY,
			LootPool.lootPool()
				.add(
					LootItem
						.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
						.setWeight(3)
				)
				.add(
					EmptyLootItem
						.emptyItem()
						.setWeight(37)
				)
		);

		registerLootTableModification(
			BuiltInLootTables.ABANDONED_MINESHAFT,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(79))
		);

		registerLootTableModification(
			BuiltInLootTables.BASTION_TREASURE,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(39))
		);

		registerLootTableModification(
			BuiltInLootTables.DESERT_PYRAMID,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(39))
		);

		registerLootTableModification(
			BuiltInLootTables.JUNGLE_TEMPLE,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(63))
		);

		registerLootTableModification(
			BuiltInLootTables.TRIAL_CHAMBERS_INTERSECTION,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(49))
		);

		registerLootTableModification(
			BuiltInLootTables.SHIPWRECK_TREASURE,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(124))
		);

		registerLootTableModification(
			BuiltInLootTables.SIMPLE_DUNGEON,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(79))
		);

		registerLootTableModification(
			BuiltInLootTables.STRONGHOLD_LIBRARY,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(39))
		);

		registerLootTableModification(
			BuiltInLootTables.VILLAGE_WEAPONSMITH,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(199))
		);

		registerLootTableModification(
			BuiltInLootTables.WOODLAND_MANSION,
			LootPool.lootPool()
				.add(LootItem.lootTableItem(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.add(EmptyLootItem.emptyItem().setWeight(31))
		);

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && MODIFIED_LOOT_POOLS.containsKey(key))
				tableBuilder.withPool(MODIFIED_LOOT_POOLS.get(key));
		});
	}

	@ApiStatus.Internal
	public static record AttunementPityCounterReference(Element attunement, Identifier counter, double baseChance, int pityStart, double chancePerPity, Supplier<ItemStack> reward) {
		public boolean roll(Player player) {
			if (!(player instanceof final ServerPlayer serverPlayer))
				return false;

			if (!serverPlayer.getInventory().hasAnyOf(Set.of(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE)))
				return false;

			final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(player);

			component.incrementPityCounter(counter);

			if (component.getOwner().level().getRandom().nextDouble() < component.getChanceFromPityCounter(counter, baseChance / 100, pityStart, chancePerPity / 100)) {
				component.resetPityCounter(counter);
				this.giveReward(serverPlayer);

				return true;
			} else return false;
		}

		public void reset(Player player) {
			final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(player);

			component.resetPityCounter(counter);
		}

		private void giveReward(ServerPlayer serverPlayer) {
			SevenElementsCriteria.PERFORM_ATTUNEMENT.trigger(serverPlayer, attunement);

			serverPlayer.getInventory().clearOrCountMatchingItems(stack -> stack.is(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE), 1, serverPlayer.inventoryMenu.getCraftSlots());

			final ItemStack reward = this.reward.get();

			if (serverPlayer.getInventory().add(reward)) {
				serverPlayer.containerMenu.broadcastChanges();
				serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
				return;
			}

			final ItemEntity itemEntity = serverPlayer.drop(reward, false);

			if (itemEntity != null) {
				itemEntity.setNoPickUpDelay();
				itemEntity.setTarget(serverPlayer.getUUID());
			}
		}
	}

	static {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (state.is(SevenElementsBlockTags.PROGRESSES_DENDRO_ATTUNEMENT))
				ElementalAttunementSmithingTemplateItem.DENDRO_ATTUNEMENT_PITY.roll(player);
		});
	}
}

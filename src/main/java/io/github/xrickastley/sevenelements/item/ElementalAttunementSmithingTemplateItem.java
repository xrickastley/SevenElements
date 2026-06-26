package io.github.xrickastley.sevenelements.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.advancement.criterion.SevenElementsCriteria;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.component.ElementComponentImpl;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.registry.SevenElementsBlockTags;
import io.github.xrickastley.sevenelements.util.Functions;
import io.github.xrickastley.sevenelements.util.TextHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

public class ElementalAttunementSmithingTemplateItem extends Item {
	private static final Map<RegistryKey<LootTable>, LootPool.Builder> MODIFIED_LOOT_POOLS = new HashMap<>();
	
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

	ElementalAttunementSmithingTemplateItem() {
		super(
			new Item.Settings()
				.rarity(Rarity.RARE)
		);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (entity instanceof final PlayerEntity player && !world.isClient)
			this.playerInventoryTick(stack, world, player);
	}

	private void playerInventoryTick(ItemStack stack, World world, PlayerEntity entity) {
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.PYRO_ATTUNEMENT_PITY,
			player -> player.getWorld().getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER) && player.sevenelements$isFullySubmergedIn(FluidTags.LAVA)
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.HYDRO_ATTUNEMENT_PITY,
			player -> player.getAir() <= 0 && player.sevenelements$isFullySubmergedIn(FluidTags.WATER)
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.ANEMO_ATTUNEMENT_PITY,
			player -> !player.isOnGround() && player.getBlockStateAtPos().isAir()
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.CRYO_ATTUNEMENT_PITY,
			player -> player.getFreezingScale() >= 1
		);
		this.tickAttunementMethod(
			entity,
			ElementalAttunementSmithingTemplateItem.GEO_ATTUNEMENT_PITY,
			player -> player.isInsideWall() && player.getEyeY() <= 0 && player.getWorld().getBlockState(BlockPos.ofFloored(player.getEyePos())).isIn(SevenElementsBlockTags.PROGRESSES_GEO_ATTUNEMENT)
		);
	}

	private void tickAttunementMethod(PlayerEntity player, AttunementPityCounterReference pityCounter, Predicate<PlayerEntity> predicate) {
		if (predicate.test(player))
			pityCounter.roll(player);
		else
			pityCounter.reset(player);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);

		tooltip.addAll(
			TextHelper.wrapLines(
				Text.translatable("item.seven-elements.elemental_attunement_smithing_template.tip").formatted(Formatting.GRAY),
				125
			)
		);
	}

	private static void registerLootTableModification(RegistryKey<LootTable> lootTableKey, LootPool.Builder lootPool) {
		MODIFIED_LOOT_POOLS.put(lootTableKey, lootPool);
	}

	static void registerLootTableModifications() {
		registerLootTableModification(
			LootTables.ANCIENT_CITY_CHEST,
			LootPool.builder()
				.with(
					ItemEntry
						.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2)))
						.weight(3)
				)
				.with(
					EmptyEntry
						.builder()
						.weight(37)
				)
		);

		registerLootTableModification(
			LootTables.ABANDONED_MINESHAFT_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(79))
		);

		registerLootTableModification(
			LootTables.BASTION_TREASURE_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(39))
		);

		registerLootTableModification(
			LootTables.DESERT_PYRAMID_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(39))
		);

		registerLootTableModification(
			LootTables.JUNGLE_TEMPLE_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(63))
		);

		registerLootTableModification(
			LootTables.TRIAL_CHAMBERS_INTERSECTION_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(49))
		);

		registerLootTableModification(
			LootTables.SHIPWRECK_TREASURE_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(124))
		);

		registerLootTableModification(
			LootTables.SIMPLE_DUNGEON_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(79))
		);

		registerLootTableModification(
			LootTables.STRONGHOLD_LIBRARY_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(39))
		);

		registerLootTableModification(
			LootTables.VILLAGE_WEAPONSMITH_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(199))
		);

		registerLootTableModification(
			LootTables.WOODLAND_MANSION_CHEST,
			LootPool.builder()
				.with(ItemEntry.builder(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE))
				.with(EmptyEntry.builder().weight(31))
		);

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && MODIFIED_LOOT_POOLS.containsKey(key))
				tableBuilder.pool(MODIFIED_LOOT_POOLS.get(key));
		});
	}

	@ApiStatus.Internal
	public static record AttunementPityCounterReference(Element attunement, Identifier counter, double baseChance, int pityStart, double chancePerPity, Supplier<ItemStack> reward) {
		public boolean roll(PlayerEntity player) {
			if (!(player instanceof final ServerPlayerEntity serverPlayer)) 
				return false;

			if (!serverPlayer.getInventory().containsAny(Set.of(SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE)))
				return false;

			final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(player);

			component.incrementPityCounter(counter);

			if (component.getOwner().getWorld().getRandom().nextDouble() < component.getChanceFromPityCounter(counter, baseChance / 100, pityStart, chancePerPity / 100)) {
				component.resetPityCounter(counter);
				this.giveReward(serverPlayer);
				
				return true;
			} else return false;
		}

		public void reset(PlayerEntity player) {
			final ElementComponentImpl component = (ElementComponentImpl) ElementComponent.KEY.get(player);

			component.resetPityCounter(counter);
		}

		private void giveReward(ServerPlayerEntity serverPlayer) {
			SevenElementsCriteria.PERFORM_ATTUNEMENT.trigger(serverPlayer, attunement);

			serverPlayer.getInventory().remove(Functions.withArgument(ItemStack::isOf, SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE), 1, serverPlayer.playerScreenHandler.getCraftingInput());
			
			final ItemStack reward = this.reward.get();

			if (serverPlayer.getInventory().insertStack(reward)) {
				serverPlayer.currentScreenHandler.sendContentUpdates();
				serverPlayer.playerScreenHandler.onContentChanged(serverPlayer.getInventory());
				return;
			}

			final ItemEntity itemEntity = serverPlayer.dropItem(reward, false);

			if (itemEntity != null) {
				itemEntity.resetPickupDelay();
				itemEntity.setOwner(serverPlayer.getUuid());
			}
		}
	}

	static {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (state.isIn(SevenElementsBlockTags.PROGRESSES_DENDRO_ATTUNEMENT))
				ElementalAttunementSmithingTemplateItem.DENDRO_ATTUNEMENT_PITY.roll(player);
		});
	}
}

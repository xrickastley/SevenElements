package io.github.xrickastley.sevenelements.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntriesAll;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class SevenElementsItems {
	public static final Item INFUSION_TABLE = register(SevenElementsBlocks.INFUSION_TABLE, TallBlockItem::new);

	public static final Item ELEMENTAL_RUNE = register("elemental_rune");
	public static final Item ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE = register("elemental_attunement_smithing_template", ElementalAttunementSmithingTemplateItem::new, settings -> settings.rarity(Rarity.RARE));
	public static final Item PYRO_ATTUNEMENT_SMITHING_TEMPLATE = register("pyro_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.PYRO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item HYDRO_ATTUNEMENT_SMITHING_TEMPLATE = register("hydro_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.HYDRO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item ANEMO_ATTUNEMENT_SMITHING_TEMPLATE = register("anemo_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.ANEMO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item ELECTRO_ATTUNEMENT_SMITHING_TEMPLATE = register("electro_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.ELECTRO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item DENDRO_ATTUNEMENT_SMITHING_TEMPLATE = register("dendro_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.DENDRO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item CRYO_ATTUNEMENT_SMITHING_TEMPLATE = register("cryo_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.CRYO, settings), settings -> settings.rarity(Rarity.RARE));
	public static final Item GEO_ATTUNEMENT_SMITHING_TEMPLATE = register("geo_attunement_smithing_template", settings -> SevenElementsSmithingTemplateItem.of(Element.GEO, settings), settings -> settings.rarity(Rarity.RARE));

	public static void register() {
		registerLootTableModifications();

		ModifyEntryHandler.addAfter(ItemGroups.FUNCTIONAL, Items.CRAFTING_TABLE, SevenElementsItems.INFUSION_TABLE);
		ModifyEntryHandler.addAllAfter(ItemGroups.INGREDIENTS, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
			SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.PYRO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.HYDRO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.ANEMO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.ELECTRO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.DENDRO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.CRYO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.GEO_ATTUNEMENT_SMITHING_TEMPLATE,
			SevenElementsItems.ELEMENTAL_RUNE
		);
	}

	public static Item register(Block block) {
		return register(block, BlockItem::new);
	}

	@SuppressWarnings("deprecation")
	public static Item register(Block block, BiFunction<Block, Item.Settings, Item> factory) {
		return register(block.getRegistryEntry().registryKey().getValue(), settings -> factory.apply(block, settings), UnaryOperator.identity());
	}

	public static Item register(String id) {
		return register(SevenElements.identifier(id), Item::new, UnaryOperator.identity());
	}

	public static Item register(String id, Function<Item.Settings, Item> factory, UnaryOperator<Item.Settings> settingsOperator) {
		return register(SevenElements.identifier(id), factory, settingsOperator);
	}

	public static Item register(Identifier id, Function<Item.Settings, Item> factory, UnaryOperator<Item.Settings> settingsOperator) {
		final RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

		return register(
			key,
			factory.apply(settingsOperator.apply(new Item.Settings().registryKey(key)))
		);
	}

	public static Item register(RegistryKey<Item> key, Item item) {
		return Registry.register(Registries.ITEM, key, item);
	}

	private static void registerLootTableModifications() {
		ElementalAttunementSmithingTemplateItem.registerLootTableModifications();
	}

	public static class ModifyEntryHandler implements ModifyEntriesAll {
		private static final ModifyEntryHandler INSTANCE = new ModifyEntryHandler();
		private static final Multimap<RegistryKey<ItemGroup>, Entry> ENTRIES = HashMultimap.create();

		public static void prepend(RegistryKey<ItemGroup> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.PREPEND, null));
		}

		public static void prependAll(RegistryKey<ItemGroup> group, Item... items) {
			ENTRIES.get(group).add(new Entry(List.of(items), EntryType.PREPEND, null));
		}

		public static void add(RegistryKey<ItemGroup> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD, null));
		}

		public static void addAll(RegistryKey<ItemGroup> group, Item... items) {
			ENTRIES.get(group).add(new Entry(List.of(items), EntryType.ADD, null));
		}

		public static void addBefore(RegistryKey<ItemGroup> group, Item before, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_BEFORE, before));
		}

		public static void addAllBefore(RegistryKey<ItemGroup> group, Item before, Item... items) {
			ENTRIES.get(group).add(new Entry(List.of(items), EntryType.ADD_BEFORE, before));
		}

		public static void addAfter(RegistryKey<ItemGroup> group, Item after, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_AFTER, after));
		}

		public static void addAllAfter(RegistryKey<ItemGroup> group, Item after, Item... items) {
			ENTRIES.get(group).add(new Entry(List.of(items), EntryType.ADD_AFTER, after));
		}

		@Override
		public void modifyEntries(ItemGroup group, FabricItemGroupEntries entries) {
			ModifyEntryHandler.ENTRIES
				.get(Registries.ITEM_GROUP.getKey(group).orElseThrow())
				.forEach(Functions.withArgument(Entry::add, entries));
		}
	}

	private static class Entry {
		final List<ItemStack> items;
		final EntryType type;
		final @Nullable Item relativeItem;

		Entry(Item item, EntryType type, @Nullable Item relativeItem) {
			this(List.of(item), type, relativeItem);
		}

		Entry(List<Item> items, EntryType type, @Nullable Item relativeItem) {
			this.items = items.stream().map(ItemStack::new).toList();
			this.type = type;
			this.relativeItem = relativeItem;
		}

		private void add(FabricItemGroupEntries entries) {
			this.type.add(this, entries);
		}
	}

	private static enum EntryType {
		PREPEND {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entry.items.forEach(entries::prepend);
			}
		},
		ADD {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.addAll(entry.items);
			}
		},
		ADD_AFTER {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.addAfter(entry.relativeItem, entry.items);
			}
		},
		ADD_BEFORE {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.addBefore(entry.relativeItem, entry.items);
			}
		};

		private EntryType() {}

		abstract void add(Entry entry, FabricItemGroupEntries entries);
	}

	static {
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register(ModifyEntryHandler.INSTANCE);
	}
}

package io.github.xrickastley.sevenelements.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.util.Functions;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntriesAll;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class SevenElementsItems {
	public static final Item INFUSION_TABLE = new TallBlockItem(SevenElementsBlocks.INFUSION_TABLE, new Item.Settings());

	public static final Item ELEMENTAL_RUNE = new Item(new Item.Settings());
	public static final Item ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE = new ElementalAttunementSmithingTemplateItem();
	public static final Item PYRO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.PYRO);
	public static final Item HYDRO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.HYDRO);
	public static final Item ANEMO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.ANEMO);
	public static final Item ELECTRO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.ELECTRO);
	public static final Item DENDRO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.DENDRO);
	public static final Item CRYO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.CRYO);
	public static final Item GEO_ATTUNEMENT_SMITHING_TEMPLATE = SevenElementsSmithingTemplateItem.of(Element.GEO);

	public static void register() {
		register("infusion_table", SevenElementsItems.INFUSION_TABLE);
		register("elemental_rune", SevenElementsItems.ELEMENTAL_RUNE);
		register("elemental_attunement_smithing_template", SevenElementsItems.ELEMENTAL_ATTUNEMENT_SMITHING_TEMPLATE);
		register("pyro_attunement_smithing_template", SevenElementsItems.PYRO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("hydro_attunement_smithing_template", SevenElementsItems.HYDRO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("anemo_attunement_smithing_template", SevenElementsItems.ANEMO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("electro_attunement_smithing_template", SevenElementsItems.ELECTRO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("dendro_attunement_smithing_template", SevenElementsItems.DENDRO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("cryo_attunement_smithing_template", SevenElementsItems.CRYO_ATTUNEMENT_SMITHING_TEMPLATE);
		register("geo_attunement_smithing_template", SevenElementsItems.GEO_ATTUNEMENT_SMITHING_TEMPLATE);

		registerLootTableModifications();

		ModifyEntryHandler.addAfter(ItemGroups.FUNCTIONAL, Items.CRAFTING_TABLE, SevenElementsItems.INFUSION_TABLE);
		ModifyEntryHandler.addAllAfter(ItemGroups.INGREDIENTS, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
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

	public static void register(String id, Item item) {
		Registry.register(Registries.ITEM, SevenElements.identifier(id), item);
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

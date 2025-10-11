package io.github.xrickastley.sevenelements.factory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.block.SevenElementsBlocks;
import io.github.xrickastley.sevenelements.util.Functions;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntriesAll;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class SevenElementsItems {
	public static final Item INFUSION_TABLE = new TallBlockItem(
		SevenElementsBlocks.INFUSION_TABLE,
		new Item.Settings()
		    .useBlockPrefixedTranslationKey()
		    .registryKey(SevenElements.registryKey(RegistryKeys.ITEM, "infusion_table"))
	);

	public static void register() {
		register("infusion_table", SevenElementsItems.INFUSION_TABLE);

		ModifyEntryHandler.addAfter(ItemGroups.FUNCTIONAL, SevenElementsItems.INFUSION_TABLE, Items.CRAFTING_TABLE);
	}

	public static void register(String id, Item item) {
		Registry.register(Registries.ITEM, SevenElements.identifier(id), item);
	}

	public static class ModifyEntryHandler implements ModifyEntriesAll {
		private static final ModifyEntryHandler INSTANCE = new ModifyEntryHandler();
		private static final Multimap<RegistryKey<ItemGroup>, Entry> ENTRIES = HashMultimap.create();

		public static void prepend(RegistryKey<ItemGroup> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.PREPEND, null));
		}

		public static void add(RegistryKey<ItemGroup> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD, null));
		}

		public static void addBefore(RegistryKey<ItemGroup> group, Item item, Item before) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_BEFORE, before));
		}

		public static void addAfter(RegistryKey<ItemGroup> group, Item item, Item after) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_AFTER, after));
		}

		@Override
		public void modifyEntries(ItemGroup group, FabricItemGroupEntries entries) {
			ModifyEntryHandler.ENTRIES
				.get(Registries.ITEM_GROUP.getKey(group).orElseThrow())
				.forEach(Functions.withArgument(Entry::add, entries));
		}
	}

	private static class Entry {
		final Item item;
		final EntryType type;
		final @Nullable Item relativeItem;

		Entry(Item item, EntryType type, @Nullable Item relativeItem) {
			this.item = item;
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
				entries.prepend(entry.item);
			}
		},
		ADD {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.add(entry.item);
			}
		},
		ADD_AFTER {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.addAfter(entry.relativeItem, entry.item);
			}
		},
		ADD_BEFORE {
			@Override
			void add(Entry entry, FabricItemGroupEntries entries) {
				entries.addBefore(entry.relativeItem, entry.item);
			}
		};

		private EntryType() {}

		abstract void add(Entry entry, FabricItemGroupEntries entries);
	}

	static {
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register(ModifyEntryHandler.INSTANCE);
	}
}

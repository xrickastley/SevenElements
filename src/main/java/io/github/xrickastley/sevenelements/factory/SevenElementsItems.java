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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SevenElementsItems {
	public static final Item INFUSION_TABLE = new DoubleHighBlockItem(
		SevenElementsBlocks.INFUSION_TABLE,
		new Item.Properties()
		    .useBlockDescriptionPrefix()
		    .setId(SevenElements.registryKey(Registries.ITEM, "infusion_table"))
	);

	public static void register() {
		register("infusion_table", SevenElementsItems.INFUSION_TABLE);

		ModifyEntryHandler.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, SevenElementsItems.INFUSION_TABLE, Items.CRAFTING_TABLE);
	}

	public static void register(String id, Item item) {
		Registry.register(BuiltInRegistries.ITEM, SevenElements.identifier(id), item);
	}

	public static class ModifyEntryHandler implements ModifyEntriesAll {
		private static final ModifyEntryHandler INSTANCE = new ModifyEntryHandler();
		private static final Multimap<ResourceKey<CreativeModeTab>, Entry> ENTRIES = HashMultimap.create();

		public static void prepend(ResourceKey<CreativeModeTab> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.PREPEND, null));
		}

		public static void add(ResourceKey<CreativeModeTab> group, Item item) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD, null));
		}

		public static void addBefore(ResourceKey<CreativeModeTab> group, Item item, Item before) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_BEFORE, before));
		}

		public static void addAfter(ResourceKey<CreativeModeTab> group, Item item, Item after) {
			ENTRIES.get(group).add(new Entry(item, EntryType.ADD_AFTER, after));
		}

		@Override
		public void modifyEntries(CreativeModeTab group, FabricItemGroupEntries entries) {
			ModifyEntryHandler.ENTRIES
				.get(BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(group).orElseThrow())
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
				entries.accept(entry.item);
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

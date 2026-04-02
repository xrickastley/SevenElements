package io.github.xrickastley.sevenelements.block;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class SevenElementsBlocks {
	public static final Block INFUSION_TABLE = new InfusionTableBlock();

	public static void register() {
		register("infusion_table", SevenElementsBlocks.INFUSION_TABLE);
	}

	public static void register(String id, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, SevenElements.identifier(id), block);
	}
}

package io.github.xrickastley.sevenelements.block;

import io.github.xrickastley.sevenelements.SevenElements;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SevenElementsBlocks {
	public static final Block INFUSION_TABLE = new InfusionTableBlock();

	public static void register() {
		register("infusion_table", SevenElementsBlocks.INFUSION_TABLE);
	}

	public static void register(String id, Block block) {
		Registry.register(Registries.BLOCK, SevenElements.identifier(id), block);
	}
}

package io.github.xrickastley.sevenelements.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Mixin(SmithingTemplateItem.class)
public interface SmithingTemplateItemAccessor {
	@Invoker("getNetheriteUpgradeEmptyBaseSlotTextures")
	public static List<Identifier> sevenelements$invokeGetNetheriteUpgradeEmptyBaseSlotTextures() { throw new AssertionError(); }
	@Accessor("TITLE_FORMATTING")
	public static Formatting getTitleFormatting() { throw new AssertionError(); }
	@Accessor("DESCRIPTION_FORMATTING")
	public static Formatting getDescriptionFormatting() { throw new AssertionError(); }
}

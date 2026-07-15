package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.sevenelements.interfaces.SevenElementsOrderedSubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;

@Mixin(OrderedSubmitNodeCollector.class)
public interface OrderedSubmitNodeCollectorMixin extends SevenElementsOrderedSubmitNodeCollector {}

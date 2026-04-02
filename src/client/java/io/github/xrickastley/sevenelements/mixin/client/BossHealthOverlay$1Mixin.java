package io.github.xrickastley.sevenelements.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.xrickastley.sevenelements.SevenElementsClient;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

@Mixin(targets={"net.minecraft.client.gui.components.BossHealthOverlay$1"})
public class BossHealthOverlay$1Mixin {
	@ModifyArg(
		method = "add",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
		),
		index = 1
	)
	private Object loadEntityIfExistent(Object obj) {
		return SevenElementsClient.SYNC_BOSS_BAR_ENTITY_HANDLER.setPossibleEntity(ClassInstanceUtil.cast(obj));
	}
}

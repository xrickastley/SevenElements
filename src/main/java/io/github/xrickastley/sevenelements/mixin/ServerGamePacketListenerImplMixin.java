package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Input;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = Integer.MIN_VALUE)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(
		method = "handlePlayerCommand",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsMountJump(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
		if (packet.getAction() == Action.START_RIDING_JUMP && this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "handleMovePlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		if (this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	// Frozen **must** disable movements and actions.
	@WrapOperation(
		method = "handlePlayerAction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V"
		)
	)
	private void frozenPreventsBlockInteraction(ServerPlayerGameMode instance, BlockPos pos, net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, Operation<Void> original) {
		if (!this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			original.call(instance, pos, action, direction, worldHeight, sequence);
	}

	// Frozen **must** disable movements and actions.
	@WrapOperation(
		method = "handlePlayerInput",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;setLastClientInput(Lnet/minecraft/world/entity/player/Input;)V"
		)
	)
	private void frozenPreventsPlayerInput(ServerPlayer instance, Input playerInput, Operation<Void> original) {
		if (!this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			original.call(instance, playerInput);
	}

	@Inject(
		method = "handleUseItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerBlockInteraction(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
		if (this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "handleInteract",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerEntityInteraction(ServerboundInteractPacket packet, CallbackInfo ci) {
		if (this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "handleUseItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerItemInteraction(ServerboundUseItemPacket packet, CallbackInfo ci) {
		if (this.player.hasEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}
}

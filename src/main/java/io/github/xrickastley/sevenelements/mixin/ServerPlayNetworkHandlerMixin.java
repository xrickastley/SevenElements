package io.github.xrickastley.sevenelements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.effect.SevenElementsStatusEffects;

@Mixin(value = ServerPlayNetworkHandler.class, priority = Integer.MIN_VALUE)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(
		method = "onClientCommand",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsMountJump(ClientCommandC2SPacket packet, CallbackInfo ci) {
		if (packet.getMode() == Mode.START_RIDING_JUMP && this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "onPlayerMove",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
		if (this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	// Frozen **must** disable movements and actions.
	@WrapOperation(
		method = "onPlayerAction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V"
		)
	)
	private void frozenPreventsBlockInteraction(ServerPlayerInteractionManager instance, BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, Operation<Void> original) {
		if (!this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			original.call(instance, pos, action, direction, worldHeight, sequence);
	}

	// Frozen **must** disable movements and actions.
	@WrapOperation(
		method = "onPlayerInput",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateInput(FFZZ)V"
		)
	)
	private void frozenPreventsPlayerInput(ServerPlayerEntity instance, float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking, Operation<Void> original) {
		if (!this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			original.call(instance, sidewaysSpeed, forwardSpeed, jumping, sneaking);
	}

	@Inject(
		method = "onPlayerInteractBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;updateSequence(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerBlockInteraction(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
		if (this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "onPlayerInteractEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerEntityInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
		if (this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}

	@Inject(
		method = "onPlayerInteractItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;updateSequence(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true,
		order = Integer.MIN_VALUE // Frozen **must** disable movements and actions.
	)
	private void frozenPreventsPlayerItemInteraction(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
		if (this.player.hasStatusEffect(SevenElementsStatusEffects.FROZEN))
			ci.cancel();
	}
}

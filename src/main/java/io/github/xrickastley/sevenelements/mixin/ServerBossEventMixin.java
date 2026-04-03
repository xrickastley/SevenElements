package io.github.xrickastley.sevenelements.mixin;

import java.util.Collection;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.sevenelements.networking.SyncBossBarEntityS2CPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

@Mixin(ServerBossEvent.class)
public abstract class ServerBossEventMixin extends BossEvent {
	@Shadow
	@Final
	private Set<ServerPlayer> players;

	@Shadow
	public abstract Collection<ServerPlayer> getPlayers();

	public ServerBossEventMixin(Component displayName, BossEvent.BossBarColor color, BossEvent.BossBarOverlay style) {
		super(null, displayName, color, style);

		throw new AssertionError();
	}

	@Unique
	@Override
	public void sevenelements$setEntity(LivingEntity entity) {
		super.sevenelements$setEntity(entity);

		final SyncBossBarEntityS2CPayload packet = new SyncBossBarEntityS2CPayload(this, this.sevenelements$getEntity());

		this.getPlayers()
			.forEach(player -> ServerPlayNetworking.send(player, packet));
	}

	@Inject(
		method = "addPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
		)
	)
	private void sendEntitySync(ServerPlayer player, CallbackInfo ci) {
		if (this.sevenelements$getEntity() == null) return;

		final SyncBossBarEntityS2CPayload packet = new SyncBossBarEntityS2CPayload(this, this.sevenelements$getEntity());

		ServerPlayNetworking.send(player, packet);
	}
}

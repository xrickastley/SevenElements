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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Mixin(ServerBossBar.class)
public abstract class ServerBossBarMixin extends BossBar {
	@Shadow
	@Final
	private Set<ServerPlayerEntity> players;

	@Shadow
	public abstract Collection<ServerPlayerEntity> getPlayers();

	public ServerBossBarMixin(Text displayName, BossBar.Color color, BossBar.Style style) {
		super(MathHelper.randomUuid(), displayName, color, style);

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
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	private void sendEntitySync(ServerPlayerEntity player, CallbackInfo ci) {
		if (this.sevenelements$getEntity() == null) return;

		final SyncBossBarEntityS2CPayload packet = new SyncBossBarEntityS2CPayload(this, this.sevenelements$getEntity());

		ServerPlayNetworking.send(player, packet);
	}
}

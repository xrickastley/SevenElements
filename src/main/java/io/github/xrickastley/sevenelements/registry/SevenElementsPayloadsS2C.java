package io.github.xrickastley.sevenelements.registry;

import io.github.xrickastley.sevenelements.entity.CrystallizeShardEntity.SyncCrystallizeShardTypeS2CPayload;
import io.github.xrickastley.sevenelements.entity.DendroCoreEntity.SyncDendroCoreS2CPayload;
import io.github.xrickastley.sevenelements.networking.FinishElementalInfusionS2CPayload;
import io.github.xrickastley.sevenelements.networking.ShowElectroChargeS2CPayload;
import io.github.xrickastley.sevenelements.networking.ShowElementalDamageS2CPayload;
import io.github.xrickastley.sevenelements.networking.ShowElementalReactionS2CPayload;
import io.github.xrickastley.sevenelements.networking.SyncBossBarEntityS2CPayload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public final class SevenElementsPayloadsS2C {
	public static void register() {
		register(ShowElectroChargeS2CPayload.ID, ShowElectroChargeS2CPayload.CODEC);
		register(ShowElementalDamageS2CPayload.ID, ShowElementalDamageS2CPayload.CODEC);
		register(ShowElementalReactionS2CPayload.ID, ShowElementalReactionS2CPayload.CODEC);
		register(SyncBossBarEntityS2CPayload.ID, SyncBossBarEntityS2CPayload.CODEC);
		register(SyncDendroCoreS2CPayload.ID, SyncDendroCoreS2CPayload.CODEC);
		register(SyncCrystallizeShardTypeS2CPayload.ID, SyncCrystallizeShardTypeS2CPayload.CODEC);
		register(FinishElementalInfusionS2CPayload.ID, FinishElementalInfusionS2CPayload.CODEC);
	}

	public static <T extends CustomPayload> void register(CustomPayload.Id<T> id, PacketCodec<RegistryByteBuf, T> codec) {
		PayloadTypeRegistry.playS2C().register(id, codec);
	}
}

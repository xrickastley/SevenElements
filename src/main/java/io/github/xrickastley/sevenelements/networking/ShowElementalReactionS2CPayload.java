package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ShowElementalReactionS2CPayload(Vec3 pos, ElementalReaction reaction) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ShowElementalReactionS2CPayload> ID = new CustomPacketPayload.Type<>(
		SevenElements.identifier("s2c/show_elemental_reaction")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ShowElementalReactionS2CPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(Vec3.CODEC), ShowElementalReactionS2CPayload::pos,
		ByteBufCodecs.fromCodec(SevenElementsRegistries.ELEMENTAL_REACTION.byNameCodec()), ShowElementalReactionS2CPayload::reaction,
		ShowElementalReactionS2CPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}

package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.reaction.ElementalReaction;
import io.github.xrickastley.sevenelements.registry.SevenElementsRegistries;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

public record ShowElementalReactionS2CPayload(Vec3d pos, ElementalReaction reaction) implements CustomPayload {
	public static final CustomPayload.Id<ShowElementalReactionS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/show_elemental_reaction")
	);

	public static final PacketCodec<RegistryByteBuf, ShowElementalReactionS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.codec(Vec3d.CODEC), ShowElementalReactionS2CPayload::pos,
		PacketCodecs.codec(SevenElementsRegistries.ELEMENTAL_REACTION.getCodec()), ShowElementalReactionS2CPayload::reaction,
		ShowElementalReactionS2CPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}

package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

public record ShowElementalDamageS2CPayload(Vec3d pos, Element element, float amount, boolean crit) implements CustomPayload {
	public static final CustomPayload.Id<ShowElementalDamageS2CPayload> ID = new CustomPayload.Id<>(
		SevenElements.identifier("s2c/show_elemental_damage")
	);

	public static final PacketCodec<RegistryByteBuf, ShowElementalDamageS2CPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.codec(Vec3d.CODEC), ShowElementalDamageS2CPayload::pos,
		PacketCodecs.codec(Element.CODEC), ShowElementalDamageS2CPayload::element,
		PacketCodecs.FLOAT, ShowElementalDamageS2CPayload::amount,
		PacketCodecs.BOOL, ShowElementalDamageS2CPayload::crit,
		ShowElementalDamageS2CPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}

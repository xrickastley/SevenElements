package io.github.xrickastley.sevenelements.networking;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.element.Element;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ShowElementalDamageS2CPayload(Vec3 pos, Element element, float amount, boolean crit) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ShowElementalDamageS2CPayload> ID = new CustomPacketPayload.Type<>(
		SevenElements.identifier("s2c/show_elemental_damage")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ShowElementalDamageS2CPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(Vec3.CODEC), ShowElementalDamageS2CPayload::pos,
		ByteBufCodecs.fromCodec(Element.CODEC), ShowElementalDamageS2CPayload::element,
		ByteBufCodecs.FLOAT, ShowElementalDamageS2CPayload::amount,
		ByteBufCodecs.BOOL, ShowElementalDamageS2CPayload::crit,
		ShowElementalDamageS2CPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}

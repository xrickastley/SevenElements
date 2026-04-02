package io.github.xrickastley.sevenelements.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PayloadHandler<T extends CustomPacketPayload> extends PlayPayloadHandler<T> {
	CustomPacketPayload.Type<T> getPayloadId();
}

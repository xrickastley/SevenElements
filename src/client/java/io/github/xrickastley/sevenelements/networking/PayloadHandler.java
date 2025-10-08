package io.github.xrickastley.sevenelements.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;

public interface PayloadHandler<T extends SevenElementsPayload> extends PlayPacketHandler<T> {
	SevenElementsPayload.Id<T> getPayloadId();
}

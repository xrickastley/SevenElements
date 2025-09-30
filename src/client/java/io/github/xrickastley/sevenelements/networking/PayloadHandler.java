package io.github.xrickastley.sevenelements.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.packet.CustomPayload;

public interface PayloadHandler<T extends CustomPayload> extends PlayPayloadHandler<T> {
	CustomPayload.Id<T> getPayloadId();
}

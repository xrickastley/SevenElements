package io.github.xrickastley.sevenelements.networking;

import com.mojang.serialization.Codec;

import org.slf4j.Logger;

import io.github.xrickastley.sevenelements.SevenElements;
import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

// Custom backport of Minecraft 1.20.5's CustomPayload class.
public interface SevenElementsPayload extends FabricPacket {
	public Id<? extends SevenElementsPayload> getId();

	public Codec<? extends SevenElementsPayload> getCodec();

	@Override
	default PacketType<?> getType() {
		final Logger LOGGER = SevenElements.sublogger(SevenElementsPayload.class);

		return PacketType.create(
			this.getId().id(),
			buf -> this.getCodec().parse(NbtOps.INSTANCE, buf.readNbt()).resultOrPartial(LOGGER::error).orElseThrow()
		);
	}

	default void write(PacketByteBuf buf) {
		final Logger LOGGER = SevenElements.sublogger(SevenElementsPayload.class);

		buf.writeNbt(
			(NbtCompound) this.getCodec()
				.encodeStart(NbtOps.INSTANCE, ClassInstanceUtil.cast(this))
				.resultOrPartial(LOGGER::error)
				.orElseThrow()
		);
	}

	public record Id<T extends SevenElementsPayload>(Identifier id, Codec<T> codec) {
		public PacketType<T> Type() {
			final Logger LOGGER = SevenElements.sublogger(SevenElementsPayload.class);

			return PacketType.create(
				this.id(),
				buf -> this.codec().parse(NbtOps.INSTANCE, buf.readNbt()).resultOrPartial(LOGGER::error).orElseThrow()
			);
		}
	}
}

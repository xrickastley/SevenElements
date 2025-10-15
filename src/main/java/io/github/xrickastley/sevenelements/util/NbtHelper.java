package io.github.xrickastley.sevenelements.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.helpers.MessageFormatter;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;

public class NbtHelper {
	public static <T> T get(NbtCompound nbt, String key, Codec<T> codec) {
		return nbt.get(key, codec)
			.orElseThrow(() -> new NbtException("Expected value for required field: " + key));
	}

	public static <T> List<T> getList(NbtCompound nbt, String key, Codec<T> entryCodec) {
		final List<T> result = new ArrayList<>();
		final NbtList list = nbt.getListOrEmpty("Owners");

		for (int i = 0; i < list.size(); i++) {
			final NbtElement element = list.get(i);

			entryCodec
				.parse(NbtOps.INSTANCE, element)
				.resultOrPartial(createErrorConsumer("Failed to read entry at array[{}] ({})", i, element))
				.ifPresent(result::add);
		}
		
		return result;
	}

	public static <T> void putList(NbtCompound nbt, String key, Codec<T> entryCodec, List<T> list) {
		final NbtList result = new NbtList();
		
		for (int i = 0; i < list.size(); i++) {
			final T element = list.get(i);

			entryCodec
				.encodeStart(NbtOps.INSTANCE, element)
				.resultOrPartial(createErrorConsumer("Failed to write entry at array[{}] ({})", i, element))
				.ifPresent(result::add);
		}
		
		nbt.put(key, result);
	}

	private static Consumer<String> createErrorConsumer(final String message, final Object... arguments) {
		return err -> {
			throw new NbtException(MessageFormatter.arrayFormat(message, arguments) + ": " + err);
		};
	}
}

package io.github.xrickastley.sevenelements.util;

import com.mojang.serialization.Codec;

import java.util.List;

import net.minecraft.nbt.NbtException;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.level.storage.ValueOutput;

public class ViewHelper {
	public static <T> T get(ValueInput view, String key, Codec<T> codec) {
		return view.read(key, codec)
			.orElseThrow(() -> new NbtException("Expected value for required field: " + key));
	}

	public static <T> List<T> getList(ValueInput view, String key, Codec<T> entryCodec) {
		return view
			.listOrEmpty(key, entryCodec)
			.stream()
			.toList();
	}

	public static <T> void putList(ValueOutput view, String key, Codec<T> entryCodec, List<T> list) {
		final TypedOutputList<T> listAppender = view.list(key, entryCodec);

		list.forEach(listAppender::add);
	}
}

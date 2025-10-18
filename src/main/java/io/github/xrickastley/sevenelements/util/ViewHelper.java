package io.github.xrickastley.sevenelements.util;

import com.mojang.serialization.Codec;

import java.util.List;

import net.minecraft.nbt.NbtException;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView.ListAppender;
import net.minecraft.storage.WriteView;

public class ViewHelper {
	public static <T> T get(ReadView view, String key, Codec<T> codec) {
		return view.read(key, codec)
			.orElseThrow(() -> new NbtException("Expected value for required field: " + key));
	}

	public static <T> List<T> getList(ReadView view, String key, Codec<T> entryCodec) {
		return view
			.getTypedListView(key, entryCodec)
			.stream()
			.toList();
	}

	public static <T> void putList(WriteView view, String key, Codec<T> entryCodec, List<T> list) {
		final ListAppender<T> listAppender = view.getListAppender(key, entryCodec);

		list.forEach(listAppender::add);
	}
}

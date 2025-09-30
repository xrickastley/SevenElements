package io.github.xrickastley.sevenelements.element;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.JavaScriptUtil;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * An {@code InternalCooldownTag} is a class used for holding unique instances of Internal Cooldown
 * Tags per session. <br> <br>
 *
 * This is a simple class that wraps a {@code String} around itself that creates new
 * {@code InternalCooldownTag} instances for new "tags" and returns already existing
 * {@code InternalCooldownTag} instances for cached "tags".
 */
public final class InternalCooldownTag {
	private static final Map<String, InternalCooldownTag> INSTANCES = new ConcurrentHashMap<>();
	public static final InternalCooldownTag NONE = new InternalCooldownTag(null);
	public static final Codec<InternalCooldownTag> CODEC = Codec.STRING.xmap(InternalCooldownTag::of, InternalCooldownTag::getTag);

	private final String tag;

	private InternalCooldownTag(final @Nullable String tag) {
		this.tag = JavaScriptUtil.nullishCoalesing(tag, "");

		InternalCooldownTag.INSTANCES.put(this.tag, this);
	}

	/**
	 * Gets an {@code InternalCooldownTag} instance based on a possible tag. <br> <br>
	 *
	 * If the tag is {@code null}, {@link InternalCooldownTag#NONE} is returned. Otherwise, a
	 * cached or new instance of {@code InternalCooldownTag} is returned depending on the given
	 * {@code String} tag.
	 *
	 * @param tag The tag to get an {@code InternalCooldownTag} instance of.
	 */
	public static InternalCooldownTag of(final @Nullable String tag) {
		return tag == null || tag.isEmpty()
			? InternalCooldownTag.NONE
			: InternalCooldownTag.tag(tag);
	}

	/**
	 * Gets a "null" {@code InternalCooldownTag} instance. <br> <br>
	 *
	 * This is equivalent to {@link InternalCooldownTag#NONE}.
	 */
	public static InternalCooldownTag none() {
		return InternalCooldownTag.NONE;
	}

	/**
	 * Gets an {@code InternalCooldownTag} instance based on a given tag. <br> <br>
	 *
	 * A cached or new instance of {@code InternalCooldownTag} is returned depending on the given
	 * {@code String} tag.
	 *
	 * @param tag The tag to get an {@code InternalCooldownTag} instance of.
	 */
	public static InternalCooldownTag tag(final String tag) {
		Objects.requireNonNull(tag);

		return INSTANCES.containsKey(tag)
			? INSTANCES.get(tag)
			: new InternalCooldownTag(tag);
	}

	public static void applySuggestions(SuggestionsBuilder builder) {
		InternalCooldownTag.INSTANCES
			.keySet()
			.stream()
			.map(string -> string.contains(" ") ? '"' + string + '"' : string)
			.forEach(builder::suggest);
	}

	/**
	 * Gets the tag of this {@code InternalCooldownTag}
	 */
	public String getTag() {
		return this.tag;
	}

	@Override
	public String toString() {
		return String.format("InternalCooldownTag@%s[%s]", Integer.toHexString(this.hashCode()), this.tag.isEmpty() ? "NULL" : this.tag);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (!(obj instanceof final InternalCooldownTag tag)) return false;

		return this.tag.equals(tag.tag);
	}

	public Text getText() {
		return this.getText(Formatting.WHITE);
	}

	public Text getText(Formatting... formatting) {
		return this != InternalCooldownTag.NONE
			? Text.literal(this.tag).formatted(formatting)
			: Text.literal("none").formatted(Formatting.RED);
	}
}

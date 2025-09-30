package io.github.xrickastley.sevenelements.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * An {@code InternalCooldownContext} is a class used for holding the various {@code InternalCooldown}
 * components together in a single class. It also contains utility functions for getting the
 * {@code InternalCooldown} instance from an {@code InternalCooldownContext} instance.
 *
 * <h2>Definitions</h2>
 *
 * <b>ICD</b> is the commonly used shorthand term for <b>Internal Cooldown</b>, a system that
 * handles how frequent elements (of the same type) are applied by the same attack. <br> <br>
 *
 * The <b>origin</b> is the {@code LivingEntity} the elemental attack comes from. Each {@code LivingEntity}
 * has their own seperate Internal Cooldown, ensuring that same {@code tag} and {@code type}(s) that come
 * from different entities are handled respectively, allowing the Internal Cooldown of entity "A"
 * to not conflict with that of entity "B". <br> <br>
 *
 * The <b>tag</b>, referred to as the {@link InternalCooldownTag} in this codebase, is internally a
 * {@code String} that identifes the attack. This tag can be shared or differ across different
 * elemental attacks, and is one of the factors that dictate Whether two elemental attacks (with the
 * same Element) share ICD. <br> <br>
 *
 * The <b>type</b>, referred to as the {@link InternalCooldownType} in this codebase, is an instance of
 * {@code InternalCooldownType} that controls the "Reset Interval" and the "Gauge Sequence" for an attack.
 * Much like the tag, this type can be shared or differ across different elemental attacks, and is also
 * one of the factors that dictate Whether two elemental attacks (with the same Element) share ICD. <br> <br>
 *
 * <h2>Sharing ICD</h2>
 *
 * For ICD to be "shared", the two elemental attacks in question must:
 *
 * <ul>
 * 	<li>Be applied from the same {@code origin}</li>
 * 	<li>Have the same {@code tag}</li>
 * 	<li>Have the same {@code type}</li>
 * </ul>
 *
 * If all three conditions above are fulfilled, the two elemental attacks in question will share
 * ICD.
 *
 * <h2>"Removing" ICD</h2>
 *
 * Internal Cooldown is <b>only</b> taken into account when an {@code origin} exists. If no {@code origin}
 * exists, the element is regarded to have been applied by <i>other</i> means, such as the environment. <br> <br>
 *
 * The Internal Cooldown may still be taken into account without an {@code origin} by using
 * {@link InternalCooldownContext#forced() InternalCooldownContext#forced}. All forced Internal Cooldowns
 * without an {@code origin} are grouped under this holder.
 */
public final class InternalCooldownContext {
	private static final UUID FORCE_HANDLER_UUID = UUID.fromString("301286c7-09eb-4ce0-a11a-94303bbc795e");

	private final @Nullable LivingEntity origin;
	private final InternalCooldownTag tag;
	private final InternalCooldownType type;
	private final boolean force;

	public InternalCooldownContext(final @Nullable LivingEntity origin, final InternalCooldownTag tag, final InternalCooldownType type) {
		this(origin, tag, type, false);
	}

	private InternalCooldownContext(final @Nullable LivingEntity origin, final InternalCooldownTag tag, final InternalCooldownType type, final boolean force) {
		this.origin = origin;
		this.tag = tag;
		this.type = type;
		this.force = force;
	}

	public static InternalCooldownContext ofNone() {
		return new InternalCooldownContext(null, InternalCooldownTag.NONE, InternalCooldownType.NONE);
	}

	public static InternalCooldownContext ofNone(final @Nullable Entity origin) {
		return InternalCooldownContext.ofNone(ClassInstanceUtil.castOrNull(origin, LivingEntity.class));
	}

	public static InternalCooldownContext ofNone(final @Nullable LivingEntity origin) {
		return new InternalCooldownContext(origin, InternalCooldownTag.NONE, InternalCooldownType.DEFAULT);
	}

	public static InternalCooldownContext ofDefault(final @Nullable Entity origin, final @Nullable String tag) {
		return InternalCooldownContext.ofDefault(ClassInstanceUtil.castOrNull(origin, LivingEntity.class), InternalCooldownTag.of(tag));
	}

	public static InternalCooldownContext ofDefault(final @Nullable Entity origin, final InternalCooldownTag tag) {
		return InternalCooldownContext.ofDefault(ClassInstanceUtil.castOrNull(origin, LivingEntity.class), tag);
	}

	public static InternalCooldownContext ofDefault(final @Nullable LivingEntity origin, final @Nullable String tag) {
		return InternalCooldownContext.ofDefault(origin, InternalCooldownTag.of(tag));
	}

	public static InternalCooldownContext ofDefault(final @Nullable LivingEntity origin, final InternalCooldownTag tag) {
		return new InternalCooldownContext(origin, tag, InternalCooldownType.DEFAULT);
	}

	public static InternalCooldownContext ofType(final @Nullable Entity origin, final @Nullable String tag, final InternalCooldownType type) {
		return InternalCooldownContext.ofType(ClassInstanceUtil.castOrNull(origin, LivingEntity.class), InternalCooldownTag.of(tag), type);
	}

	public static InternalCooldownContext ofType(final @Nullable Entity origin, final InternalCooldownTag tag, final InternalCooldownType type) {
		return InternalCooldownContext.ofType(ClassInstanceUtil.castOrNull(origin, LivingEntity.class), tag, type);
	}

	public static InternalCooldownContext ofType(final @Nullable LivingEntity origin, final @Nullable String tag, final InternalCooldownType type) {
		return InternalCooldownContext.ofType(origin, InternalCooldownTag.of(tag), type);
	}

	public static InternalCooldownContext ofType(final @Nullable LivingEntity origin, final InternalCooldownTag tag, final InternalCooldownType type) {
		return new InternalCooldownContext(origin, tag, type);
	}

	public static InternalCooldownContext.Builder builder() {
		return new InternalCooldownContext.Builder();
	}

	private @Nullable UUID getUuid() {
		return origin == null && force
			? FORCE_HANDLER_UUID
			: ClassInstanceUtil.mapOrNull(origin, Entity::getUuid);
	}

	public InternalCooldownContext withOrigin(@Nullable LivingEntity origin) {
		return new InternalCooldownContext(origin, this.tag, this.type);
	}

	public InternalCooldownContext forced() {
		return new InternalCooldownContext(this.origin, this.tag, this.type, true);
	}

	public boolean hasInternalCooldown() {
		return this.getUuid() != null;
	}

	public @Nullable InternalCooldown getInternalCooldown(final ElementHolder holder) {
		final UUID uuid = this.getUuid();

		return uuid != null
			? this.getInternalCooldown(holder.internalCooldowns.computeIfAbsent(uuid, e -> new InternalCooldownHolder(holder.getOwner())))
			: null;
	}

	public InternalCooldown getInternalCooldown(final InternalCooldownHolder holder) {
		return holder.getInternalCooldown(this.tag, this.type);
	}

	public boolean hasOrigin() {
		return this.origin != null;
	}

	public @Nullable LivingEntity getOrigin() {
		return this.origin;
	}

	@Override
	public String toString() {
		return String.format("InternalCooldownContext@%s[origin=%s,tag=%s,type=%s]", Integer.toHexString(this.hashCode()), origin == null ? "null" : origin, tag, type);
	}

	public static final class Builder {
		public static final Codec<InternalCooldownContext.Builder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			InternalCooldownTag.CODEC.fieldOf("tag").forGetter(i -> i.tag),
			InternalCooldownType.REGISTRY_CODEC.optionalFieldOf("type", RegistryEntry.of(InternalCooldownType.DEFAULT)).forGetter(i -> RegistryEntry.of(i.type))
		).apply(instance, InternalCooldownContext.Builder::new));

		private InternalCooldownTag tag;
		private InternalCooldownType type;

		private Builder() {}

		private Builder(InternalCooldownTag tag, RegistryEntry<InternalCooldownType> type) {
			this.tag = tag;
			this.type = type.value();
		}

		public static InternalCooldownContext.Builder ofNone() {
			return new InternalCooldownContext.Builder()
				.setTag(InternalCooldownTag.NONE)
				.setType(InternalCooldownType.NONE);
		}

		public static InternalCooldownTag getTag(Builder builder) {
			return builder.tag;
		}

		public static InternalCooldownType getType(Builder builder) {
			return builder.type;
		}

		public InternalCooldownContext.Builder setTag(InternalCooldownTag tag) {
			this.tag = tag;

			return this;
		}

		public InternalCooldownContext.Builder setType(InternalCooldownType type) {
			this.type = type;

			return this;
		}

		public InternalCooldownContext build(@Nullable LivingEntity origin) {
			Objects.requireNonNull(this.tag);
			Objects.requireNonNull(this.type);

			return InternalCooldownContext.ofType(origin, tag, type);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (!(obj instanceof final Builder builder)) return false;

			return Objects.equals(this.type, builder.type)
				&& Objects.equals(this.tag, builder.tag);
		}
	}
}

package io.github.xrickastley.sevenelements.element;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;

public final class InternalCooldownHolder {
	private final LivingEntity owner;
	private final InternalCooldown ICD_NONE;
	private final Map<String, InternalCooldown> cooldowns = new HashMap<>();

	InternalCooldownHolder(final LivingEntity owner) {
		this.owner = owner;
		this.ICD_NONE = InternalCooldown.none(this);
	}

	public InternalCooldown getInternalCooldown(@Nullable String tag, InternalCooldownType type) {
		return this.getInternalCooldown(InternalCooldownTag.of(tag), type);
	}

	public InternalCooldown getInternalCooldown(InternalCooldownTag tag, InternalCooldownType type) {
		if (tag == InternalCooldownTag.NONE || tag.getTag() == null) return this.ICD_NONE;

		final String id = InternalCooldown.getIdentifier(tag, type);

		return cooldowns.computeIfAbsent(id, _key -> new InternalCooldown(this, tag, type));
	}

	LivingEntity getOwner() {
		return owner;
	}
}

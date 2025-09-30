package io.github.xrickastley.sevenelements.element;

/**
 * An {@code InternalCooldown} is a class used for holding the various {@code InternalCooldown}
 * components together in a single class. <br> <br>
 *
 * In addition to this, this class is also the handler for the Internal Cooldown system. <br> <br>
 *
 * When the Internal Cooldown is considered active through {@link InternalCooldown#isInInternalCooldown()},
 * elements should <b>not</b> be applied or refreshed. <br> <br>
 *
 * To read more about the {@code InternalCooldown}, refer to the {@link InternalCooldownContext} class.
 */
public final class InternalCooldown {
	private final InternalCooldownHolder holder;
	private final InternalCooldownType type;
	private final InternalCooldownTag tag;
	private int cooldown = 0;
	private int totalHits = 0;

	InternalCooldown(final InternalCooldownHolder holder, final InternalCooldownTag tag, final InternalCooldownType type) {
		this.holder = holder;
		this.type = type;
		this.tag = tag;
	}

	public static String getIdentifier(InternalCooldownTag tag, InternalCooldownType type) {
		return tag.getTag() + type.getId().toString();
	}

	public static String getIdentifier(String tag, InternalCooldownType type) {
		return tag + type.getId().toString();
	}

	static InternalCooldown none(final InternalCooldownHolder holder) {
		return new InternalCooldown(holder, InternalCooldownTag.NONE, InternalCooldownType.NONE);
	}

	/**
	 * Checks if an element can be applied based on this Internal Cooldown. <br> <br>
	 *
	 * For registering a hit and checking the Internal Cooldown after, use
	 * {@link InternalCooldown#handleInternalCooldown} instead.
	 *
	 * @see InternalCooldown#handleInternalCooldown
	 */
	public boolean isInInternalCooldown() {
		return tag.getTag() != null && holder.getOwner().age >= cooldown || totalHits > type.getGaugeSequence();
	}

	/**
	 * Handles the Internal Cooldown. <br> <br>
	 *
	 * Upon using this method, a hit is registered, and it returns Whether the element can
	 * be applied. <br> <br>
	 *
	 * For only checking the Internal Cooldown without registering a hit, use
	 * {@link InternalCooldown#isInInternalCooldown} instead.
	 *
	 * @see InternalCooldown#isInInternalCooldown
	 */
	public boolean handleInternalCooldown() {
		if (tag.getTag() == null) return true;

 		if (holder.getOwner().age >= cooldown) {
			cooldown = holder.getOwner().age + type.getResetInterval();
			totalHits = 1;

			return true;
		} else if (totalHits >= type.getGaugeSequence()) {
			totalHits = 1;

			return true;
		} else {
			totalHits++;

			return false;
		}
	}
}

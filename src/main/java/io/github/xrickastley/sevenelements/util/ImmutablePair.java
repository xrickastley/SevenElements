package io.github.xrickastley.sevenelements.util;

import net.minecraft.util.Pair;

public final class ImmutablePair<A, B> extends Pair<A, B> {
	public ImmutablePair(final A left, final B right) {
		super(left, right);
	}

	public static <A, B> Pair<A, B> of(final Pair<A, B> pair) {
		return new ImmutablePair<>(pair.getLeft(), pair.getRight());
	}

	@Override
	public void setLeft(A left) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRight(B right) {
		throw new UnsupportedOperationException();
	}
}

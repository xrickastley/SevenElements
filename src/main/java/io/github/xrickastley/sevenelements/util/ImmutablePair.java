package io.github.xrickastley.sevenelements.util;

import net.minecraft.util.Tuple;

public final class ImmutablePair<A, B> extends Tuple<A, B> {
	public ImmutablePair(final A left, final B right) {
		super(left, right);
	}

	public static <A, B> Tuple<A, B> of(final Tuple<A, B> pair) {
		return new ImmutablePair<>(pair.getA(), pair.getB());
	}

	@Override
	public void setA(A left) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setB(B right) {
		throw new UnsupportedOperationException();
	}
}

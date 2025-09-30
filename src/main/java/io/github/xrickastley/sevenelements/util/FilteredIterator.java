package io.github.xrickastley.sevenelements.util;

import java.util.Iterator;
import java.util.function.Predicate;

public final class FilteredIterator<T> implements Iterator<T> {
	private final Iterator<T> iterator;
	private final Predicate<? super T> predicate;
	private T next;
	private boolean hasNext;
	private boolean computed;
	private boolean canRemove;

	private FilteredIterator(Iterator<T> iterator, Predicate<? super T> predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
	}

	/**
	 * Creates a {@code FilteredIterator} instance from the provided {@code Iterator}. <br> <br>
	 *
	 * This {@code FilteredIterator} contains all elements of {@code iterator} such that for each
	 * element {@code e}, {@code predicate.test(e)} is {@code true} <br> <br>
	 *
	 * If an element {@code e} in the underlying {@code Iterator} exists such that
	 * {@code predicate.test(e)} is {@code false}, the element is ignored and is not removable with
	 * {@link Iterator#remove()}, retrievable with {@link Iterator#next()} or counted as an element
	 * with {@link Iterator#hasNext()}.
	 *
	 * @param <T> The type of the {@code Iterator} and the resulting {@code FilteredIterator}
	 * @param iterator The {@code Iterator} to create a {@code FilteredIterator} instance from.
	 * @param predicate The {@code Predicate} that determines whether an item is included.
	 */
	public static <T> FilteredIterator<T> of(Iterator<T> iterator, Predicate<? super T> predicate) {
		return new FilteredIterator<>(iterator, predicate);
	}

	private void computeNext() {
		if (computed) return;

		while (iterator.hasNext()) {
			final T value = iterator.next();

			if (!predicate.test(value)) continue;

			next = value;
			hasNext = true;
			computed = true;

			return;
		}

		next = null;
		hasNext = false;
		computed = true;
	}

	@Override
	public boolean hasNext() {
		computeNext();

		// Bad normally, but works in the case of clearStatusEffects
		this.canRemove = false;

		return hasNext;
	}

	@Override
	public T next() {
		computeNext();

		this.computed = false;
		this.canRemove = true;

		return this.next;
	}

	@Override
	public void remove() {
		if (!this.canRemove) throw new IllegalStateException("remove() can only be called once after next()");

		this.iterator.remove();
		this.canRemove = false;
	}
}

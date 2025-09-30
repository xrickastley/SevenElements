package io.github.xrickastley.sevenelements.util;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Wraps a {@link CopyOnWriteArrayList} around the JavaScript Array API, bringing the implementation of
 * JavaScript's array methods to Java.
 */
public class Array<T> implements List<T> {
	private CopyOnWriteArrayList<T> array = new CopyOnWriteArrayList<>();

	public Array() {}

	/**
	 * Creates an {@code Array} with the specified elements.
	 * @param elements The elements of the array.
	 */
	@SuppressWarnings("unchecked")
	public Array(T... elements) {
		array.addAll(Arrays.asList(elements));
	}

	/**
	 * Creates an {@code Array} from the specified {@link Iterable}.
	 * @param iterable The {@code Iterable} to create the array from.
	 */
	public Array(Iterable<T> iterable) {
		for (T element : iterable) array.add(element);
	}

	/**
	 * Creates an {@code Array} from the specified {@link Stream}. This consumes all the elements of the stream.
	 * @param stream The {@code Stream} to create the array from.
	 */
	public Array(Stream<T> stream) {
		stream.forEachOrdered(array::add);
	}

	/**
	 * Creates an {@code Array} from the specified {@link Iterator}. This consumes all the elements in the iterator.
	 * @param iterator The {@code Iterator} to create the array from.
	 */
	public Array(Iterator<T> iterator) {
		while (iterator.hasNext()) array.add(iterator.next());
	}

	/**
	 * Creates an {@code Array} from the specified {@link Collection}.
	 * @param collection The {@code Collection} to create the array from.
	 */
	public Array(Collection<T> collection) {
		this.array = new CopyOnWriteArrayList<>(collection);
	}

	/**
	 * Creates an {@code Array} from another {@code Array}. <br> <br>
	 *
	 * This array will have all the elements of {@code array}, but any changes made to this array will
	 * <strong>not</strong> reflect in the original array.
	 *
	 * @param array The {@code Array} to create the array from.
	 */
	public Array(Array<T> array) {
		this.array = new CopyOnWriteArrayList<>(array.array);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, appends the specified element to the end of this {@code Array}.
	 * {@inheritDoc}
	 */
	public boolean add(T e) {
		return this.array.add(e);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, inserts the specified element at the specified position in this {@code Array}.
	 * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
	 * {@inheritDoc}
	 */
	public void add(int index, T e) {
		this.array.add(index, e);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, appends all the elements in the specified collection to the end of this list,
	 * in the order that they are returned by the specified collection's iterator.
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(@NotNull Collection<? extends T> c) {
		return this.array.addAll(c);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, inserts all the elements in the specified collection into this list, starting
	 * at the specified position. Shifts the element currently at that position (if any) and any subsequent elements to the
	 * right (increases their indices). The new elements will appear in this list in the order that they are returned by the
	 * specified collection's iterator.
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends T> c) {
		return this.array.addAll(index, c);
	}

	@Override
	public T set(int index, T element) {
		return this.array.set(index, element);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method and an alias for {@link Array#length}, returns the length (the number of elements)
	 * in this {@code Array}. This is a number one higher than the highest index in the array.
	 *
	 * @see Array#length
	 */
	@Override
	public int size() {
		return this.length();
	}

	/**
	 * Returns the element located at the specified index.
	 * @param index The zero-based index of the desired element. A negative index will count back from the last element.
	 * @implNote Time complexity: {@code O(1)}.
	 */
	public T at(int index) {
		final int newIndex = normalizeIndex(index);

		if (newIndex >= this.array.size()) return null;

		return array.get(index);
	}

	/**
	 * An {@link CopyOnWriteArrayList} method, removes all the elements from this {@code Array}. The {@code Array} will
	 * be empty after this call returns.
	 */
	public void clear() {
		this.array.clear();
	}

	/**
	 * Combines two or more arrays. This returns a new {@code Array} without modifying any existing Arrays.
	 * @param items Additional items to add to the end of the array.
	 * @implNote Time complexity: {@code O(n)}.
	 */
	@SuppressWarnings("unchecked")
	public Array<T> concat(T... items) {
		final Array<T> concatArray = new Array<>(this.array);

		concatArray.array.addAll(Arrays.asList(items));

		return concatArray;
	}

	/**
	 * Combines two or more arrays. This returns a new {@code Array} without modifying any existing Arrays.
	 *
	 * @param arrays Additional arrays with the items to add at the end of the array. These arrays are
	 * iterated over, with their inner elements being added as elements of the array.
	 */
	@SuppressWarnings("unchecked")
	public Array<T> concat(T[]... arrays) {
		Array<T> concatArray = new Array<>(this.array);

		for (T[] array : arrays) concatArray.array.addAll(Arrays.asList(array));

		return concatArray;
	}

	/**
	 * Alias for {@link Array#includes}, returns {@code true} if this {@code Array} contains the specified element.
	 * More formally, returns {@code true} if and only if this {@code Array} contains at least one element {@code e} such
	 * that {@code Objects.equals(o, e)}.
	 *
	 * @param object The element to search for in this {@code Array}.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean contains(Object object) {
		return this.includes(object);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, returns {@code true} if this {@code Array} contains all the elements
	 * of the specified collection.
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return new HashSet<>(this.array).containsAll(c);
	}

	/**
	 * Determines whether any elements of this {@code Array} satisfy the specified predicate.
	 * @param predicate The predicate to apply to the elements of this {@code Array}.
	 * @see Array#allMatch
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean every(Predicate<? super T> predicate) {
		return this.array
			.stream()
			.allMatch(predicate);
	}

	/**
	 * Alias for {@link Array#every}, determines whether all the members of this {@code Array} satisfy the specified predicate.
	 * @param predicate The predicate to apply to the elements of this {@code Array}.
	 * @see Array#every
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean allMatch(Predicate<? super T> predicate) {
		return every(predicate);
	}

	/**
	 * Alias for {@link Array#some}, determines whether at least one element of this {@code Array} satisfy the specified predicate.
	 * @param predicate The predicate to apply to the elements of this {@code Array}.
	 * @see Array#some
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean anyMatch(Predicate<T> predicate) {
		return this.array
			.stream()
			.anyMatch(predicate);
	}

	/**
	 * Changes all array elements to {@code value} and returns the modified array.
	 * @param value The value to fill this array with.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Array<T> fill(T value) {
		return fill(value, 0, this.length() - 1);
	}

	/**
	 * Changes all array elements from {@code start} to {@code array.length() - 1} into {@code value} and returns the modified array.
	 * @param value The value to fill this array with.
	 * @param start The starting fill point of the array.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Array<T> fill(T value, int start) {
		return fill(value, start, this.length() - 1);
	}

	/**
	 * Changes all array elements from {@code start} to {@code end} into {@code value} and returns the modified array. <br> <br>
	 *
	 * @param value The value to fill this array with.
	 * @param start The starting fill index point of the array. If this value is negative, it is treated as {@code length + start}
	 * @param end The ending fill index point of the array. This element at this index is <strong>included</strong>
	 * and is also changed to {@code value}. If this value is negative, it is treated as {@code length + end}
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Array<T> fill(T value, int start, int end) {
		final int newStart = start < 0
			? start + this.length()
			: start;

		final int newEnd = end < 0
			? end + this.length()
			: end;

		for (int i = 0; i < array.size(); i++) {
			if (newStart <= i && i < newEnd) array.set(i, value);
		}

		return this;
	}

	/**
	 * Returns a new {@code Array} consisting of all the elements of this {@code Array} that satisfy the specified predicate.
	 * @param predicate The predicate to apply to the elements of this {@code Array}.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Array<T> filter(Predicate<? super T> predicate) {
		final Array<T> result = new Array<>();

		for (T e : this.array) if (predicate.test(e)) result.add(e);

		return result;
	}

	/**
	 * Returns the value of the first element in this {@code Array} that satisfies the given predicate, and {@code null}
	 * otherwise.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findAsOptional
	 * @implNote Time complexity: {@code O(n)}
	 */
	public T find(Predicate<? super T> predicate) {
		for (T element : this.array) {
			if (predicate.test(element)) return element;
		}

		return null;
	}

	/**
	 * Returns the value of the first element in this {@code Array} that satisfies the given predicate as
	 * an {@code Optional}.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#find
	 * @implNote Time complexity: {@code O(n)}.
	 */
	public Optional<T> findAsOptional(Predicate<? super T> predicate) {
		return Optional.ofNullable(
			this.find(predicate)
		);
	}

	/**
	 * A {@link Stream} method, returns an {@link Optional} describing the first element of this {@code Array}, or an empty
	 * {@code Optional} if the this {@code Array} is empty.
	 */
	public Optional<T> findFirst() {
		return Optional.ofNullable(this.at(0));
	}

	/**
	 * Returns the index of the first element in this {@code Array} that satisfies the given predicate, and {@code -1}
	 * otherwise.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findIndexAsOptional
	 * @implNote Time complexity: {@code O(n)}.
	 */
	public int findIndex(Predicate<? super T> predicate) {
		return this
			.findIndexAsOptional(predicate)
			.orElse(-1);
	}

	/**
	 * Returns the index of the first element in this {@code Array} that satisfies the given predicate as
	 * an {@code Optional}.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findIndex
	 * @implNote Time complexity: {@code O(n)}.
	 */
	public Optional<Integer> findIndexAsOptional(Predicate<? super T> predicate) {
		for (int i = 0; i < array.size(); i++) {
			if (predicate.test(array.get(i))) return Optional.of(i);
		}

		return Optional.empty();
	}

	/**
	 * Returns the value of the last element in this {@code Array} that satisfies the given predicate, and {@code null}
	 * otherwise.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findLastAsOptional
	 * @implNote Time complexity: {@code O(n)}
	 */
	public T findLast(Predicate<? super T> predicate) {
		return this
			.toReversed()
			.find(predicate);
	}

	/**
	 * Returns the value of the last element in this {@code Array} that satisfies the given predicate as
	 * an {@code Optional}.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findLast
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Optional<T> findLastAsOptional(Predicate<? super T> predicate) {
		return this
			.toReversed()
			.findAsOptional(predicate);
	}

	/**
	 * Returns the index of the last element in this {@code Array} that satisfies the given predicate, and {@code -1}
	 * otherwise.
	 *
	 * @param predicate The predicate to apply in finding an element from this {@code Array}.
	 * @see Array#findIndexAsOptional
	 * @implNote Time complexity: {@code O(n)}.
	 */
	public int findLastIndex(Predicate<? super T> predicate) {
		return this
			.toReversed()
			.findIndex(predicate);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method and an alias for {@link Array#at}, returns the element located at the specified
	 * index. {@inheritDoc}
	 *
	 * @see Array#at
	 */
	@Override
	public T get(int index) {
		return this.at(index);
	}

	/**
	 * Returns {@code true} if this {@code Array} contains the specified element. More formally, returns {@code true} if and
	 * only if this {@code Array} contains at least one element {@code e} such that {@code Objects.equals(o, e)}.
	 *
	 * @param object The element to search for in this {@code Array}.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean includes(Object object) {
		return array.contains(object);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in this {@code Array}, or {@code -1} if this
	 * {@code Array} does not contain the element. More formally, returns the lowest index {@code i} such that
	 * {@code Objects.equals(o, get(i))}, or {@code -1} if there is no such index.
	 *
	 * @param object The value to locate in this {@code Array}.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public int indexOf(Object object) {
		return array.indexOf(object);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, returns an iterator over the elements in this list in proper sequence. <br> <br>
	 *
	 * The returned iterator provides a snapshot of the state of the list when the iterator was constructed.
	 * No synchronization is needed while traversing the iterator. The iterator does <i>NOT</i> support the {@code remove}
	 * method.
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull Iterator<T> iterator() {
		return array.iterator();
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, returns {@code true} if this list contains no elements.
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return this.array.isEmpty();
	}

	/**
	 * Joins all the elements of this {@code Array} into a {@code String}, separated by commas.
	 */
	public String join() {
		return this.join(",");
	}

	/**
	 * Joins all the elements of this {@code Array} into a {@code String}, separated by the specified separator string.
	 * @param separator A string used to separate one element of the array from the next in the resulting string.
	 */
	public String join(String separator) {
		final Iterator<T> iterator = array.iterator();
		final StringBuilder sb = new StringBuilder();

		while (iterator.hasNext()) {
			sb.append(iterator.next());

			if (iterator.hasNext()) sb.append(separator);
		}

		return sb.toString();
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this {@code Array}, or {@code -1} if this
	 * {@code Array} does not contain the element. More formally, returns the highest index {@code i} such that
	 * {@code Objects.equals(o, get(i))}, or {@code -1} if there is no such index.
	 * @param object The value to locate in the array.
	 * @implNote Time complexity: {@code O(n)}
	 */
	public int lastIndexOf(Object object) {
		return this.array.lastIndexOf(object);
	}

	/**
	 * Returns the length (the number of elements) in this {@code Array}. This is a number one higher than the highest index
	 * in the array.
	 *
	 * @implNote Time complexity: {@code O(1)}.
	 */
	public int length() {
		return array.size();
	}

	@Override
	public @NotNull ListIterator<T> listIterator() {
		return this.array.listIterator();
	}

	@Override
	public @NotNull ListIterator<T> listIterator(int index) {
		return this.array.listIterator(index);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T @NotNull [] toArray() {
		return (T[]) this.array.toArray();
	}

	@Override
	public <U> U @NotNull [] toArray(U @NotNull [] a) {
		return this.array.toArray(a);
	}

	/**
	 * Returns a new {@code Array} consisting of the results of applying the given function to the elements of this {@code Array}.
	 *
	 * @param <R> The element type of the new {@code Array}.
	 * @param mapper A function that takes an element and returns a value.
	 */
	public <R> Array<R> map(Function<? super T, ? extends R> mapper) {
		return new Array<>(
			this.array
				.stream()
				.map(mapper)
		);
	}

	/**
	 * A {@link Stream} method, performs the specified action for each element in an array, then returns the {@code Array}.
	 * @param consumer The action to be performed for each element.
	 * @see Iterable#forEach
	 */
	public Array<T> peek(Consumer<? super T> consumer) {
		forEach(consumer);

		return this;
	}

	/**
	 * Removes the element at the end of this {@code Array} and returns it. <br> <br>
	 *
	 * If the array is empty, {@code null} is returned and the {@code Array} is not modified.
	 */
	public T pop() {
		return array.remove(array.size() - 1);
	}

	/**
	 * Appends new elements to the end of this {@code Array} and returns the new length of this {@code Array}.
	 * @param elements The new elements to add to this {@code Array}.
	 */
	@SuppressWarnings("unchecked")
	public int push(T... elements) {
		this.array.addAll(Arrays.asList(elements));

		return array.size();
	}

	/**
	 * Performs a reduction on the elements of this {@code Array}, using the provided {@code initialValue} and
	 * {@code accumulator} function. This is equivalent to:
	 * <pre>{@code
	 *U result = initialValue;
	 *for (T element : this.array)
	 *	result = accumulator.apply(result, element);
	 *return result;
	 *}</pre>
	 *
	 * @param <U> The type of the initial value, and the value {@code reducer} must return.
	 * @param accumulator A function that combines both the value and the current element.
	 * @param initialValue The initial value for the reducer function.
	 * @see Array#reduceRight
	 */
	public <U> U reduce(BiFunction<U, T, U> accumulator, U initialValue) {
		U value = initialValue;

		for (T element : this.array) value = accumulator.apply(value, element);

		return value;
	}

	/**
	 * A right-sided version of {@link Array#reduce}, performs a reduction on the elements of this {@code Array} starting from the
	 * last element to the first element, using the provided {@code initialValue} and {@code accumulator} function.
	 *
	 * @param <U> The type of the initial value, and the value {@code reducer} must return.
	 * @param accumulator A function that combines both the value and the current element.
	 * @param initialValue The initial value for the reducer function.
	 * @see Array#reduce
	 */
	public <U> U reduceRight(BiFunction<U, T, U> accumulator, U initialValue) {
		return this
			// .toReversed()
			.reduce(accumulator, initialValue);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return this.array.retainAll(c);
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return this.array.removeAll(c);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, removes the first occurrence of the specified element from this {@code Array},
	 * if it is present. <br> <br> {@inheritDoc}
	 */
	public boolean remove(Object o) {
		return this.array.remove(o);
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, removes the element at the specified position in this list and shifts
	 * any subsequent elements to the left (subtracts one from their indices). <br> <br> {@inheritDoc}
	 */
	public T remove(int index) {
		return this.array.remove(index);
	}

	/**
	 * Reverses the order of elements of this {@code Array}. This method mutates the current {@code Array}.
	 * @see Array#toReversed
	 */
	public Array<T> reverse() {
		final CopyOnWriteArrayList<T> reverseArray = new CopyOnWriteArrayList<>();

		for (int i = array.size() - 1; i >= 0; i--) reverseArray.add(array.get(i));

		this.array = reverseArray;

		return this;
	}

	/**
	 * Removes the first element from an array and returns it. <br> <br>
	 *
	 * If the array is empty, {@code null} is returned and the array is not modified.
	 */
	public T shift() {
		return array.remove(0);
	}

	/**
	 * Returns a copy of a section of an {@code Array} from {@code 0} to {@code array.length() - 1}. This is returned as a
	 * new {@code Array}, meaning that any modifications made to the returned {@code Array} are not reflected in the original
	 * {@code Array}. <br> <br>
	 */
	public Array<T> slice() {
		return slice(0, array.size());
	}

	/**
	 * Returns a copy of a section of an {@code Array} from {@code start} to {@code array.length() - 1}. This is returned as
	 * a new {@code Array}, meaning that any modifications made to the returned {@code Array} are not reflected in the original
	 * {@code Array}. <br> <br>
	 *
	 * For both {@code start} and {@code end}, a negative index can be used to indicate an offset from the end of the array.
	 * For example, {@code -2} refers to the second to last element of the array.
	 *
	 * @param start The beginning index of the specified portion of the array.
	 */
	public Array<T> slice(int start) {
		return slice(start, array.size());
	}

	/**
	 * Returns a copy of a section of an {@code Array}. This is returned as a new {@code Array}, meaning that any modifications
	 * made to the returned {@code Array} are not made in the original {@code Array}. <br> <br>
	 *
	 * For both {@code start} and {@code end}, a negative index can be used to indicate an offset from the end of the array.
	 * For example, {@code -2} refers to the second to last element of the array.
	 *
	 * @param start The beginning index of the specified portion of the array.
	 * @param end The end index of the specified portion of the array.
	 */
	public Array<T> slice(int start, int end) {
		final int newStart = normalizeIndex(start);
		final int newEnd = normalizeIndex(end);

		return this.filter(element -> newStart <= this.indexOf(element) && this.indexOf(element) < newEnd);
	}

	/**
	 * Determines whether at least one element of this {@code Array} satisfy the specified predicate.
	 * @param predicate The predicate to apply to the elements of this {@code Array}.
	 * @see Array#anyMatch
	 * @implNote Time complexity: {@code O(n)}
	 */
	public boolean some(Predicate<T> predicate) {
		return this.array
			.stream()
			.anyMatch(predicate);
	}

	/**
	 * A renamed implementation of
	 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort">Array.prototype.sort()</a>,
	 * sorts an array's elements' string value in ascending, ASCII character order.
	 */
	public Array<T> sortElements() {
		return this.sortElements((a, b) -> a.toString().compareTo(b.toString()));
	}

	/**
	 * A renamed implementation of
	 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort">Array.prototype.sort()</a>,
	 * sorts this {@code Array} in place according to the order induced by the specified {@code BiFunction}. The sort is
	 * <i>stable</i>: this method must not reorder equal elements. <br> <br>
	 *
	 * The resulting sort will be based on the value of {@code compareFn(a, b)}.
	 * <ul>
	 * 	<li>If {@code compareFn(a, b) < 0}, then {@code a > b}.
	 * 	<li>If {@code compareFn(a, b) = 0}, then {@code a = b}.
	 * 	<li>If {@code compareFn(a, b) > 0}, then {@code a < b}.
	 * </ul> <br> <br>
	 *
	 * This method mutates the original {@code Array}.
	 *
	 * @param compareFn The function used to compare {@code Array} elements.
	 * @implNote Time complexity: {@code O(n log n)}
	 */
	public Array<T> sortElements(BiFunction<T, T, Integer> compareFn) {
		array.sort(compareFn::apply);

		return this;
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, returns a sequential {@code Stream} with this {@code Array} as its source. <br> <br> {@inheritDoc}
	 */
	public @NotNull Stream<T> stream() {
		return this.array.stream();
	}

	/**
	 * A {@link CopyOnWriteArrayList} method, returns a view of the portion of this list between {@code fromIndex}, inclusive,
	 * and {@code toIndex}, exclusive. The returned list is backed by this list, so changes in the returned list are reflected
	 * in this list. <br> <br>
	 *
	 * The semantics of the list returned by this method become undefined if the backing list (i.e., this list) is modified in
	 * any way other than via the returned list.
	 * {@inheritDoc}
	 */
	@Override
	public @NotNull List<T> subList(int fromIndex, int toIndex) {
		return this.array.subList(fromIndex, toIndex);
	}

	/**
	 * Reverses the order of elements of this {@code Array}. Unlike {@link Array#reverse}, this method returns a new {@code Array}
	 * with the reversed elements.
	 * @see Array#reverse
	 * @implNote Time complexity: {@code O(n)}
	 */
	public Array<T> toReversed() {
		return new Array<>(this).reverse();
	}

	/**
	 * Sorts this {@code Array} according to the order induced by the specified {@code Comparator}. The sort is
	 * <i>stable</i>: this method must not reorder equal elements. Unlike {@link Array#sort}, this method returns a new {@code Array}
	 * with the sorted elements.
	 * @param compareFn The function used to compare {@code Array} elements.
	 * @implNote Time complexity: {@code O(n log n)}
	 */
	public Array<T> toSorted(BiFunction<T, T, Integer> compareFn) {
		return new Array<>(this)
			.sortElements(compareFn);
	}

	/**
	 * Normalizes index inputs, resolving negative and positive array indexes.
	 * @param index The index to normalize.
	 * @return The normalized index.
	 */
	private int normalizeIndex(int index) {
		return index < 0
			? index + array.size()
			: index;
	}

	@Override
	public String toString() {
		return array.toString();
	}
}

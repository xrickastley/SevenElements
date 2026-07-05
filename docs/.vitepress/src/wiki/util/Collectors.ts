
import { BiConsumer, Function, Supplier } from "./FunctionTypes";
import Collector from "./Collector";
import ExtendedCollection from "./ExtendedCollection";

namespace Collectors {
	class CollectorImpl<T, A, R> implements Collector<T, A, R> {
		public constructor(supplier: Supplier<A>, accumulator: BiConsumer<T, A>)
		public constructor(supplier: Supplier<A>, accumulator: BiConsumer<T, A>, finisher: Function<A, R>)
		public constructor(supplier: Supplier<A>, accumulator: BiConsumer<T, A>, finisher: Function<A, R> = a => a as unknown as R) {
			this.#supplier = supplier;
			this.#accumulator = accumulator;
			this.#finisher = finisher;
		}

		readonly #supplier: Supplier<A>;
		readonly #accumulator: BiConsumer<T, A>;
		readonly #finisher: Function<A, R>;

		public supplier(): Supplier<A> {
			return this.#supplier;
		}

		public accumulator(): BiConsumer<T, A> {
			return this.#accumulator;
		}

		public finisher(): Function<A, R> {
			return this.#finisher;
		}
	}

	export function keyed<T, R>(keyFn: Function<T, R>): Collector<T, ExtendedCollection<R, T[]>, ExtendedCollection<R, T[]>> {
		return new CollectorImpl(
			() => new ExtendedCollection(),
			(value, collection) => collection
				.computeIfAbsent(keyFn(value), () => [] as T[])
				.push(value)
		);
	}
}

export default Collectors;
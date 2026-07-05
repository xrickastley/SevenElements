
import { BiConsumer, Function, Supplier } from "./FunctionTypes";

interface Collector<T, A, R> {
	supplier(): Supplier<A>;
	accumulator(): BiConsumer<T, A>;
	finisher(): Function<A, R>;
}

export default Collector;
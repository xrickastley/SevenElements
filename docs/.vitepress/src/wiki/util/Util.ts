
import { Result } from "@sapphire/shapeshift";

import { Supplier } from "./FunctionTypes";
import Formatting from "./Formatting";
import Pair from "./Pair";

namespace Util {
	export function normalizeArray<T>(array: RestOrArray<T>): T[] {
		return Array.isArray(array[0]) && array.length === 1
			? [...array[0]]
			: array as T[];
	}

	export function asDataResult<T>(supplier: Supplier<T>): Result<T, Error> {
		try {
			return Result.ok(supplier());
		} catch (error) {
			if (!(error instanceof Error))
				throw new TypeError("Util.asDataResult expects the supplier to throw an Error object!");

			return Result.err(error);
		}
	}

	export function isNullOrUndefined(obj: unknown): obj is null | undefined {
		return obj === null || obj === undefined;
	}

	export function tryOrFallback<T>(supplier: Supplier<T>, fallback: T): T {
		try {
			return supplier();
		} catch {
			return fallback;
		}
	}

	export function tryOrCatch<T>(supplier: Supplier<T>): Pair<T, null> | Pair<null, unknown> {
		try {
			return new Pair(supplier(), null);
		} catch (error: unknown) {
			return new Pair(null, error);
		}
	}

	export function unpackNestedErrors(error: Error): string {
		let result = `${error.name}: ${error.message}`;

		if (error.stack)
			result = error.stack;

		if (error.cause)
			result += `\nCaused by: ${Util.unpackNestedErrors(error).split("\n").join("\n\t")}`;

		if ("errors" in error && Array.isArray(error.errors)) {
			const add = error.errors
				.flat(1)
				.filter(error => error instanceof Error)
				.map(error => Util.unpackNestedErrors(error).split("\n").join("\n\t"))
				.join("\n");

			result += "\n" + add;
		}

		return result;
	}

	export function resolveFormattingCode(code: string): string {
		const formattingCode = code.startsWith("§") ? code.slice(1) : code;

		switch (formattingCode) {
			case `0`: return Formatting.BLACK;
			case `1`: return Formatting.DARK_BLUE;
			case `2`: return Formatting.DARK_GREEN;
			case `3`: return Formatting.DARK_AQUA;
			case `4`: return Formatting.DARK_RED;
			case `5`: return Formatting.DARK_PURPLE;
			case `6`: return Formatting.GOLD;
			case `7`: return Formatting.GRAY;
			case `8`: return Formatting.DARK_GRAY;
			case `9`: return Formatting.BLUE;
			case `a`: return Formatting.GREEN;
			case `b`: return Formatting.AQUA;
			case `c`: return Formatting.RED;
			case `d`: return Formatting.LIGHT_PURPLE;
			case `e`: return Formatting.YELLOW;
			case `f`: return Formatting.WHITE;

			case `k`:
			case `l`:
			case `m`:
			case `n`:
			case `o`:
			case `r`:
				throw new Error(`The formatting code: §${formattingCode} is unsupported here!`);
			default:
				throw new Error(`Invalid formatting code: §${formattingCode}`);
		}
	}

	export function formatText(text: string): string {
		const spanStack: string[] = [];
		const result = text.replace(/§[a-f0-9]/g, code => {
			spanStack.unshift("</span>");

			return `<span class="${Util.resolveFormattingCode(code)}">`;
		});

		return result + spanStack.join("");
	}
}

export type RestOrArray<T> = T[] | [T[]];

export default Util;
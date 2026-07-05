
import { BaseValidator, Result, s } from "@sapphire/shapeshift";

import Util from "./Util";

class Identifier {
	private constructor(namespace: string, path: string) {
		this.namespace = namespace;
		this.path = path;

		this.validate();

		if (Identifier.#IS_MEMOIZED) Identifier.#MEMO.set(this.toString(), this);
	}

	static readonly #MEMO: Map<string, Identifier> = new Map();
	static #IS_MEMOIZED: boolean = true;

	private static readonly DEFAULT_NAMESPACE: string = "minecraft";

	private readonly namespace: string;
	private readonly path: string;

	private static validate(idString: string): Identifier {
		if (!idString.includes(":"))
			throw new Error("Expected identifier string to be of the format \"namespace:path\"!");

		return Identifier.ofDynamic(idString);
	}

	private static create(namespace: string, path: string): Identifier {
		return Identifier.#IS_MEMOIZED && Identifier.#MEMO.has(this.toString())
			? Identifier.#MEMO.get(this.toString()) as Identifier
			: new Identifier(namespace, path);
	}

	public static getCodec(): BaseValidator<Identifier> {
		return s.union([
			s.string().reshape(string => Util.asDataResult(() => Identifier.validate(string))),
			s.object({
				namespace: s.string().default(Identifier.DEFAULT_NAMESPACE),
				path: s.string().reshape(string => {
					return Identifier.getPathRegExp().test(string)
						? Result.ok(string)
						: Result.err(new Error(`Non [a-z0-9/._-] character in path of location: ${this}`));
				})
			}).required().transform(Identifier.resolve)
		]);
	}

	public static shouldMemoizeIds(memoize: boolean): void {
		Identifier.#IS_MEMOIZED = memoize;
	}

	public static equals(a?: IdentifierResolvable, b?: IdentifierResolvable): boolean;
	public static equals(a?: any, b?: any): boolean;
	public static equals(a?: any, b?: any): boolean {
		if (!a || !b) return false;

		const idA: Identifier | null = Util.tryOrFallback(() => Identifier.resolve(a), null);
		const idB: Identifier | null = Util.tryOrFallback(() => Identifier.resolve(b), null);

		if (!idA || !idB) return false;

		return idA.equals(idB);
	}

	public static of(namespace: string, path: string): Identifier;
	public static of(path: string): Identifier;
	public static of(namespaceOrPath: string, path?: string): Identifier {
		return !path
			? Identifier.create(Identifier.DEFAULT_NAMESPACE, namespaceOrPath)
			: Identifier.create(namespaceOrPath, path);
	}

	public static ofDynamic(idString: string): Identifier;
	public static ofDynamic(idString: string, defaultNamespace?: string): Identifier;
	public static ofDynamic(idString: string, defaultNamespace: string = Identifier.DEFAULT_NAMESPACE): Identifier {
		const splitId = idString.split(":");

		return splitId.length === 1
			? Identifier.create(defaultNamespace, splitId[0])
			: Identifier.create(splitId[0], splitId[1]);
	}

	public static ofVanilla(path: string): Identifier {
		return Identifier.create(Identifier.DEFAULT_NAMESPACE, path);
	}

	public static resolve(resolvable: IdentifierResolvable): Identifier;
	public static resolve(resolvable: IdentifierResolvable, defaultNamespace?: string): Identifier;
	public static resolve(resolvable: IdentifierResolvable, defaultNamespace: string = Identifier.DEFAULT_NAMESPACE): Identifier {
		return resolvable instanceof Identifier
			? resolvable
			: typeof resolvable === "object"
				? Identifier.create(resolvable.namespace, resolvable.path)
				: Identifier.ofDynamic(resolvable, defaultNamespace);
	}

	public static isIdentifierPattern(id: string): boolean {
		const splitId = id.split(":");

		return Identifier.getNamespaceRegExp().test(splitId[0])
			&& Identifier.getPathRegExp().test(splitId[1]);
	}

	public static getNamespaceRegExp(): RegExp {
		return /^[a-z0-9_.-]+$/gm;
	}

	public static getPathRegExp(): RegExp {
		return /^[a-z0-9/._-]+$/gm;
	}

	public getNamespace(): string {
		return this.namespace;
	}

	public getPath(): string {
		return this.path;
	}

	public equals(other: Identifier): boolean
	public equals(other: string): boolean
	public equals(other: Identifier | string): boolean
	public equals(resolvableOther: Identifier | string): boolean {
		const other = resolvableOther instanceof Identifier
			? resolvableOther
			: Identifier.ofDynamic(resolvableOther);

		return other.namespace === this.namespace && other.path === this.path;
	}

	public toString() {
		return `${this.namespace}:${this.path}`;
	}

	private validate() {
		this.validateNamespace();
		this.validatePath();
	}

	private validateNamespace() {
		if (!this.namespace)
			throw new Error(`No character in namespace of location: ${this}`);

		if (!Identifier.getNamespaceRegExp().test(this.namespace))
			throw new Error(`Non [a-z0-9_.-] character in namespace of location: ${this}`);
	}

	private validatePath() {
		if (!this.path)
			throw new Error(`No character in namespace of location: ${this}`);

		if (!Identifier.getPathRegExp().test(this.path))
			throw new Error(`Non [a-z0-9/._-] character in path of location: ${this}`);
	}
}

interface IdentifierObject {
	namespace: string;
	path: string;
}

type IdentifierResolvable = string | IdentifierObject | Identifier;

export default Identifier;
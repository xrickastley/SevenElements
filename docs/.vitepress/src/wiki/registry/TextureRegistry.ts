
import ExtendedCollection from "../util/ExtendedCollection";
import Identifier from "../util/Identifier";

class TextureRegistry {
	static #INSTANCE: TextureRegistry;

	private readonly assets: ExtendedCollection<string, ExtendedCollection<string, string>>;

	private constructor() {
		if (TextureRegistry.#INSTANCE)
			throw new TypeError(`An instance of the TextureRegistry already exists! Use TextureRegistry.getInstance() to get the current instance instead!`);

		TextureRegistry.#INSTANCE = this;

		this.assets = new ExtendedCollection();

		const textureEntries = Object.entries(import.meta.glob("@assets/**/*.{png,gif}", { eager: true, import: "default" })) as [string, string][];

		for (const textureEntry of textureEntries) {
			const [origPath, newPath] = textureEntry;
			const parts = origPath.slice(origPath.indexOf("assets/") + 7).replace(/\\/g, "/").split("/");

			if (parts.length < 2) continue;

			const namespace = parts.shift() as string;
			const path = parts.join("/");

			this.assets
				.computeIfAbsent(namespace, () => new ExtendedCollection<string, string>())
				.set(path, newPath);
		}
	}

	public static getInstance(): TextureRegistry {
		return TextureRegistry.#INSTANCE
			? TextureRegistry.#INSTANCE
			: new TextureRegistry();
	}

	public static getTexture(id: Identifier): string {
		const value = TextureRegistry.getInstance().getTexture(id);

		if (!value)
			throw new Error(`No texture found: ${value}!`);

		return value;
	}

	public getTexture(id: Identifier): string | null {
		return this.assets
			.get(id.getNamespace())
			?.get(id.getPath()) ?? null;
	}
}

export default TextureRegistry;
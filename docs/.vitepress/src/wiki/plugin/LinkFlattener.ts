
import fg from "fast-glob";
import path from "path";

import ExtendedCollection from "../util/ExtendedCollection";
import Stream from "../util/Stream";

class LinkFlattener {
	public constructor(basePath: string)
	public constructor(basePath: string, include: string[])
	public constructor(basePath: string, include: string[], preserve: string[])
	public constructor(basePath: string, include: string[] = ["**/*"], preserve: string[] = []) {
		this.path = basePath;
		this.include = include;
		this.preserve = preserve;
		this.mappings = new Map();

		const docFiles = fg.sync(this.include, { cwd: this.path, onlyFiles: true })
			.filter(file => file.endsWith(".md"));

		const reverseMappings = new ExtendedCollection<string, string[]>();

		for (const filePath of docFiles)
			this.createMapping(filePath, reverseMappings);

		for (const reverseMapping of reverseMappings.filter(mapped => mapped.length > 1)) {
			console.warn(`[WARNING] Unable to flatten ${reverseMapping[1].map(path => `"${path}"`).join(", ")}, all links resolve to single path: "${reverseMapping[0]}"!`);

			reverseMapping[1].forEach(mapping => this.mappings.set(mapping, mapping));
		}
	}

	private readonly path: string;
	private readonly include: string[];
	private readonly preserve: string[];
	private readonly mappings: Map<string, string>;

	private createMapping(filePath: string, reverseMappings: ExtendedCollection<string, string[]>) {
		const longestStrip = Stream.of(this.preserve)
			.filter(strip => filePath.startsWith(strip))
			.max(strip => strip.length) ?? ``;

		const mappedPath = path.normalize(longestStrip ? path.relative(path.dirname(longestStrip), filePath) : path.basename(filePath)).replaceAll("\\", "/");

		this.setMapping(filePath, mappedPath, reverseMappings);

		if (path.basename(filePath) === "index.md")
			this.setMapping(path.dirname(filePath) === "." ? "" : path.dirname(filePath), mappedPath, reverseMappings);
	}

	private setMapping(filePath: string, mappedPath: string, reverseMappings: ExtendedCollection<string, string[]>) {
		this.mappings.set(filePath, mappedPath);
		reverseMappings.computeIfAbsent<string[]>(mappedPath, () => [])
			.push(filePath);
	}

	public getBasePath(): string {
		return this.path;
	}

	public getLinkMapping(original: string, includeExtension: boolean = false): string {
		original = original.replaceAll("\\", "/");

		if (original.startsWith("/"))
			original = original.slice(original.indexOf("/") + 1);

		const resolves: string[] = [original];

		if (!original.endsWith(".md")) {
			resolves.push(original + ".md");
			resolves.push(original + "/index.md");
		}

		let mapping = resolves.map(possible => this.mappings.get(possible)).find(value => value);

		if (!mapping)
			throw new Error(`No mapping for unknown path: "${original}"!`);

		if (!includeExtension && mapping.includes(".", Math.max(0, mapping.lastIndexOf("/"))))
			mapping = mapping.substring(0, this.lastIndexOf(mapping, ".", Math.max(0, mapping.lastIndexOf("/"))));

		if (includeExtension && !mapping.endsWith(".md"))
			mapping += ".md";

		return mapping;
	}

	private lastIndexOf(string: string, searchString: string, position: number) {
		const excluded = string.slice(0, position);
		const toSearch = string.slice(position);

		return toSearch.includes(searchString)
			? excluded.length + toSearch.lastIndexOf(searchString)
			: -1;
	}
}

export default LinkFlattener;
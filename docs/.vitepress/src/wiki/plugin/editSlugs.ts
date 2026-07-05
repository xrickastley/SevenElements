
import { Plugin } from "vitepress";
import fs from "fs";
import path from "path";

import { Function } from "../util/FunctionTypes";
import ExtendedCollection from "../util/ExtendedCollection";

export function slugifyMarkdown(slugify: Function<string, string>): Plugin {
	const slugCache: ExtendedCollection<string, ExtendedCollection<string, string>> = new ExtendedCollection();

	function computeSlugs(pagePath: string) {
		return slugCache.computeIfAbsent(pagePath, () => {
			const slugTransforms = new ExtendedCollection<string, string>();
			const pageContents = fs.readFileSync(pagePath).toString();

			pageContents
				.split(/\r?\n/)
				.filter(line => /\s?^#{1,6}\s?(.*)\s?$/gm.test(line.trim()))
				.map(line => /\s?^#{1,6}\s?(.*)\s?$/gm.exec(line.trim())?.[1])
				.filter(title => typeof title === "string")
				.forEach(title => slugTransforms.set(title.toLowerCase().replace(/\s+/gm, "-"), slugify(title)));

			return slugTransforms;
		});
	}

	return {
		name: "slugify-markdown",

		transform(code, id) {
			const filePath = id.slice(0, id.includes("?") ? id.lastIndexOf("?") : undefined);

			if (!filePath.endsWith(".md"))
				return;

			return code.replaceAll(/href=\\"(.*?)\\"/gm, (match, href: string) => {
				if (!href.startsWith(".") || !href.includes("#"))
					return match;

				const pageLink = href.slice(0, href.lastIndexOf("#"));
				const anchor = href.substring(href.lastIndexOf("#") + 1);

				let pagePath = path.resolve(path.dirname(filePath), pageLink);

				if (!pagePath.endsWith(".md"))
					pagePath += ".md";

				const newSlug = computeSlugs(pagePath).get(anchor) ?? anchor;

				return match.replace(anchor, newSlug);
			});
		}
	};
}
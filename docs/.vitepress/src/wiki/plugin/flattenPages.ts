
import { Plugin } from "vitepress";
import path from "path";

import LinkFlattener from "./LinkFlattener";

export function flattenPages(flattener: LinkFlattener): Plugin {
	return {
		name: "flatten-pages",

		transform(code, id) {
			const filePath = id.slice(0, id.includes("?") ? id.lastIndexOf("?") : undefined);

			if (!filePath.endsWith(".md"))
				return;

			const remappedRelativeFrom = flattener.getLinkMapping(path.relative(flattener.getBasePath(), filePath), true);

			return code.replaceAll(/href=\\"(.*?)\\"/gm, (match, href: string) => {
				if (!href.startsWith("."))
					return match;

				const link = href.substring(0, href.includes("#") ? href.lastIndexOf("#") : undefined);

				const absoluteTo = path.resolve(path.dirname(filePath), link);
				const relativeTo = path.normalize(path.relative(flattener.getBasePath(), absoluteTo)).replaceAll("\\", "/");
				const remappedRelativeTo = flattener.getLinkMapping(relativeTo, false);

				let remappedLink = link
					? path.normalize(path.relative(path.dirname(remappedRelativeFrom), remappedRelativeTo)).replaceAll("\\", "/")
					: link;

				if (!remappedLink.startsWith("."))
					remappedLink = "./" + remappedLink;

				if (remappedLink.endsWith(".md"))
					remappedLink = remappedLink.slice(0, -3);

				return match.replace(href, href.replace(link, remappedLink));
			});
		}
	};
}
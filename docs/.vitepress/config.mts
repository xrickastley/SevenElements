
import { fileURLToPath } from "url";
import { HeadConfig, defineConfig } from "vitepress";
import path from "path";

import { flattenPages } from "./src/wiki/plugin/flattenPages";
import { slugifyMarkdown } from "./src/wiki/plugin/editSlugs";
import LinkFlattener from "./src/wiki/plugin/LinkFlattener";

const filename = fileURLToPath(import.meta.url);
const dirname = path.dirname(filename);

const linkFlattener = new LinkFlattener(path.resolve(filename, "../../"), ["**/*"], ["developer", "guide/misc/Commands"]);
const slugify = (str: string) => str.replace(/\s+/gm, "_");

// https://vitepress.dev/reference/site-config
export default defineConfig({
	base: "/SevenElements/wiki",
	title: "Seven Elements Docs",
	description: "Documentation for the Seven Elements Minecraft mod.",
	head: [["link", { rel: "icon", type: "image/png", href: "/SevenElements/wiki/icon.png" }]],
	lastUpdated: true,
	themeConfig: {
		// https://vitepress.dev/reference/default-theme-config
		nav: [
			{ text: "Home", link: "/" }
		],

		search: {
			provider: "local"
		},

		logo: "/SevenElements/wiki/icon.png",

		sidebar: {
			"/": [
				{ text: "Getting Started", link: linkFlattener.getLinkMapping("/guide/index.md") },
				{ text: "Installation", link: linkFlattener.getLinkMapping("/guide/installation.md") },
				{
					text: "Workstations",
					items: [
						{ text: "Infusion Table", link: linkFlattener.getLinkMapping("/guide/blocks/infusion_table") }
					]
				},
				{
					text: "Elements",
					items: [
						{ text: "The Seven Elements", link: linkFlattener.getLinkMapping("/guide/elements/the_seven_elements") },
						{ text: "Elemental Combat", link: linkFlattener.getLinkMapping("/guide/elements/elemental_combat") },
						{ text: "Elemental Reactions", link: linkFlattener.getLinkMapping("/guide/elements/elemental_reactions") },
						{ text: "Elemental Gauge Theory", link: linkFlattener.getLinkMapping("/guide/elements/elemental_gauge_theory") },
						{ text: "Internal Cooldown", link: linkFlattener.getLinkMapping("/guide/elements/internal_cooldown") }
					]
				},
				{
					text: "Miscellaneous",
					items: [
						{ text: "Commands", link: linkFlattener.getLinkMapping("/guide/misc/Commands") },
						{ text: "Configuration", link: linkFlattener.getLinkMapping("/guide/misc/configuration") },
						{ text: "Game rule", link: linkFlattener.getLinkMapping("/guide/misc/game_rule") }
					]
				}
			],
			"/developer/": [
				{ text: "Getting Started", link: linkFlattener.getLinkMapping("/developer/index.md") },
				{ text: "Dependency", link: linkFlattener.getLinkMapping("/developer/dependency.md") },
				{
					text: "Data pack",
					items: [
						{ text: "ICD Type definition", link: linkFlattener.getLinkMapping("/developer/data_pack/internal_cooldown_type_definition") },
						{ text: "Damage type tag", link: linkFlattener.getLinkMapping("/developer/data_pack/damage_type_tag") },
						{ text: "Entity type tag", link: linkFlattener.getLinkMapping("/developer/data_pack/entity_type_tag") },
						{ text: "Item tag", link: linkFlattener.getLinkMapping("/developer/data_pack/item_tag") }
					]
				},
				{
					text: "Mod",
					items: [
						{ text: "Disabling Entity Elements", link: linkFlattener.getLinkMapping("/developer/mod/disabling_entity_elements") },
						{ text: "Adding an Elemental Reaction", link: linkFlattener.getLinkMapping("/developer/mod/adding_an_elemental_reaction") },
						{ text: "Events", link: linkFlattener.getLinkMapping("/developer/mod/events") }
					]
				},
				{
					text: "Compatibility",
					items: [
						{ text: "Fixing Elemental Infusions", link: linkFlattener.getLinkMapping("/developer/compatibility/fixing_elemental_infusions") },
						{ text: "Fixing Boss Bar Displays", link: linkFlattener.getLinkMapping("/developer/compatibility/fixing_boss_bar_displays") }
					]
				}
			]
		},

		socialLinks: [
			{ icon: "github", link: "https://github.com/xrickastley/SevenElements/tree/wiki" }
		],

		outline: {
			level: [2, 3]
		},

		footer: {
			message: "THIS PROJECT IS NOT AFFILIATED WITH NEITHER HOYOVERSE NOR GENSHIN IMPACT.",
			copyright: "© All rights reserved by HoYoverse. Other properties belong to their respective owners. | Docs released under the <a href=\"https://github.com/xrickastley/SevenElements/tree/wiki/LICENSE\">CC BY-NC-SA License</a>"
		},

		notFound: {
			quote: "How about we explore the area ahead of us later?",
			linkText: "Teleport back"
		},

		docFooter: {
			next: false,
			prev: false
		}
	},
	rewrites(id) {
		return linkFlattener.getLinkMapping(id, true);
	},
	markdown: {
		math: true,
		theme: {
			dark: "dark-plus",
			light: "light-plus"
		},
		image: {
			lazyLoading: true
		},
		languages: [
			async () =>
				await import("syntax-mcfunction/mcfunction.tmLanguage.json", {
					with: { type: "json" }
				}).then(lang => ({ ...(lang.default as any), name: "mcfunction" }))
		],
		anchor: {
			slugify
		}
	},
	vite: {
		resolve: {
			alias: {
				"@assets": path.resolve(dirname, "./src/assets"),
				"@data": path.resolve(dirname, "./src/data")
			}
		},
		plugins: [
			slugifyMarkdown(slugify),
			flattenPages(linkFlattener)
		]
	},
	transformHead({ assets }) {
		const genshinFont = assets.find(f => /Genshin\.[\w]+\.ttf/.test(f));
		const config: HeadConfig[] = [];

		if (genshinFont) config.push([
			"link",
			{ rel: "preload", href: genshinFont, as: "font", type: "font/ttf", crossorigin: "" }
		]);

		return config;
	},
	cleanUrls: true
});

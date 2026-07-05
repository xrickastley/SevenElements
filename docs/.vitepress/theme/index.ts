// https://vitepress.dev/guide/custom-theme

import { h } from "vue";
import type { Theme } from "vitepress";
import DefaultTheme from "vitepress/theme";

import "./style.css";
import "./custom.css";

import CraftingRecipe from "./components/CraftingRecipe.vue";
import Ingredient from "./components/Ingredient.vue";
import MultiCraftingRecipe from "./components/MultiCraftingRecipe.vue";
import Tooltip from "./components/Tooltip.vue";
import Treeview from "./components/Treeview.vue";
import TreeviewEntry from "./components/TreeviewEntry.vue";

export default {
	extends: DefaultTheme,
	Layout: () => {
		return h(DefaultTheme.Layout, null, {
			// https://vitepress.dev/guide/extending-default-theme#layout-slots
		});
	},
	enhanceApp({ app }) {
		app.component("CraftingRecipe", CraftingRecipe);
		app.component("Ingredient", Ingredient);
		app.component("MultiCraftingRecipe", MultiCraftingRecipe);
		app.component("Tooltip", Tooltip);
		app.component("Treeview", Treeview);
		app.component("TreeviewEntry", TreeviewEntry);
	}
} satisfies Theme;

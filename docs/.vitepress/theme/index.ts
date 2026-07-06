// https://vitepress.dev/guide/custom-theme

import { h } from "vue";
import type { Theme } from "vitepress";
import DefaultTheme from "vitepress/theme";

import "./style.css";
import "./custom.css";

import CraftingRecipe from "./components/CraftingRecipe.vue";
import Ingredient from "./components/Ingredient.vue";
import MultiRecipe from "./components/MultiRecipe.vue";
import SmithingRecipe from "./components/SmithingRecipe.vue";
import Tooltip from "./components/Tooltip.vue";
import Treeview from "./components/Treeview.vue";
import TreeviewEntry from "./components/TreeviewEntry.vue";

import FootNote from "./components/extend/foot-note.vue";
import FootRef from "./components/extend/foot-ref.vue";
import ImgMode from "./components/extend/img-mode.vue";
import ImgTexture from "./components/extend/img-texture.vue";

import ContainerEntry from "./components/chance/ContainerEntry.vue";
import ItemChanceTable from "./components/chance/ItemChanceTable.vue";
import ItemEntry from "./components/chance/ItemEntry.vue";
import StructureEntry from "./components/chance/StructureEntry.vue";

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
		app.component("MultiRecipe", MultiRecipe);
		app.component("SmithingRecipe", SmithingRecipe);
		app.component("Tooltip", Tooltip);
		app.component("Treeview", Treeview);
		app.component("TreeviewEntry", TreeviewEntry);

		app.component(`foot-note`, FootNote);
		app.component(`foot-ref`, FootRef);
		app.component(`img-mode`, ImgMode);
		app.component(`img-texture`, ImgTexture);

		app.component(`ContainerEntry`, ContainerEntry);
		app.component(`ItemChanceTable`, ItemChanceTable);
		app.component(`ItemEntry`, ItemEntry);
		app.component(`StructureEntry`, StructureEntry);
	}
} satisfies Theme;

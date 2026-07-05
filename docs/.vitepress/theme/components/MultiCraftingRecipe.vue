<script lang="ts">
import { cloneVNode, defineComponent, h, onMounted, onUnmounted, ref } from 'vue';

import CraftingRecipe from './CraftingRecipe.vue';

import Constants from '../../constants.ts';

export default defineComponent({
	setup(_, { slots }) {
		const children = slots.default?.() ?? [];

		if (children.some(child => child.type !== CraftingRecipe))
			throw new Error("The children of <MultiCraftingRecipe> must all be of <CraftingRecipe>!");

		if (children.length < 2)
			throw new Error("The children of <MultiCraftingRecipe> must be at least 2 <CraftingRecipe>!");

		let recipeSwitcher: ReturnType<typeof setInterval>;

		function onMouseEnter() {
			if (recipeSwitcher)
				clearInterval(recipeSwitcher);
		}

		function onMouseLeave() {
			recipeSwitcher = createRecipeSwitcher();
		}

		function createRecipeSwitcher(): ReturnType<typeof setInterval> {
			return setInterval(() => recipeIndex.value = (recipeIndex.value + 1) % children.length, Constants.SWITCH_DELAY);
		}

		onMounted(() => recipeSwitcher = createRecipeSwitcher());
		onUnmounted(() => recipeSwitcher ? clearInterval(recipeSwitcher) : null);

		const recipeIndex = ref(0);

		return () => {
			const children = slots.default?.() ?? [];
			
			return h(
				'div',
				{
					onMouseenter: onMouseEnter,
					onMouseleave: onMouseLeave,
					style: {
						display: 'inline-block',
						width: 'fit-content'
					}
				},
				cloneVNode(children[recipeIndex.value], {
					key: recipeIndex.value
				})
			);
		};
	},
});
</script>
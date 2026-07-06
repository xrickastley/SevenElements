<script setup lang="ts">
import { computed, useSlots } from 'vue';

import Ingredient from './Ingredient.vue';

import ExtendedCollection from '../../src/wiki/util/ExtendedCollection';

const slots = useSlots();
const children = computed(() => slots.default?.() ?? []);

if (children.value.some(child => child.type !== Ingredient))
	throw new Error("The children of <SmitihingRecipe> must all be of <Ingredient>!");

if (children.value.some(child => isNaN(Number(child.props?.slotId)) || Number(child.props?.slotId) < 0 || 3 < Number(child.props?.slotId)))
	throw new Error("The children of <SmitihingRecipe> must define the property slotId within the range 0 - 3!");

const seen: ExtendedCollection<number, string[]> = new ExtendedCollection();

for (const ingredient of children.value) {
	const slotId = Number(ingredient.props?.slotId);

	seen.computeIfAbsent<string[]>(slotId, () => [])
		.push(ingredient.props?.id || ingredient.props?.tag);
}

const dupes = seen.filter(slotEntry => slotEntry.length > 1);

if (dupes.size > 0)
	throw new Error(`The children of <SmitihingRecipe> must all have unique slots! ${dupes.map((entries, slotId) => `Slot ${slotId}: ${entries.join(", ")}`)}`)

if (!seen.has(0))
	throw new Error("The children of <SmitihingRecipe> must have at least one <Ingredient> with slotId=0 for the result!");

if (!seen.findKey((_entries, slotId) => 0 < slotId && slotId <= 3))
	throw new Error("The children of <SmitihingRecipe> must have at least one <Ingredient> with slotId=0..9 for the input!");

const input = computed(() => 
	children.value
		.map(child => ({ vnode: child, slotId: Number(child.props?.slotId) }))
		.filter(input => input.slotId > 0)
);

const result = computed(() =>
	children.value.find(child => Number(child.props?.slotId) === 0)
);
</script>

<template>
	<div class="menu">
		<div class="input">
			<div
				v-for="ingredient in input"
				:style="{
					gridColumn: ingredient.slotId
				}">
				<component :is="ingredient.vnode"/>
			</div>
		</div>
		<div class="result">
			<component :is="result"/>
		</div>
	</div>
</template>

<style lang="css" scoped>
.menu {
	display: inline-block;
	position: relative;
	background-image: url("./smithing_menu.png");
	width: 296px;
	height: 68px;
	image-rendering: pixelated;
}

.input {
	display: grid;
	grid-template-columns: repeat(3, 32px);
	grid-auto-rows: 32px;
	gap: 4px;
	position: absolute;
	left: 32px;
	top: 17px;
	padding: 0;
	margin: 0;
	width: fit-content;
}

.result {
	position: absolute;
	left: 217px;
	top: 18px;
	padding: 0;
	margin: 0;
	width: fit-content;
}
</style>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import Constants from '../../constants.ts';

import Registries from '../../src/wiki/registry/Registries';
import RegistryKeys from '../../src/wiki/registry/RegistryKeys';
import TagKey from '../../src/wiki/registry/TagKey';

import Identifier from '../../src/wiki/util/Identifier';

import Tooltip from './Tooltip.vue';



interface BaseIngredient {
	count?: string;
	slotId?: string;
}

interface ItemIngredient extends BaseIngredient {
	id: string;
	tag?: never;
}

interface TagIngredient extends BaseIngredient {
	tag: string;
	id?: never;
}

type Ingredient = ItemIngredient | TagIngredient;

const props = defineProps<Ingredient>();

if (!!props?.tag === !!props?.id)
	throw new Error(`Exactly one of "id" or "tag" must be specified!`);

const items = props.id
	? [Registries.ITEM.getOrThrow(Identifier.getCodec().parse(props.id))]
	: Registries.ITEM.getTagEntries(TagKey.unprefixedCodec(RegistryKeys.ITEM).parse(props.tag));

const count = props?.count;

onMounted(() => {
	// avoids the "jittering" when switching
	items.forEach(item => new Image().src = item.getInventoryIcon());
})

const hover = ref(false);
const x = ref(0);
const y = ref(0);

const itemIndex = ref(0);
const item = computed(() => items[itemIndex.value]);

let itemSwitcher = createItemSwitcher();

function onMouseEnter() {
	hover.value = true;

	if (itemSwitcher)
		clearInterval(itemSwitcher);
}

function onMouseLeave() {
	hover.value = false;

	itemSwitcher = createItemSwitcher();
}

function onMouseMove(e: MouseEvent) {
	x.value = e.clientX;
	y.value = e.clientY;
}

function createItemSwitcher(): ReturnType<typeof setInterval> {
	return setInterval(() => itemIndex.value = (itemIndex.value + 1) % items.length, Constants.SWITCH_DELAY);
}
</script>

<template>
	<a class="false-link" :href="`${item.getLink()}`">
		<div class="slot">
			<div
				@mouseenter="onMouseEnter"
				@mouseleave="onMouseLeave"
				@mousemove="onMouseMove"
				class="slot-item"
				href=""
				:style="{
					'--ingredient': `url(${item.getInventoryIcon()})`
				}">
				<div class="count"
					v-if="count"
					>{{ count }}</div>
			</div>
		</div>
	</a>
	<Tooltip class="tooltip"
        v-if="hover"
		v-html="item.getTooltip()"
		:style="{
			left: `${x}px`,
			top: `${y}px`
		}">
	</Tooltip>
</template>

<style lang="css" scoped>
.false-link,
.false-link:hover,
.false-link:active,
.false-link:visited {
	color: white;
	text-decoration: none !important;
}

.slot {
	width: 32px;
	height: 32px;
}

.slot:hover {	
    background: rgba(255, 255, 255, 0.3);
}

.slot:active .slot-item {
    transform: translate(25%, -25%);
}

.slot-item {
	position: relative;
	width: 32px;
	height: 32px;
	
	background-image: var(--ingredient);
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;

	image-rendering: pixelated;
}

.slot-item .count {
    position: absolute;
    right: -2px;
    bottom: -4px;
	font: 16px var(--minecraft-font);
	text-shadow: 2px 2px 0 #3f3f3f;
}

.tooltip {
	position: fixed;
	transform: translate(8px, -30px);
	z-index: 999;
}
</style>
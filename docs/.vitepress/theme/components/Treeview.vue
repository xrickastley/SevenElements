<script setup lang="ts">
import { computed, useSlots } from 'vue';

import TreeviewEntry from './TreeviewEntry.vue';

const slots = useSlots();
const children = computed(() => slots.default?.() ?? []);

if (children.value.some(child => child.type !== TreeviewEntry))
	throw new Error("The children of <Treeview> must be a <TreeviewEntry>!");

if (children.value.length !== 1)
	throw new Error("The children of <Treeview> must be a single <TreeviewEntry>!");
</script>

<template>
	<div class="treeview">
		<ul>
			<li>
				<slot></slot>
			</li>
		</ul>
	</div>
</template>

<style lang="css" scoped>
.treeview {
	margin-top: 0.3em;
    display: flow-root;
	font-size: 16px;
}

.treeview :deep(ul), 
.treeview :deep(li) {
    margin: 0 !important;
    padding: 0;
    list-style-type: none;
    list-style-image: none;
}

.treeview :deep(li li) {
    position: relative;
    padding-left: 13px;
    margin-left: 7px !important;
    border-left: 1px solid #636363;
}

.treeview :deep(li li::before) {
    content: "";
    position: absolute;
    top: 0;
    left: -1px;
    width: 11px;
    height: 11px;
    border-bottom: 1px solid #636363;
}

.treeview :deep(li li:last-child:not(.treeview-continue)::before) {
    border-left: 1px solid #636363;
    width: 10px;
}

.treeview :deep(li li:last-child:not(.treeview-continue)) {
    border-color: transparent;
}
</style>
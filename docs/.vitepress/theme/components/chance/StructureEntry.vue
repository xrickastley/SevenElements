<script setup lang="ts">
import { computed, useSlots } from 'vue';

import ContainerEntry from './ContainerEntry.vue';

const slots = useSlots();
const children = computed(() => slots.default?.() ?? []);

if (children.value.some(child => child.type !== ContainerEntry))
	throw new Error("The children of <StructureEntry> must all be of <ContainerEntry>!");

// TODO: move to registry query if item chance table is needed multiple times
const props = defineProps<{
	name: string;
	icon?: string;
	link?: string;
}>();
</script>
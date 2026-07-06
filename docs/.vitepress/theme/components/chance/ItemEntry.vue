<script setup lang="ts">
import { computed, useSlots } from 'vue';

import Registries from '../../../src/wiki/registry/Registries';

import Identifier from '../../../src/wiki/util/Identifier';

import StructureEntry from './StructureEntry.vue';

const slots = useSlots();
const children = computed(() => slots.default?.() ?? []);

if (children.value.some(child => child.type !== StructureEntry))
	throw new Error("The children of <ItemEntry> must all be of <StructureEntry>!");



const props = defineProps<{
	id: string;
	bold?: 'true' | 'false';
}>();

Registries.ITEM.getOrThrow(Identifier.getCodec().parse(props.id));
</script>
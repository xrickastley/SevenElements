<script setup lang="ts">
import { useAttrs } from 'vue';

const attrs = useAttrs();
const props = defineProps<{
	id: string;
}>();

const id = props.id ? `foot-note-${props.id}` : undefined;
const href = props.id ? `#foot_ref-${props.id}` : undefined;
</script>

<template>
	<a v-bind="attrs" :id="id" :href="href" aria-label="Jump up" title="Jump up" class="jump">↑</a>
</template>

<style lang="css" scoped>
.jump {
	text-decoration: none;
}
</style>
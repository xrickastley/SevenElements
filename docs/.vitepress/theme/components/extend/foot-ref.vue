<script setup lang="ts">
import { useAttrs } from 'vue';

const attrs = useAttrs();
const props = defineProps<{
	id: string;
}>();

const id = props.id ? `foot_ref-${props.id}` : undefined;
const href = props.id ? `#foot-note-${props.id}` : undefined;
</script>

<template>
	<sup v-bind="attrs" :id="id"><a :href="href" class="jump">{{ props.id ? `[${props.id}]` : `` }}</a></sup>
</template>

<style lang="css" scoped>
.jump {
	text-decoration: none;
}
</style>
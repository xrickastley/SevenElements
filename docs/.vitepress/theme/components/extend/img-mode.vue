<script setup lang="ts">
import { useData, withBase } from 'vitepress';
import { computed, useAttrs } from 'vue';

const { isDark } = useData();
const attrs = useAttrs();
const props = defineProps<{
	darkSrc: string;
	lightSrc: string;
}>();

const src = computed(() => withBase(isDark.value || import.meta.env.SSR ? props.darkSrc : props.lightSrc));
</script>

<template>
	<img v-bind="attrs" :src="src">
</template>
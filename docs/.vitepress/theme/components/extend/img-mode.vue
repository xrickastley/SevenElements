<script setup lang="ts">
import { useData, withBase } from 'vitepress';
import { useAttrs } from 'vue';

const { isDark } = useData();
const attrs = useAttrs();
const props = defineProps<{
	darkSrc: string;
	lightSrc: string;
}>();

const darkSrc = withBase(props.darkSrc);
const lightSrc = withBase(props.lightSrc);
</script>

<template>
	<img v-bind="attrs" v-if="isDark" :src="darkSrc">
	<img v-bind="attrs" v-else :src="lightSrc">
</template>
<script setup lang="ts">
import { useAttrs } from 'vue';

import TextureRegistry from '../../../src/wiki/registry/TextureRegistry';

import Identifier from '../../../src/wiki/util/Identifier';
import Util from '../../../src/wiki/util/Util';

const attrs = useAttrs();
const props = defineProps<{
	src: string;
}>();

const id = Identifier.getCodec().parse(props.src);
const src = Util.tryOrFallback(() => TextureRegistry.getTexture(id), "");
</script>

<template>
	<img v-bind="attrs" :src="src">
</template>

<style lang="css" scoped>
img {
	image-rendering: pixelated;
}
</style>
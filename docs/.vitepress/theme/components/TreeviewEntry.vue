<script setup lang="ts">
import { useSlots } from 'vue';

import EnumHelper from '../../src/wiki/util/helpers/EnumHelper';

const slots = useSlots();



const props = defineProps<{
	type?: string;
	title?: string;
	description?: string;
	keyCount?: 'true' | 'false';
}>();

enum NbtType {
	BYTE, DOUBLE, FLOAT, INT, LONG, SHORT, STRING, COMPOUND, BYTE_ARRAY, INT_ARRAY, LIST, BOOLEAN, LONG_ARRAY
}

const type = props.type ? EnumHelper.enumValueOf(NbtType, props.type) : null;

if (!type && props.type)
	throw new Error(`Invalid NBT type: ${type}!`);

enum NbtTypeTitle {
	BYTE = "Byte", 
	BOOLEAN = "Boolean", 
	SHORT = "Short", 
	INT = "Int", 
	LONG = "Long", 
	FLOAT = "Float", 
	DOUBLE = "Double", 
	STRING = "String", 
	LIST = "NBT List / JSON Array", 
	COMPOUND = "NBT Compound / JSON Object", 
	BYTE_ARRAY = "Byte Array", 
	INT_ARRAY = "Int Array", 
	LONG_ARRAY = "Long Array"
}

const x = type ? -16 * (type % 4) : 0;
const y = type ? -16 * Math.floor(type! / 4) : 0;
const title = props.title;
const description = props.description;
const values = !slots.default || slots.default().length === 0
	? `no values`
	: slots.default().length === 1
		? `1 value`
		: `${slots.default().length} values`
</script>

<template>
	<span 
		v-if="type"
		:title="NbtTypeTitle[NbtType[type]]" 
		class="nbt-sprite sprite"
		:style="{ backgroundPosition: `${x}px ${y}px` }"
	></span>
	<b v-if="title">{{ " " + title }}</b>{{ description ? `: ${description}` : "" }}
	<i v-if="keyCount">{{ keyCount && Boolean(keyCount) ? ` (${values})` : `` }}</i>
	<ul v-if="$slots.default">
		<li
			v-for="(node, index) in slots.default!()"
			:key="index">
			<component :is="node"/>
		</li>
	</ul>
</template>

<style lang="css" scoped>
.nbt-sprite {
	background-image: url(https://minecraft.wiki/images/Nbtsheet.png);
	background-size: 64px auto;
}

.sprite {
    display: inline-block;
    vertical-align: text-top;
    height: 16px;
    width: 16px;
    background-repeat: no-repeat;
}

:deep(code a) {
	text-underline-offset: 4px;
}
</style>
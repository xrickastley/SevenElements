<script setup lang="ts">
</script>

<template>
	<div class="tooltip">
		<slot></slot>
	</div>
</template>

<style lang="css" scoped>
.tooltip {
    border: 10px solid transparent;
    border-image-source: url("./tooltip.png");
    border-image-slice: 10 fill;
    border-image-repeat: stretch;

	width: fit-content;

	font: 15px var(--minecraft-font);
	word-spacing: 15%;
	line-height: 1.35;

    padding: 2px 4px;
	
	pointer-events: none;
}

.tooltip p {
	border: 0;
	padding: 0;
}
</style>
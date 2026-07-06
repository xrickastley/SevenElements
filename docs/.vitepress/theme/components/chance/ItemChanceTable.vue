<script lang="ts">
import { computed, defineComponent, h, ref, Slot, Slots, useSlots, VNode } from 'vue';

import ItemEntry from './ItemEntry.vue';

import Registries from '../../../src/wiki/registry/Registries.ts';

import Stream from '../../../src/wiki/util/Stream.ts';
import Identifier from '../../../src/wiki/util/Identifier.ts';
import { withBase } from 'vitepress';

export default defineComponent({
	setup(_, { slots }) {
		const children = slots.default?.() ?? [];

		if (children.some(child => child.type !== ItemEntry))
			throw new Error("The children of <ItemChanceTable> must all be of <ItemEntry>!");

		const hidden = ref(false);

		return () => h(
			`table`, [
				h(`thead`, [
					h(`tr`, [
						h(`th`, `Item`),
						h(`th`, `Structure`),
						h(`th`, `Container`),
						h(`th`, `Quantity`),
						h(`th`, [
							`Chance `,
							h(`button`, { onClick: () => hidden.value = !hidden.value }, [
								`[`, h(`a`, `hide`), `]`
							])
						]),
					])
				]),
				!hidden.value
					? h(
						`tbody`,
						Stream.of(children)
							.mapMulti<ReturnType<Slot>>((itemEntry, consumer) => {
								const structureEntries = Stream.of((itemEntry.children as Slots).default!())
									.mapMulti<ReturnType<Slot>>((structureEntry, consumer) => {
										const containerEntries = Stream.of((structureEntry.children as Slots).default!())
											.map(containerEntry => [
												h(`td`, containerEntry.props!.name),
												h(`td`, containerEntry.props!.quantity),
												h(`td`, containerEntry.props!.chance)
											])
											.toArray();

										const length = containerEntries.length;
										const first = containerEntries.shift() ?? [];

										const icon = structureEntry.props!.icon 
											? h(`img`, { src: withBase(structureEntry.props!.icon) }) 
											: null;
										const span = [icon, structureEntry.props!.name];
										const link = structureEntry.props!.link
											? h(`a`, { class: `nowrap`, href: withBase(structureEntry.props!.link) }, span) 
											: span
										
										const display = h(`span`, { class: `nowrap` }, link)

										consumer([h(`td`, { rowspan: length }, display), ...first])
										containerEntries.forEach(consumer);
									})
									.toArray();

								const length = structureEntries.length;
								const first = structureEntries.shift() ?? [];
								const item = Registries.ITEM.getOrThrow(Identifier.getCodec().parse(itemEntry.props!.id));
								const name = Boolean(itemEntry.props!.bold)
									? h(`b`, item.getName())
									: item.getName();
								const img = h(`img`, { width: 16, height: 16, src: item.getInventoryIcon() })
								const display = h(`span`, { class: `nowrap` }, [img, name])

								consumer([h(`td`, { rowspan: length }, display), ...first])
								structureEntries.forEach(consumer);
							})
							.map(children => h(`tr`, children))
							.toArray()
					)
					: null
			]
		);
	}
});
</script>

<style lang="css" scoped>
.nowrap {
	display: inline-flex;
	white-space: nowrap;
	align-items: center;
	gap: 6px;
}

a {
	text-decoration: none;
}
</style>
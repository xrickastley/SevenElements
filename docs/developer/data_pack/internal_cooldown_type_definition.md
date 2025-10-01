---
outline: 2
prev: false
---

# Internal Cooldown Type definition

[Internal Cooldown Types](../../guide/elements/internal_cooldown.md) are stored as [JSON](https://minecraft.wiki/w/JSON) files within a [data pack](https://minecraft.wiki/w/Data_pack) in the path `data/<namespace>/internal_cooldowns`.

This excludes the default types: `seven-elements:default` and `seven-elements:none`, which **cannot** be overriden by data-driven means.

::: tip
Before you create one, consider trying `seven-elements:default` for your use case. After all, elements should be applied strategically, not "spammingly".
:::

## JSON format

<div class="treeview">
	<ul>
		<li>
			<span title="NBT Compound / JSON Object" class="nbt-sprite sprite" style="background-position:-48px -16px;background-size:64px auto;height:16px;width:16px"></span>: The root object.
			<ul>
				<li>
					<span title="Int" class="nbt-sprite sprite" style="background-position:-48px 0px;background-size:64px auto;height:16px;width:16px"></span> <b>gauge_sequence</b>: Value between 0 and 2,147,483,647 (inclusive) — Controls the amount of time in ticks before an Element can be applied again.
				</li>
				<li>
					<span title="Int" class="nbt-sprite sprite" style="background-position:-48px 0px;background-size:64px auto;height:16px;width:16px"></span> <b>reset_interval</b>: Value between 0 and 2,147,483,647 (inclusive) — Controls the amount of hits needed before an Element can be applied <b>within</b> the reset interval's timer.
				</li>
			</ul>
		</li>
	</ul>
</div>

### Examples

```json
{
	"gauge_sequence": 3,
	"reset_interval": 50
}
```
This example creates an Internal Cooldown Type with a `gauge_sequence` of `3` hits and a `reset_interval` of `50` ticks.
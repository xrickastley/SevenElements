---
outline: 2
---

# Internal Cooldown Type definition

[Internal Cooldown Types](../../guide/elements/Internal%20Cooldown.md) are stored as [JSON](https://minecraft.wiki/w/JSON) files within a [data pack](https://minecraft.wiki/w/Data_pack) in the path `data/<namespace>/internal_cooldowns`.

This excludes the default types: `seven-elements:default` and `seven-elements:none`, which **cannot** be overriden by data-driven means.

::: tip
Before you create one, consider trying `seven-elements:default` for your use case. After all, elements should be applied strategically.
:::

## JSON format

<Treeview>
	<TreeviewEntry type="compound" description="The root object">
		<TreeviewEntry type="int" title="gauge_sequence" description="(Optional) Value between 0 and 2,147,483,647 (inclusive) — Controls the amount of time in ticks before an Element can be applied again."/>
		<TreeviewEntry type="int" title="reset_interval" description="(Optional) Value between 0 and 2,147,483,647 (inclusive) — Controls the amount of hits needed before an Element can be applied within the reset interval's timer."/>
	</TreeviewEntry>
</Treeview>

### Examples

```json
{
	"gauge_sequence": 3,
	"reset_interval": 50
}
```
This example creates an Internal Cooldown Type with a `gauge_sequence` of `3` hits and a `reset_interval` of `50` ticks.
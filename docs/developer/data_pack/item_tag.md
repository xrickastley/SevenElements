---
next: false
---

# Item tag

An item [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [items](https://minecraft.wiki/w/Item). They are used when a [recipe](https://minecraft.wiki/w/Recipe) allows multiple different items as inputs and control many other gameplay features. See below for the use of each item tag. They can also be used when testing for item arguments in commands with `#<resource location>`, which succeeds if the item matches any of the items specified in the tag, and can be searched in the creative inventory by searching `#<resource location>`.

## List of tags

### heavy_weapon

Controls what items are considered "Heavy Weapons", which can trigger the [Shatter](../../guide/elements/elemental_reactions/frozen.md#shatter) reaction without the need to deal <span class="geo">**Geo DMG**</span>.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:heavy_weapon</b> <i>(3 values)</i>
			<ul>
				<li><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#axes"><code>#minecraft:axes</code></a></li>
				<li><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#pickaxes"><code>#minecraft:pickaxes</code></a></li>
				<li><code>minecraft:mace</code></li>
			</ul>
		</li>
	</ul>
</div>
# Item tag

An item [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [items](https://minecraft.wiki/w/Item). They are used when a [recipe](https://minecraft.wiki/w/Recipe) allows multiple different items as inputs and control many other gameplay features. See below for the use of each item tag. They can also be used when testing for item arguments in commands with `#<resource location>`, which succeeds if the item matches any of the items specified in the tag, and can be searched in the creative inventory by searching `#<resource location>`.

## List of tags

### attunable_items

Controls what items are attunable with an aligned [Elemental Attunement Smithing Template](../items/Elemental%20Attunement%20Smithing%20Template.md).

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:attunable_items" keyCount=true>
		<code><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#enchantable/armor">#minecraft:#enchantable/armor</a></code>
		<code><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#enchantable/weapon">#minecraft:#enchantable/weapon</a></code>
		<code>minecraft:trident</code>
	</TreeviewEntry>
</Treeview>

### heavy_weapon

Controls what items are considered "Heavy Weapons", which can trigger the [Shatter](../elemental_reactions/Frozen.md#shatter) reaction without the need to deal <span class="geo">**Geo DMG**</span>.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:heavy_weapon" keyCount=true>
		<code><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#axes">#minecraft:axes</a></code>
		<code><a href="https://minecraft.wiki/w/Item_tag_(Java_Edition)#pickaxes">#minecraft:pickaxes</a></code>
		<code>minecraft:mace</code>
	</TreeviewEntry>
</Treeview>
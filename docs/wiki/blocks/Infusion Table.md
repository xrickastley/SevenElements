# Infusion Table

<p style="display: flex; align-items: flex-start; gap: 1.5em;">
	<span style="flex: 1;">
		An <b>infusion table</b> is a <a href="https://minecraft.wiki/w/Block">block</a> used to spend <a href="https://minecraft.wiki/w/Experience">experience</a> to apply an elemental infusion to items.
	</span>
	<img-texture src="seven-elements:textures/item/infusion_table.png" style="width: 160px;" />
</p>

## Obtaining

### Breaking

An infusion table requires an [iron pickaxe](https://minecraft.wiki/w/Pickaxe) or better to be mined, in which case it drops itself. Otherwise, it drops nothing.

### Crafting

<br>

<CraftingRecipe>
	<Ingredient id="seven-elements:elemental_rune" slotId=2></Ingredient>
	<Ingredient id="minecraft:block_of_gold" slotId=5></Ingredient>
	<Ingredient id="minecraft:smooth_stone_slab" slotId=8></Ingredient>
	<Ingredient id="seven-elements:infusion_table" slotId=0></Ingredient>
</CraftingRecipe>

## Usage

<p style="display: flex; align-items: flex-start; gap: 1.25em;">
	<span style="flex: 1;">
		An item can be infused by using an infusion table and placing the item in the centered input slot. Once an item is placed, the "Infuse" button is shown. <br> <br>
		To successfully infuse an item, the player must have at least 10 levels of experience. Otherwise, the Infuse button will appear disabled, and cannot be used. <br> <br>
		The infusion table is 1 <sup>1</sup>/<sub>4</sub> blocks high.
	</span>
	<img src="../.media/blocks/infusion_table_interface.png" alt="Infusion Table Interface" style="width: 200px;">
</p>

### Elemental infusion

&nbsp; &nbsp; *Main page: [Elemental Combat/Elemental Infusion](../../guide/elements/Elemental%20Combat.md#elemental-infusion)*

The infusion table's main purpose is to infuse items with the elements. The table can infuse **all** items with the elements.

If an item is already infused with an element, it is replaced with another random elemental infusion.

The elemental infusion on the item will also have a random selected amount of corresponding [Elemental Gauge Units](../../guide/elements/Elemental%20Combat.md) from either 1.0, 1.5 or 2.0.

### Removing elemental infusions

With a [Grindstone](https://minecraft.wiki/w/Grindstone), an item with an Elemental Infusion may be stripped of its Elemental Infusion.

Like [enchanted](https://minecraft.wiki/w/Enchanted) items, placing an infused item in either input slot of the Grindstone makes an "un-infused" item appear in the output slot. This works regardless if the item is enchanted or not.

Removing the "un-infused" item from the output slot deletes the input item and causes the Grindstone to drop some [experience](https://minecraft.wiki/w/Experience). The experience dropped is proportional to the [Gauge Units](../../guide/elements/Elemental%20Gauge%20Theory.md) the Elemental Infusion would've applied.
# Elemental Rune

<p style="display: flex; align-items: flex-start; gap: 1.5em;">
	<span style="flex: 1;">
		An <strong>elemental rune</strong> is a crafting ingredient mainly used as an intermediate step in crafting element-related items. It is required to craft the <a href="../blocks/Infusion%20Table.md">infusion table</a> and to duplicate <a href="./Elemental%20Attunement%20Smithing%20Template.md">elemental attunement smithing templates</a>.
	</span>
	<img-texture src="seven-elements:textures/item/elemental_rune.png" style="width: 160px;" />
</p>

## Obtaining

### Crafting

In [Survival](https://minecraft.wiki/w/Survival) mode, elemental runes are obtainable only through crafting.

<CraftingRecipe>
	<Ingredient tag="minecraft:stone_crafting_materials" slotId=1 />
	<Ingredient id="minecraft:fire_charge" slotId=2 />
	<Ingredient id="minecraft:water_bucket" slotId=3 />
	<Ingredient id="minecraft:blue_ice" slotId=4 />
	<Ingredient id="minecraft:smooth_stone" slotId=5 />
	<Ingredient id="minecraft:wind_charge" slotId=6 />
	<Ingredient tag="minecraft:leaves" slotId=7 />
	<Ingredient id="minecraft:lightning_rod" slotId=9 />
	<Ingredient id="seven-elements:elemental_rune" count=2 slotId=0 />
</CraftingRecipe>

## Usage

The only usage of elemental runes is to craft an [infusion table](../blocks/Infusion%20Table.md), duplicate [elemental attunement smithing templates](./Elemental%20Attunement%20Smithing%20Template.md) or be used as a smithing ingredient for elemental attunement.

<CraftingRecipe>
	<Ingredient id="seven-elements:elemental_rune" slotId=2></Ingredient>
	<Ingredient id="minecraft:block_of_gold" slotId=5></Ingredient>
	<Ingredient id="minecraft:smooth_stone_slab" slotId=8></Ingredient>
	<Ingredient id="seven-elements:infusion_table" slotId=0></Ingredient>
</CraftingRecipe>
<br>
<CraftingRecipe>
	<Ingredient id="minecraft:diamond" slotId=1 />
	<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=2 />
	<Ingredient id="minecraft:diamond" slotId=3 />
	<Ingredient id="minecraft:diamond" slotId=4 />
	<Ingredient id="minecraft:totem_of_undying" slotId=5 />
	<Ingredient id="minecraft:diamond" slotId=6 />
	<Ingredient id="minecraft:diamond" slotId=7 />
	<Ingredient id="seven-elements:elemental_rune" slotId=8 />
	<Ingredient id="minecraft:diamond" slotId=9 />
	<Ingredient id="seven-elements:elemental_attunement_smithing_template" count=2 slotId=0 />
</CraftingRecipe>
<br>
<MultiRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:pyro_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:pyro_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:hydro_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:hydro_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:anemo_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:anemo_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:electro_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:electro_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:dendro_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:dendro_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:cryo_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:cryo_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
	<CraftingRecipe>
		<Ingredient id="seven-elements:geo_attunement_smithing_template" slotId=2 />
		<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
		<Ingredient id="seven-elements:elemental_rune" slotId=8 />
		<Ingredient id="seven-elements:geo_attunement_smithing_template" count=2 slotId=0 />
	</CraftingRecipe>
</MultiRecipe>
<br>
<SmithingRecipe>
	<Ingredient id="seven-elements:special/mace_hydro_infused" slotId=1></Ingredient>
	<Ingredient id="seven-elements:hydro_attunement_smithing_template" slotId=2></Ingredient>
	<Ingredient id="seven-elements:elemental_rune" slotId=3></Ingredient>
	<Ingredient id="seven-elements:special/mace_hydro_attuned" slotId=0></Ingredient>
</SmithingRecipe>
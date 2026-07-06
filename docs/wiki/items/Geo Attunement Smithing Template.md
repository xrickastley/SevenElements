# Geo Attunement Smithing Template

<p style="display: flex; align-items: flex-start; gap: 1.5em;">
	<span style="flex: 1;">
		A <strong>geo attunement smithing template</strong> is a type of <a href="./Elemental%20Attunement%20Smithing%20Template.md">elemental attunement smithing template</a> used in smithing tables that can be used to add an <a href="../../guide/elements/Elemental%20Attunement.md">elemental attunement</a> to weapons or armor. It is consumed when used, but can be duplicated using an existing template and an <a href="./Elemental%20Rune.md">elemental rune</a>.
	</span>
	<img-texture src="seven-elements:textures/item/geo_attunement_smithing_template.png" style="width: 160px;" />
</p>

## Obtaining

A geo attunement smithing template can be obtained by aligning an elemental attunement smithing template to <span class="geo">**Geo**</span>. Once obtained, it can be duplicated using the crafting recipe below.

### Cloning

<br>
<CraftingRecipe>
	<Ingredient id="seven-elements:geo_attunement_smithing_template" slotId=2 />
	<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
	<Ingredient id="seven-elements:elemental_rune" slotId=8 />
	<Ingredient id="seven-elements:geo_attunement_smithing_template" count=2 slotId=0 />
</CraftingRecipe>

### Elemental alignment

&nbsp; &nbsp; *Main page: [Elemental Attunement Smithing Template § Elemental alignment](./Elemental%20Attunement%20Smithing%20Template.md#elemental-alignment)*

An elemental attunement smithing template may be aligned to a geo attunement smithing template by being suffocated inside stone-related blocks.

## Usage

### Crafting ingredient

It is an ingredient in its own recipe, making it possible to craft copies after the initial item is obtained.

<CraftingRecipe>
	<Ingredient id="seven-elements:geo_attunement_smithing_template" slotId=2 />
	<Ingredient id="seven-elements:elemental_attunement_smithing_template" slotId=5 />
	<Ingredient id="seven-elements:elemental_rune" slotId=8 />
	<Ingredient id="seven-elements:geo_attunement_smithing_template" count=2 slotId=0 />
</CraftingRecipe>

### Smithing ingredient

Using the smithing table, the geo attunement smithing template can be applied to weapons and tools, using an elemental rune.

<SmithingRecipe>
	<Ingredient id="seven-elements:special/mace_geo_infused" slotId=1></Ingredient>
	<Ingredient id="seven-elements:geo_attunement_smithing_template" slotId=2></Ingredient>
	<Ingredient id="seven-elements:elemental_rune" slotId=3></Ingredient>
	<Ingredient id="seven-elements:special/mace_geo_attuned" slotId=0></Ingredient>
</SmithingRecipe>
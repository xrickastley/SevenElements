# Elemental Attunement Smithing Template

<p style="display: flex; align-items: flex-start; gap: 1.5em;">
	<span style="flex: 1;">
		An <strong>elemental attunement smithing template</strong> is a type of <a href="https://minecraft.wiki/w/Smithing_Template">smithing template</a> used in smithing tables that can be aligned with an element to add an <a href="../../guide/elements/Elemental%20Attunement.md">elemental attunement</a> to weapons or armor. It is consumed when aligned, but can be duplicated using an existing template, a <a href="https://minecraft.wiki/w/Totem_of_Undying">totem of undying</a>, an <a href="./Elemental%20Rune.md">elemental rune</a>, and <a href="https://minecraft.wiki/w/Diamond">diamonds</a>.
	</span>
	<img-texture src="seven-elements:textures/item/elemental_attunement_smithing_template.png" style="width: 160px;" />
</p>

## Obtaining

An elemental attunement smithing template can be found in various structures. Once obtained, it can be duplicated using the crafting recipe below.

### Cloning

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

### Generated loot

<ItemChanceTable>
	<ItemEntry id="seven-elements:elemental_attunement_smithing_template" bold=true>
		<StructureEntry name="Ancient City" icon="https://minecraft.wiki/images/EnvSprite_ancient-city.png" link="https://minecraft.wiki/w/Ancient_City">
			<ContainerEntry name="Chest" quantity="1–2" chance="7.5%" />
		</StructureEntry>
		<StructureEntry name="Mineshaft" icon="https://minecraft.wiki/images/EnvSprite_abandoned-mineshaft.png" link="https://minecraft.wiki/w/Mineshaft">
			<ContainerEntry name="Chest" quantity="1" chance="1.3%" />
		</StructureEntry>
		<StructureEntry name="Bastion Remnant" icon="https://minecraft.wiki/images/EnvSprite_bastion-remnant.png" link="https://minecraft.wiki/w/Bastion_Remnant">
			<ContainerEntry name="Treasure Chest" quantity="1" chance="2.5%" />
		</StructureEntry>
		<StructureEntry name="Desert Pyramid" icon="https://minecraft.wiki/images/EnvSprite_desert-pyramid.png" link="https://minecraft.wiki/w/Desert_Pyramid">
			<ContainerEntry name="Chest" quantity="1" chance="2.5%" />
		</StructureEntry>
		<StructureEntry name="Jungle Pyramid" icon="https://minecraft.wiki/images/EnvSprite_jungle-pyramid.png" link="https://minecraft.wiki/w/Jungle_Pyramid">
			<ContainerEntry name="Chest" quantity="1" chance="1.6%" />
		</StructureEntry>
		<StructureEntry name="Trial Chambers" icon="https://minecraft.wiki/images/EnvSprite_trial-chambers.png" link="https://minecraft.wiki/w/Trial_Chambers">
			<ContainerEntry name="Intersection Chest" quantity="1" chance="2.0%" />
		</StructureEntry>
		<StructureEntry name="Shipwreck" icon="https://minecraft.wiki/images/EnvSprite_shipwreck.png" link="https://minecraft.wiki/w/Shipwreck">
			<ContainerEntry name="Treasure Chest" quantity="1" chance="0.8%" />
		</StructureEntry>
		<StructureEntry name="Monster Room" icon="https://minecraft.wiki/images/EnvSprite_dungeon.png?cd556" link="https://minecraft.wiki/w/Monster_Room">
			<ContainerEntry name="Chest" quantity="1" chance="1.3%" />
		</StructureEntry>
		<StructureEntry name="Stronghold" icon="https://minecraft.wiki/images/EnvSprite_stronghold.png" link="https://minecraft.wiki/w/Stronghold">
			<ContainerEntry name="Library Chest" quantity="1" chance="2.5%" />
		</StructureEntry>
		<StructureEntry name="Woodland Mansion" icon="https://minecraft.wiki/images/EnvSprite_mansion.png" link="https://minecraft.wiki/w/Woodland_Mansion">
			<ContainerEntry name="Chest" quantity="1" chance="3.1%" />
		</StructureEntry>
		<StructureEntry name="Village" icon="https://minecraft.wiki/images/EnvSprite_new-village.png?3e8a5" link="https://minecraft.wiki/w/Village">
			<ContainerEntry name="Weaponsmith's Chest" quantity="1" chance="0.5%" />
		</StructureEntry>
	</ItemEntry>
</ItemChanceTable>

## Usage

### Crafting ingredient

An elemental attunement smithing template can be duplicated with diamonds, a totem of undying, and an elemental rune.

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

Aligned elemental attunement smithing templates can be duplicated with a blank elemental attunement smithing template and an elemental rune.

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

### Elemental alignment

An elemental attunement smithing template can be aligned with an element, turning it into one of the seven aligned elemental attunement smithing templates.

To align an elemental attunement smithing template with an element, you must have at least one of the item inside your inventory while performing the alignment, which usually involves doing something related to the element.

When elemental alignment succeeds, only a single elemental attunement smithing template is aligned, regardless if you have multiple elemental attunement smithing templates.

| Alignment	| Requirement	| Base chance	| Pity start	| Chance <br> per pity	| Tick method	|
|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|---------------|-----------------------|-------------------|
| <span class=nowrap>Pyro alignment</span>		| Be fully submerged<foot-ref id=1 /> inside [lava](https://minecraft.wiki/w/Lava) while being in [The Nether](https://minecraft.wiki/w/The_Nether).	| `0.025%`		| `1,800`		| `0.25%`				| per tick			|
| <span class=nowrap>Hydro alignment</span>		| Be fully submerged<foot-ref id=1 /> inside [water](https://minecraft.wiki/w/Water) while having no air left.											| `0.025%`		| `900`			| `0.25%`				| per tick			|
| <span class=nowrap>Anemo alignment</span>		| Be airborne.																																			| `0.025%`		| `1,800`		| `0.25%`				| per tick			|
| <span class=nowrap>Electro alignment</span>	| Be struck by lightning.																																| `0%`			| `6`			| `50%`					| per tick			|
| <span class=nowrap>Dendro alignment</span> 	| Break specific nature-related<foot-ref id=2 /> blocks.																								| `0.001%`		| `2,000`		| `1.6%`				| per block broken	|
| <span class=nowrap>Cryo alignment</span>		| Be frozen<foot-ref id=3 />.																															| `0.025%`		| `900`			| `0.25%`				| per tick			|
| <span class=nowrap>Geo alignment</span>		| Suffocate inside specific stone-related<foot-ref id=4 /> blocks.																						| `0.025%`		| `900`			| `0.25%`				| per tick			|

#### Pity system

When performing an elemental alignment, a hidden pity system determines whether the alignment succeeds or not. This guarantees that you'll eventually align an elemental attunement smithing template with an element given that you've attempted the alignment enough times.

When you fulfill the conditions for an elemental alignment, an single attempt is made based on the **tick method**, with the chance for this attempt succeeding being provided by the following formula.

$$
\text{Chance}_\text{Final} = \text{Chance}_\text{Base} + \text{Chance}_\text{Per pity} \times max(\text{Pity}_\text{Counter} - \text{Pity}_\text{Start}, 0)
$$

The chance for elemental alignment succeeding will initially be $\text{Chance}_\text{Base}$. If elemental alignment doesn't succeed, $\text{Pity}_\text{Counter}$ is incremented once.

Once you've performed the elemental alignment enough times, $\text{Pity}_\text{Start}$ times to be exact, pity will begin to be factored in. Each failed alignment attempt after $\text{Pity}_\text{Start}$ will each increase the chance for elemental alignment by $\text{Chance}_\text{Per pity}$, factored in the next elemental alignment attempt.

When elemental alignment succeeds, $\text{Pity}_\text{Counter}$ is reset back to zero.

Additionally, for most elemental alignments, $\text{Pity}_\text{Counter}$ is reset back to zero when you stop fulfilling the requirement for alignment.

## Notes

1. <foot-note id=1 /> Your hitbox must be fully inside the fluid to be considered "fully submerged". 
2. <foot-note id=2 /> Nature-related blocks refer to blocks inside the [`seven-elements:progresses_dendro_attunement`](../tags/Block%20tag.md#progresses_dendro_attunement) block tag.
3. <foot-note id=3 /> Freeze, either via [Powder Snow](https://minecraft.wiki/w/Powder_Snow#Freezing) or by other means.
4. <foot-note id=4 /> Stone-related blocks refer to blocks inside the [`seven-elements:progresses_geo_attunement`](../tags/Block%20tag.md#progresses_geo_attunement) block tag.
---
outline: [1, 2]
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Frozen
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Frozen is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="cryo">**Cryo**</span> is applied onto an entity already affected by <span class="hydro">**Hydro**</span> or vice versa.

Frozen by itself deals **no damage**. Instead, it applies a <span class="cryo">**Freeze**</span> aura and the "Frozen" Status Effect onto the target for a certain period of time.

When an entity with the <span class="cryo">**Freeze**</span> aura receives <span class="geo">**Geo DMG**</span> or is hit by a [Heavy Weapon](../../../developer/data_pack/item_type_tag.md#heavy_weapon), the [**Shatter**](#shatter) reaction is triggered.

Inflicting <span class="pyro">**Pyro**</span>/<span class="electro">**Electro**</span>/<span class="hydro">**Anemo**</span> on a Freeze aura will consume it to trigger [Melt](./melt.md)/[Superconduct](./superconduct.md)/[Swirl](./swirl.md) respectively.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/frozen.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction ID: `seven-elements:frozen`

<div><br></div><hr>

# Shatter
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Shatter is the [Elemental Reaction](../elemental_reactions.md) triggered when an entity inflicted with the <span class="cryo">**Freeze**</span> aura receives <span class="geo">**Geo DMG**</span> or is **directly** hit by a [Heavy Weapon](../../../developer/data_pack/item_type_tag.md#heavy_weapon).

Upon triggering Shatter, the <span class="cryo">**Freeze**</span> aura is removed. If Shatter was triggered by dealing <span class="geo">**Geo DMG**</span>, the <span class="geo">**Geo**</span> attack that dealt the damage will sustain no gauge deduction.	

Unlike **Genshin Impact**, Shatter has its own *reaction text*!

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/shatter.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **3**  
Reaction ID(s):

- `seven-elements:shatter` (triggered by being hit with an [Axe](https://minecraft.wiki/w/Axe) or [Pickaxe](https://minecraft.wiki/w/Pickaxe))
- `seven-elements:shatter_geo` (triggered by receiving <span class="geo">**Geo DMG**</span>)


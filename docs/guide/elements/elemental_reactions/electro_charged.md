---
outline: 1
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Electro-Charged
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Electro-Charged is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="electro">**Electro**</span> is applied onto an entity already affected by <span class="hydro">**Hydro**</span> or vice versa.

This reaction deals <span class="electro">**Electro DMG**</span> over time to the Electro-Charged entity. 

Electro-Charged does not apply <span class="electro">**Electro**</span> to any targets hit, and therefore, cannot trigger any further elemental reactions.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/electro-charged.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

When there are nearby entities that are affected by <span class="hydro">**Hydro**</span>, <span class="electro">**Electro DMG**</span> is also dealt to them when the Electro-Charged entity takes Electro-Charged DMG, indicated by the "lightning" effect on the target.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/electro-charged-AoE.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **2.5**  
Reaction ID: `seven-elements:electro-charged`
---
outline: 1
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Superconduct
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Superconduct is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="electro">**Electro**</span> is applied onto an entity already affected by <span class="cryo">**Cryo**</span> or vice versa.

This reaction deals <span class="cryo">**AoE Cryo DMG**</span> in a **5m** radius and applies the "Superconduct" Status Effect to all entities excluding the entity triggering the reaction, which reduces their **Physical RES** by **40%** for **12** seconds.

Superconduct does not apply <span class="cryo">**Cryo**</span> to any targets hit, and therefore, cannot trigger any further elemental reactions.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/superconduct.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **1.5**  
Reaction ID(s): 

- `seven-elements:superconduct`
- `seven-elements:superconduct_frozen` (triggered on the <span class="cryo">**Freeze**</span> aura)  

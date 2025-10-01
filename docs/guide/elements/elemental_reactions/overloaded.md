---
outline: 1
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Overloaded
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Overloaded is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="electro">**Electro**</span> is applied onto an entity already affected by <span class="pyro">**Pyro**</span> or vice versa.

This reaction causes an explosion of power **3** and deals <span class="pyro">**AoE Pyro DMG**</span> to all entities within the approximately **5m** explosion radius excluding the entity triggering the reaction.

Overloaded does not apply <span class="pyro">**Pyro**</span> to any targets hit, and therefore, cannot trigger any further elemental reactions.

::: tip
You can disable <span style="color: #fc7fa4">**Overloaded**</span> from destroying blocks by setting the `overloadedBlockDestruction` gamerule to `false`.
:::

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/overloaded.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **2.75**  
Reaction ID: `seven-elements:overloaded`
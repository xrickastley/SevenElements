---
outline: [1, 2]
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Quicken
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Quicken is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="dendro">**Dendro**</span> is applied onto an entity already affected by <span class="electro">**Electro**</span> or vice versa.

Quicken by itself deals **no damage**. Instead, it applies a <span class="quicken">**Quicken**</span> aura onto the target for a certain period of time.

When an entity with the <span class="quicken">**Quicken**</span> aura receives <span class="dendro">**Dendro DMG**</span> or <span class="electro">**Electro DMG**</span>, the [**Spread**](#spread) and [**Aggravate**](#aggravate) reactions are triggered, respectively.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/quicken.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction ID: `seven-elements:quicken`

<div><br></div><hr>

# Spread
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Spread is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="dendro">**Dendro**</span> is applied onto an entity already affected by <span class="quicken">**Quicken**</span>.

This reaction increases the damage of the <span class="dendro">**Dendro**</span> attack that triggered the reaction with an additive flat DMG bonus.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/spread.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **1.25**  
Reaction ID: `seven-elements:spread`

<div><br></div><hr>

# Aggravate
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Aggravate is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="electro">**Electro**</span> is applied onto an entity already affected by <span class="quicken">**Quicken**</span>.

This reaction increases the damage of the <span class="electro">**Electro**</span> attack that triggered the reaction with an additive flat DMG bonus.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/aggravate.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **1.15**  
Reaction ID: `seven-elements:aggravate`

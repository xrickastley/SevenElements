---
outline: 1
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/Elemental_Reactions'
---

# Burning

Burning is the [Elemental Reaction](../../guide/elements/Elemental%20Reactions.md) triggered when <span class="pyro">**Pyro**</span> is applied onto an entity already affected by <span class="dendro">**Dendro**</span> or vice versa.

This reaction deals <span class="pyro">**AoE Pyro DMG**</span> and applies 1 [gauge unit](../../guide/elements/Elemental%20Gauge%20Theory.md#elemental-auras-and-the-aura-tax) of <span class="pyro">**Pyro**</span> to all entities in a **1m** radius. This Pyro application has an [Internal Cooldown](../../guide/elements/Internal%20Cooldown.md) of 2 seconds. 

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../.media/elemental_reactions/burning.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **0.25**  
Reaction ID(s): 

- `seven-elements:burning`
- `seven-elements:burning_quicken` (triggered on the <span class="quicken">**Quicken**</span> aura)
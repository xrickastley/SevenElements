---
outline: 1
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Vaporize
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Vaporize is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="hydro">**Hydro**</span> is applied onto an entity already affected by <span class="pyro">**Pyro**</span> or vice versa.

This reaction increases the damage of the <span class="hydro">**Hydro**</span> or <span class="pyro">**Pyro**</span> attack that triggered the reaction. If the reaction is triggered by a <span class="pyro">**Pyro**</span> attack, the damage is multiplied by **1.5**; If the reaction is triggered by a <span class="hydro">**Hydro**</span> attack, the damage is multiplied by **2**.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/vaporize.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data
  
Reaction ID(s):

- `seven-elements:vaporize_hydro` (triggered if <span class="hydro">**Hydro**</span> was applied on a <span class="pyro">**Pyro**</span> aura)
- `seven-elements:vaporize_pyro` (triggered if <span class="pyro">**Pyro**</span> was applied on a <span class="hydro">**Hydro**</span> aura)
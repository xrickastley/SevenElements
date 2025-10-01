---
outline: 1
next: false
prev:
    text: 'Elemental Reactions'
    link: '/guide/elements/elemental_reactions'
---

# Swirl
<sup>&nbsp; &nbsp; < [Elemental Reactions](../elemental_reactions.md)</sup>

Swirl is the [Elemental Reaction](../elemental_reactions.md) triggered when <span class="anemo">**Anemo**</span> is applied onto an entity already affected by <span class="pyro">**Pyro**</span>/<span class="electro">**Electro**</span>/<span class="hydro">**Hydro**</span>/<span class="cryo">**Cryo**</span>.

This reaction applies the involved non-<span class="anemo">**Anemo**</span> element to all entities within a **5m** radius excluding the entity the reaction was triggered on. <span class="pyro">**Pyro**</span>, <span class="electro">**Electro**</span> and <span class="cryo">**Cryo**</span> Swirl will also deal **AoE DMG** of the involved element, including the entity the reaction was triggered on, while <span class="hydro">**Hydro**</span> Swirl only spreads the <span class="hydro">**Hydro**</span> aura and deals DMG to the target that the reaction was triggered upon.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../../media/elemental_reactions/swirl.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

### Internal Data

Reaction Multiplier: **0.6**  
Reaction ID(s): 

- `seven-elements:swirl_pyro` (triggered on the <span class="pyro">**Pyro**</span> aura)
- `seven-elements:swirl_hydro` (triggered on the <span class="hydro">**Hydro**</span> aura)
- `seven-elements:swirl_electro` (triggered on the <span class="electro">**Electro**</span> aura)
- `seven-elements:swirl_cryo` (triggered on the <span class="cryo">**Cryo**</span> aura)
- `seven-elements:swirl_frozen` (triggered on the <span class="cryo">**Freeze**</span> aura) 
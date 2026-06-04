# Game rule

Seven Elements aims to be as configurable as possible, so game rules have been provided for you to customize the world mechanics to your liking.

## List of game rules

The following is a list of game rules you may edit for a world.

<p align="center"><b>List of game rules</b></p>


| Rule name						| Description																| Default value	| Type		| Category		|
|-------------------------------|---------------------------------------------------------------------------|:-------------:|-----------|---------------|
| `do_elements`					| Whether elements can be applied.											| `true`		| integer	| Elements	|
| `level_multiplier`			| Controls the strength of Elemental Reactions.								| `5.0`			| double	| Elements	|
| `overloaded_block_destruction`| Whether the [<span style="color: #fc7fa4">Overloaded</span>](../elements/elemental_reactions/overloaded.md) reaction destroys blocks.						| `false`		| boolean	| Elements	|
| `overloaded_creates_fire`		| Whether the [<span style="color: #fc7fa4">Overloaded</span>](../elements/elemental_reactions/overloaded.md) reaction creates fire.						| `true`		| boolean	| Elements	|
| `pyro_from_fire`				| Whether standing in fire applies <span class="pyro">**Pyro**</span>		| `true`		| double	| Elements	|
| `hydro_from_water`			| Whether touching water applies <span class="hydro">**Hydro**</span>		| `true`		| double	| Elements	|
| `electro_from_lightning`		| Whether [Lightning Bolts](https://minecraft.wiki/w/Thunderstorm#Lightning) apply <span class="electro">**Electro**</span>	| `true`		| boolean	| Elements	|
| `infusion_table`				| Whether the [Infusion Table](../workstations/infusion_table.md) is enabled.									| `true`		| boolean	| Elements	|
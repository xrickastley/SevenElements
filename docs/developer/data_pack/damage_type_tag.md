# Damage type tag

A damage type [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [damage types](https://minecraft.wiki/w/Damage_type). Damage type tags can be used when testing for damage type arguments with `#<resource location>`, which succeeds if the damage type matches any of the damage types specified in the tag.

Data pack or mod developers who may wish to add a compatibility layer between their data pack or mod and Seven Elements may do so by adding their damage type into the proper Seven Elements damage type tag.

## List of tags

### has_pyro_infusion

Damage from these types will apply **1U** <span class="pyro">**Pyro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_pyro_infusion" keyCount=true>
		<code><a href="https://minecraft.wiki/w/Damage_type_tag_(Java_Edition)#is_fire">#minecraft:is_fire</a></code>
		<code>minecraft:explosion</code>
		<code>minecraft:player_explosion</code>
		<code>seven-elements:burning</code>
		<code>seven-elements:overloaded</code>
	</TreeviewEntry>
</Treeview>

### has_hydro_infusion

Damage from these types will apply **1U** <span class="hydro">**Hydro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_hydro_infusion" keyCount=true>
		<code>minecraft:drown</code>
	</TreeviewEntry>
</Treeview>

### has_anemo_infusion

Damage from these types will apply **1U** <span class="anemo">**Anemo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_anemo_infusion" keyCount=true>
		<code>seven-elements:swirl</code>
	</TreeviewEntry>
</Treeview>

### has_electro_infusion

Damage from these types will apply **1U** <span class="electro">**Electro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_electro_infusion" keyCount=true>
		<code>seven-elements:electro-charged</code>
	</TreeviewEntry>
</Treeview>

### has_dendro_infusion

Damage from these types will apply **1U** <span class="dendro">**Dendro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_dendro_infusion" keyCount=true>
		<code>seven-elements:dendro_core</code>
	</TreeviewEntry>
</Treeview>

### has_cryo_infusion

Damage from these types will apply **1U** <span class="cryo">**Cryo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_dendro_infusion" keyCount=true>
		<code>minecraft:freeze</code>
		<code>seven-elements:superconduct</code>
	</TreeviewEntry>
</Treeview>

### has_geo_infusion

Damage from these types will apply **1U** <span class="geo">**Geo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:has_geo_infusion" keyCount=true />
</Treeview>

### prevents_cooldown_trigger

Damage from these types will **not** trigger the damage cooldown, also known as the [Invulnerability timer](https://minecraft.wiki/w/Damage#Invulnerability_timer).

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:prevents_cooldown_trigger" keyCount=true>
		<code>seven-elements:burning</code>
		<code>seven-elements:dendro_core</code>
		<code>seven-elements:electro-charged</code>
		<code>seven-elements:overloaded</code>
		<code>seven-elements:shatter</code>
		<code>seven-elements:superconduct</code>
		<code>seven-elements:swirl</code>
	</TreeviewEntry>
</Treeview>
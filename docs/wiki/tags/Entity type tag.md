# Entity type tag

An entity type [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [entity](https://minecraft.wiki/w/Entity) types. It can be used in [`type` target selector arguments](https://minecraft.wiki/w/Target_selectors#Selecting_targets_by_type) and loot table conditions with `#<resource location>`, which checks if the entity's type matches any of the entity types specified in the tag. Entity type tags are also used to control a number of other gameplay features; see below for the use of each tag.

Data pack or mod developers who may wish to add a compatibility layer between their data pack or mod and Seven Elements may do so by adding their entity type into the proper Seven Elements entity type tag.

## List of tags

### deals_pyro_damage

Contains entity types that innately deal **1U** <span class="pyro">**Pyro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_pyro_damage" keyCount=true>
		<code>minecraft:blaze</code>
		<code>minecraft:magma_cube</code>
	</TreeviewEntry>
</Treeview>

### deals_hydro_damage

Contains entity types that innately deal **1U** <span class="hydro">**Hydro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_hydro_damage" keyCount=true>
		<code>minecraft:axolotl</code>
		<code>minecraft:elder_guardian</code>
		<code>minecraft:drowned</code>
		<code>minecraft:guardian</code>
	</TreeviewEntry>
</Treeview>

### deals_anemo_damage

Contains entity types that innately deal **1U** <span class="anemo">**Anemo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_anemo_damage" keyCount=true>
		<code>minecraft:breeze</code>
	</TreeviewEntry>
</Treeview>

### deals_electro_damage

Contains entity types that innately deal **1U** <span class="electro">**Electro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_electro_damage" keyCount=true />
</Treeview>

### deals_dendro_damage

Contains entity types that innately deal **1U** <span class="dendro">**Dendro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_dendro_damage" keyCount=true />
</Treeview>

### deals_cryo_damage

Contains entity types that innately deal **1U** <span class="cryo">**Cryo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_cryo_damage" keyCount=true>
		<code>minecraft:snow_golem</code>
		<code>minecraft:stray</code>
	</TreeviewEntry>
</Treeview>

### deals_geo_damage

Contains entity types that innately deal **1U** <span class="geo">**geo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:deals_geo_damage" keyCount=true />
</Treeview>

### ignored_targets

Contains entity types that are ignored as targets for both the [Hyperbloom](../elemental_reactions/Bloom.md#hyperbloom) Elemental Reaction and the [Elemental Shard](../elemental_reactions/Crystallize.md#elemental-shard).

Please consider only using this tag for entities that **truly** shouldn't be valid targets of these mechanics.

<Treeview>
	<TreeviewEntry type="list" title="#seven-elements:ignored_targets" keyCount=true>
		<code>minecraft:armor_stand</code>
		<code>seven-elements:crystallize_shard</code>
		<code>seven-elements:dendro_core</code>
	</TreeviewEntry>
</Treeview>
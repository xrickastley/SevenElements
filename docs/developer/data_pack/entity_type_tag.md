# Entity type tag

An entity type [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [entity](https://minecraft.wiki/w/Entity) types. It can be used in [`type` target selector arguments](https://minecraft.wiki/w/Target_selectors#Selecting_targets_by_type) and loot table conditions with `#<resource location>`, which checks if the entity's type matches any of the entity types specified in the tag. Entity type tags are also used to control a number of other gameplay features; see below for the use of each tag.

Data pack or mod developers who may wish to add a compatibility layer between their data pack or mod and Seven Elements may do so by adding their entity type into the proper Seven Elements entity type tag.

## List of tags

### deals_pyro_damage

Contains entity types that innately deal **1U** <span class="pyro">**Pyro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_pyro_damage</b> <i>(2 values)</i>
			<ul>
				<li><code>minecraft:blaze</code></li>
				<li><code>minecraft:magma_cube</code></li>
			</ul>
		</li>
	</ul>
</div>

### deals_hydro_damage

Contains entity types that innately deal **1U** <span class="hydro">**Hydro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_hydro_damage</b> <i>(4 values)</i>
			<ul>
				<li><code>minecraft:axolotl</code></li>
				<li><code>minecraft:elder_guardian</code></li>
				<li><code>minecraft:drowned</code></li>
				<li><code>minecraft:guardian</code></li>
			</ul>
		</li>
	</ul>
</div>

### deals_anemo_damage

Contains entity types that innately deal **1U** <span class="anemo">**Anemo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_anemo_damage</b> <i>(no values)</i>
		</li>
	</ul>
</div>

### deals_electro_damage

Contains entity types that innately deal **1U** <span class="electro">**Electro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_electro_damage</b> <i>(no values)</i>
		</li>
	</ul>
</div>

### deals_dendro_damage

Contains entity types that innately deal **1U** <span class="dendro">**Dendro**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_dendro_damage</b> <i>(no values)</i>
		</li>
	</ul>
</div>

### deals_cryo_damage

Contains entity types that innately deal **1U** <span class="cryo">**Cryo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_cryo_damage</b> <i>(2 values)</i>
			<ul>
				<li><code>minecraft:snow_golem</code></li>
				<li><code>minecraft:stray</code></li>
			</ul>
		</li>
	</ul>
</div>

### deals_geo_damage

Contains entity types that innately deal **1U** <span class="geo">**geo**</span> with the tag `seven-elements:mob_damage` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:deals_geo_damage</b> <i>(no values)</i>
		</li>
	</ul>
</div>

### ignored_targets

Contains entity types that are ignored as targets for both the [Hyperbloom](../../guide/elements/elemental_reactions/bloom.md#hyperbloom) Elemental Reaction and the [Elemental Shard](../../guide/elements/elemental_reactions/crystallize.md#elemental-shard).

Please consider only using this tag for entities that shouldn't be valid targets of these mechanics.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:ignored_targets</b> <i>(3 values)</i>
			<ul>
				<li><code>minecraft:armor_stand</code></li>
				<li><code>seven-elements:crystallize_shard</code></li>
				<li><code>seven-elements:dendro_core</code></li>
			</ul>
		</li>
	</ul>
</div>
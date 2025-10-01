# Damage type tag

A damage type [tag](https://minecraft.wiki/w/Tag_(Java_Edition)) is a group of [damage types](https://minecraft.wiki/w/Damage_type). Damage type tags can be used when testing for damage type arguments with `#<resource location>`, which succeeds if the damage type matches any of the damage types specified in the tag.

Data pack or mod developers who may wish to add a compatibility layer between their data pack or mod and Seven Elements may do so by adding their damage type into the proper Seven Elements damage type tag.

## List of tags

### has_pyro_infusion

Damage from these types will apply **1U** <span class="pyro">**Pyro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_pyro_infusion</b> <i>(11 values)</i>
			<ul>
				<li><code>#minecraft:is_fire</code></li>
				<li><code>minecraft:explosion</code></li>
				<li><code>minecraft:player_explosion</code></li>
				<li><code>seven-elements:burning</code></li>
				<li><code>seven-elements:overloaded</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_hydro_infusion

Damage from these types will apply **1U** <span class="hydro">**Hydro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_hydro_infusion</b> <i>(1 value)</i>
			<ul>
				<li><code>minecraft:drown</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_anemo_infusion

Damage from these types will apply **1U** <span class="anemo">**Anemo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_anemo_infusion</b> <i>(1 value)</i>
			<ul>
				<li><code>seven-elements:swirl</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_electro_infusion

Damage from these types will apply **1U** <span class="electro">**Electro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_electro_infusion</b> <i>(1 value)</i>
			<ul>
				<li><code>seven-elements:electro-charged</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_dendro_infusion

Damage from these types will apply **1U** <span class="dendro">**Dendro**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_dendro_infusion</b> <i>(1 value)</i>
			<ul>
				<li><code>seven-elements:dendro_core</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_cryo_infusion

Damage from these types will apply **1U** <span class="cryo">**Cryo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_cryo_infusion</b> <i>(2 values)</i>
			<ul>
				<li><code>minecraft:freeze</code></li>
				<li><code>seven-elements:superconduct</code></li>
			</ul>
		</li>
	</ul>
</div>

### has_geo_infusion

Damage from these types will apply **1U** <span class="geo">**Geo**</span> with the tag `seven-elements:damage_infusion` if no previous elemental infusion exists or the damage being dealt is **Physical**.

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:has_geo_infusion</b> <i>(no values)</i>
		</li>
	</ul>
</div>

### prevents_cooldown_trigger

Damage from these types will **not** trigger the damage cooldown, also known as the [Invulnerability timer](https://minecraft.wiki/w/Damage#Invulnerability_timer).

<div class="treeview">
	<ul>
		<li>
			<span title="NBT List / JSON Array" class="nbt-sprite sprite" style="background-position:-32px -32px;background-size:64px auto;height:16px;width:16px"></span> <b>#seven-elements:prevents_cooldown_trigger</b> <i>(7 values)</i>
			<ul>
				<li><code>seven-elements:burning</code></li>
				<li><code>seven-elements:dendro_core</code></li>
				<li><code>seven-elements:electro-charged</code></li>
				<li><code>seven-elements:overloaded</code></li>
				<li><code>seven-elements:shatter</code></li>
				<li><code>seven-elements:superconduct</code></li>
				<li><code>seven-elements:swirl</code></li>
			</ul>
		</li>
	</ul>
</div>
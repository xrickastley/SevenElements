---
next: false
---

# Internal Cooldown

**Internal Cooldown**, commonly abbreviated as **ICD**, is a game mechanic that regulates how often an ability can apply an element onto an entity.

## Introduction

When dealing elemental damage, we may fully expect that the element would **always** be applied. However, that isn't always the case.

<div align="center">
	<video width="95%" height="auto" controls>
		<source src="../media/elements/icd.mp4" type="video/mp4">
		Your browser does not support the video tag.
	</video>
</div>

As seen in the video above, despite clearly doing <span class="cryo">**Cryo DMG**</span>, the <span style="color: #f2be87">**Melt**</span> reaction isn't always triggered.

This is due to a mechanic known as Internal Cooldown, or **ICD** for short. As seen in the video above, the Internal Cooldown *regulates* when <span class="cryo">**Cryo**</span> is applied as an Element, resulting in not all of our <span class="cryo">**Cryo DMG**</span> instances triggering the <span style="color: #f2be87">**Melt**</span> reaction. 

However, the target still takes <span class="cryo">**Cryo DMG**</span>, even when <span class="cryo">**Cryo**</span> itself isn't applied.

## Overview

Internal Cooldown is handled by two properties: 

Internal Cooldown is based on the attack's **tag** and **type**, as well as the **entity** that dealt the damage. Attacks from the same entity that share the same **tag** and **type** will share ICD.

## Internal Cooldown Type

&nbsp; &nbsp; *"Internal Cooldown Type" redirects here. For the definiton of Internal Cooldown Types in data packs, see [Internal Cooldown Type definition](../../developer/data_pack/internal_cooldown_type_definition.md)*.

An Internal Cooldown Type contains two properties: the **reset interval** and the **gauge sequence**, commonly denoted as: **(ResetInterval)s/(GaugeSequence) hits**.

For an Element to be applied by the same ability, either:  

- The time from the previous Elemental Application to this Elemental Application exceeds the Internal Cooldown's **reset interval**, or
- The amount of "hits" from this ability that attempted to apply an Element exceeds the Internal Cooldown's **gauge sequence**.

If any of these conditions are fulfilled, the Internal Cooldown is considered to be inactive, allowing the Element to be applied.

It is also important to note that:

- The first hit after the **gauge sequence** applies an element, but does **not** clear the timer given by the **reset interval**.
- The first hit after the **reset interval** applies an element **and** clears both the timer given by the **reset interval** and the **gauge sequence**.

In simpler terms, clearing the **gauge sequence** allows you to apply an element without clearing the **reset interval**, effectively "sneaking in" an Elemental application, rewarding fast attackers. Slower attackers unable to clear the **gauge sequence** in time rely instead on the **reset interval**.

Most abilities in Genshin Impact follow the standard ICD of **2.5s**/**3** hits, represented in Seven Elements as `seven-elements:default`. Elemental Attacks that have a cooldown may also choose to have no ICD, represented in Seven Elements as `seven-elements:none`.
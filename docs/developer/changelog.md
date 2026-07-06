# Seven Elements Changelog

This page serves as the changelog for Seven Elements, to be mainly used by addon developers or for reference, as the formatting for the changelong on Modrinth is quite ambigious and sometimes, hard to follow considering the multiple versions uploaded.

::: tip
A version number: `x.y.z`, will be inclusive of its LTS version number: `x.y.z+1-LTS.v`, unless said LTS version number exists as a separate version number listed in this changelog.
:::

## Version 1.1: Attunement Update

::: warning
With the introduction of the Elemental Mastery attribute, `SevenElements.getLevelMultiplier()` and `ElementalReaction.getLevelMultiplier()` are both deprecated. You will now need an instance of `ElementalReaction` to properly calculate reaction damage, done via `ElementalReaction#getReactionStrength()`.

For a more in-depth developer changelog to this version of Seven Elements, refer to [this page](./changelog/Version%201.1.md).
:::

### Version 1.1.0

::: shift
#### Additions
- Added elemental attunement.
- Added new items.
	- `seven-elements:elemental_rune`
	- `seven-elements:elemental_attunement_smithing_template`
	- `seven-elements:pyro_attunement_smithing_template`
	- `seven-elements:hydro_attunement_smithing_template`
	- `seven-elements:anemo_attunement_smithing_template`
	- `seven-elements:electro_attunement_smithing_template`
	- `seven-elements:dendro_attunement_smithing_template`
	- `seven-elements:cryo_attunement_smithing_template`
	- `seven-elements:geo_attunement_smithing_template`
- Added advancement criterions.
	- `seven-elements:reaction_triggered`
	- `seven-elements:perform_attunement`
- Added new attributes.
	- `seven-elements:elemental_mastery`
	- `seven-elements:critical_rate`
	- `seven-elements:critical_damage`
	- `seven-elements:shield_strength`
- Added new recipes and recipe advancements.
- Added new advancements.
- Added subtitles.
- Added new game rule.
	- `pyroDoesFireEffects` / `seven-elements:pyro_does_fire_effects`
- Added uninfusing to the Infusion Table. ([#8](https://github.com/xrickastley/SevenElements/issues/8))

#### Fixes
- Fixed `electroFromLightning` gamerule implementation ([#11](https://github.com/xrickastley/SevenElements/issues/11))
- Fixed `pyroFromFire` gamerule implementation ([#13](https://github.com/xrickastley/SevenElements/issues/13))
- Fixed `seven-elements:elemental_infusion` advancement criterion.
- Fixed not being able to hit entities that take 0 DMG.
- Fixed Elemental DMG Bonus attributes applying.
- Fixed invalid elements being able to be applied as attunements.
- Fixed damage origin for Electro-Charged and Burning ticks.
- Fixed Frozen Crystallize not giving the Cryo Crystallize Shield.
- Fixed command errors and message formatting for `/element`.
- Fixed modifying item name via Anvil for infused items.
- Fixed elemental infusion screen background being too dark.
- [**~1.20**] Fixed thrown Tridents not applying their elemental infusions on hit.
- [**≥1.21.5**] Fixed element rendering.
- [**≥1.21.5**] Fixed possible error when upgrading a world from previous Minecraft versions.

#### Changes
- Enhanced various advancements.
- Increased minimum and maximum values for Elemental DMG Bonus% and RES% attributes.
- Changed DMG calculations for Elemental Reactions due to Elemental Mastery.
- Enhanced elemental infusion screen.
- Pyro-infused Projectiles can now apply Fire effects.
- Frozen is now stricter with disabling actions.
- Shot projectiles can use their infusion when possible.
- Infused items now display a static glint of their element.
:::

## Version 1.0: Release

### Version 1.0.6

::: shift
#### Additions
- Added `/element infusion apply <target> random command`. ([#10](https://github.com/xrickastley/SevenElements/issues/10))
- Added a game rule for the Overloaded Elemental Reaction creating Fire. ([#12](https://github.com/xrickastley/SevenElements/issues/12))
- Added initial groundwork for the Attunement system. (coming in a future update...)
- Added ZH-CN localization for all versions of Seven Elements.

#### Fixes
- Fixed Elemental DMG Bonus attributes applying.

#### Changes
- [**≥1.21**] Breezes now deal Anemo DMG
:::

### Version 1.0.5

:::shift
#### Additions
- [**26.1+patch.1**] Added ZH-CN localization.

#### Fixes
- Fixed Sprawling Shots visually disappearing client-side.
- Fixed Sprawling Shots targetting Creative Mode players.
- Fixed elements and ICDs persisting after death.
- [**~1.20**] Fixed data pack folder names. ([#6](https://github.com/xrickastley/SevenElements/issues/6))
- [**≥1.21.5**] Fixed possible memory leaks caused by repeated/unclosed BufferAllocator instances.
- [**1.21.11+patch.1**] Fixed crash upon attempting to pierce with a Spear without an elemental infusion.

#### Changes
- Optimized Sprawling Shot homing trajectory when close to the target.
- Rephrased death messages.
- Changed `overloadedBlockDestruction` to have a default value of `false`.
:::

### Version 1.0.4

::: shift
#### Fixes
- Fixed crits possibly not being registered visually.
- Fixed Swirl (Frozen) triggering abnormally.
- Fixed command message errors and argument parsing.

#### Changes
- Removed `.sevenelements` from the maven_group.
:::

### Version 1.0.3

::: shift
#### Fixes
- Fixed crash when spawning Ender Dragon in a non-end dimension. ([#4](https://github.com/xrickastley/SevenElements/issues/4))
- [**≥1.21.5**] Fixed Hydro applying abnormally in comparison to previous versions.
- [**≥1.21.9**] Fixed element icons rendering through blocks.
:::

### Version 1.0.3-LTS.1

::: shift
#### Additions
- Added compatibility with Spell Power and More RPG Classes. ([#2](https://github.com/xrickastley/SevenElements/issues/2))
- [**1.20.1**] Added Sinytra Connector compatibility. ([#3](https://github.com/xrickastley/SevenElements/issues/3))
:::

### Version 1.0.2

::: shift
#### Fixes
- Fixed issue when trying to summon Crystallize Shards via the `/summon` command.
- Fixed broken contact links in `fabric.mod.json`.

#### Additions
- Added item tag translations.
:::

### Version 1.0.1

::: shift
#### Fixes
- Fixed entity context in `/element infuse` command.
:::
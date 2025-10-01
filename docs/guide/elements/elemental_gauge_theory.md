# Elemental Gauge Theory

::: tip
You **technically** don't need to read all of this! This section is a summary into the inner workings of the theory behind Genshin's Elemental Combat system, which has been applied to **Seven Elements**. You may click off this page if you don't want to read further.
:::

## Introduction

In Genshin Impact and Seven Elements, when you apply an element through an attack, most people think that the element is applied as-is; thinking of applied elements as two states of *applied* and *not applied*. In reality, there is a system beneath the simple application of elements that allows you to take advantage of certain characters over the others.

When you apply an element through an attack, a certain amount of **Gauge Units** corresponding to that element are applied to the target. Each attack has a certain amount of **Gauge Units** that it can apply, which can differ across attacks. Simply put, this can be thought of as the amount of the applied element.

When an Element is applied to an enemy that isn't inflicted with an Element, the Element is considered an **Aura Element** or an **Elemental Aura**. When a reaction is triggered against an enemy **already** afflicted with an **Aura Element**, the element that *sparked* the reaction is the **Triggering Element**. <span class="geo">**Geo**</span> and **Anemo** cannot apply any Auras, and as such, cannot be **Aura Elements** (unless otherwise specified, i.e. Anemo Hypostasis, Geo Hypostasis).

### Elemental Auras and the Aura Tax

In the **Elemental Gauge Theory**, elements are represented as **gauges**, hence the name **Elemental Gauge Theory**/**Gauge Unit Theory**. This *gauge* represents how *much* of an Element is currently applied, and when referred to, are called **1-unit**, or **1U** for short.

The values for each gauge are dependent on the attack that inflicted the Element. For instance, Furina's Elemental Skill: [Salon Solitare](https://genshin-impact.fandom.com/wiki/Salon_Solitaire), is a **1U** <span class="hydro">**Hydro**</span> Attack. 

When an Element is applied as an **Elemental Aura**, it is *taxed* due to the **Aura Tax**, which states that if an elemental attack is being used to **apply** an **Elemental Aura**, the amount of Gauge Units it has is reduced by **20%**. For example, if a <span class="hydro">**Hydro**</span> **Aura** is created by Furina's Elemental Skill: [Salon Solitare](https://genshin-impact.fandom.com/wiki/Salon_Solitaire), a **1U** <span class="hydro">**Hydro**</span> Attack, the applied Gauge Units onto the attacked target will be **0.8U** due to the **Aura Tax**.

It is also important to note that when Elemental Auras are applied from multiple sources, they **do not** stack on top of each other. Instead, the Elemental Aura is simply *refreshed*. When an Elemental Aura is *refreshed*, the Elemental Application with the **highest Gauge Units** is used as the new Gauge Unit value. Additionally, *refreshing* an Elemental Aura will **not exempt** it from the **Aura Tax**.

For instance, if a target currently has a **1U** <span class="hydro">**Hydro**</span> Aura and they are attacked with a **2U** <span class="hydro">**Hydro**</span> Attack, the <span class="hydro">**Hydro**</span> Aura will now be a **2U** <span class="hydro">**Hydro**</span> Aura with its current Gauge Units being **1.6U** due to the **Aura Tax**.

::: tip
To visualize the elemental gauges better, you may enable **Developer > Show Elemental Gauges** in the Seven Elements config, and may optionally enable **Developer > Show Gauge Ruler** too!
:::

### Triggering Elements and Elemental Reactions

When a valid **Triggering Element** is applied to a target that has an **Elemental Aura**, an **Elemental Reaction** is triggered and a portion of the **Elemental Aura**'s gauge is consumed.

The amount of Gauge Units consumed are dependent on the attack that triggered the **Elemental Reaction**. For example, if a **1U** <span class="electro">**Electro**</span> Attack is applied onto an entity with the <span class="pyro">**Pyro**</span> aura to trigger the **Overloaded** reaction, the <span class="pyro">**Pyro**</span> aura is reduced by **1** Gauge Unit.

Each Reaction has its own **Reaction Cost**, also known as the **Reaction Coefficient**. The **Reaction Coefficient** is a factor that amplifies how much Gauge Units are consumed from the **Elemental Aura** when a reaction is triggered. For example, if Furina's Elemental Burst: Let the People Rejoice, a **1U** <span class="hydro">**Hydro**</span> Attack, reacts with a **1U** <span class="pyro">**Pyro**</span> Aura, Hydro Vaporize (also known as Forward Vaporize) is triggered, which has a **Reaction Coefficient** of **2**. This means that **2U** of the <span class="pyro">**Pyro**</span> aura are consumed, consuming all of the <span class="pyro">**Pyro**</span> aura.

### Aura Decay Rate and Decay Rate Inheritance
An **Elemental Aura** decays over time until it is depleted, where the decay rate in (s/U) is given by the function:
$$
D(x) = \frac{\text{Base Duration}}{\text{Initial Gauge}} = \frac{35}{4x} + \frac{25}{8}
$$
where, $x = \text{Gauge}_\text{Elemental Attack}$	.

When an Elemental Attack with **higher** initial Gauge Units is applied on top of an Elemental Aura with a lower amount of initial Gauge Units, the resulting aura will **inherit** the decay rate of the previous Elemental Aura.

However, **Decay Rate Inheritance** does not apply to the <span class="pyro">**Pyro**</span> aura.

## Advanced Mechanics, Simultaneous and Underlying Auras

::: tip
This section is a follow-up and assumes you have read and know the concepts from the [Elemental Gauge Theory](#elemental-gauge-theory) section. 
:::

This section contains the more *special* cases of elements that aren't handled by the standard system of **Aura Elements** and **Triggering Elements**.

### Simultaneous Auras

Normally, a target is only afflicted with a single Elemental Aura. However, there are instances where this may change and multiple simultaneous Elemental Auras may coexist with one another. The best example of this is the <span class="cryo">**Cryo**</span> and <span class="dendro">**Dendro**</span> auras, both of which do not react with each other, resulting in both coexisting as Elemental Auras.

This concept applies to only a couple of elements and reactions, being <span class="hydro">**Hydro**</span> and <span class="electro">**Electro**</span> (Electro-Charged), Burning, Quicken and Frozen, with some of these using the **Underlying Aura** subsystem.

### Underlying Auras

An **Underlying Aura** is a special type of [Simultaneous Aura](#simultaneous-auras), having its element "lying under" another aura. This allows Elemental Auras of higher priority to effectively *hide* Elemental Auras with lower priority. As such, when the Aura that *hides* these Underlying Auras is fully consumed, the Underlying Auras are *exposed*, allowing for them to be triggered with.

### Hydro and Electro (Electro-Charged)

Unlike standard Elemental Reactions where the **Elemental Aura** being consumed by the **Triggering Reaction**, when Electro-Charged is triggered, it allows **both** <span class="electro">**Electro**</span> and <span class="hydro">**Hydro**</span> to coexist as Elemental Auras. While both of these elements exist on a target, Electro-Charged deals one tick of DMG per second, consuming **0.4U** from both auras. Additionally, both auras will still continue to decay naturally while Electro-Charged is being triggered.

When a third Element is introduced, it can react with both <span class="electro">**Electro**</span> and <span class="hydro">**Hydro**</span> at the same time, allowing you to trigger two reactions at once. For instance, applying the <span class="pyro">**Pyro**</span> aura on a target with both the <span class="hydro">**Hydro**</span> and <span class="electro">**Electro**</span> aura allows both **Overloaded** and **Vaporize** to be triggered at the same time.

### Burning

When Burning is triggered, much like **Electro-Charged**, both <span class="pyro">**Pyro**</span> and <span class="dendro">**Dendro**</span> auras coexist with one another. However, the <span class="dendro">**Dendro**</span> aura will now decay at a special rate, while the <span class="pyro">**Pyro**</span> aura decays naturally. In addition to this, a <span class="pyro">**Burning**</span> aura is created on the target, which coexists with both <span class="pyro">**Pyro**</span> and <span class="dendro">**Dendro**</span> auras.

The special Dendro consumption rate (gauge units/second) is:
$$
D(x) = \text{max}(0.4, \text{ Natural Decay Rate}_\text{Dendro Aura} × 2)
$$

Do note that this Dendro consumption rate is applied per-tick while the <span class="pyro">**Burning**</span> aura is active, not per "reaction" like the Electro-Charged reaction.

<span class="pyro">**Burning**</span> also applies **1U** of <span class="pyro">**Pyro**</span> (2 sec ICD) per tick. The Burning aura coexists with the <span class="pyro">**Pyro**</span> and <span class="dendro">**Dendro**</span> auras, but also has a higher priority over them, allowing them to be [Underlying Auras](#underlying-auras). In addition to this, the <span class="pyro">**Burning**</span> aura and the <span class="dendro">**Dendro**</span> aura maintain the <span class="pyro">**Burning**</span> state, and as such, will end if either aura is depleted.

### Quicken

Quicken is triggered like a normal reaction, the only exception being its ability to generate a new Elemental Aura: the <span class="quicken">**Quicken**</span> aura. This <span class="quicken">**Quicken**</span> aura can trigger most <span class="dendro">**Dendro**</span> reactions, except another <span class="quicken">**Quicken**</span> reaction. 

For instance, if <span class="hydro">**Hydro**</span> is applied onto a <span class="quicken">**Quicken**</span> aura, it triggers <span class="quicken">**Bloom**</span> and it **may** also trigger <span class="electro">**Electro-Charged**</span>.

#### Quicken Aura Gauge
$$
\text{Gauge}_\text{Quicken Aura} = \text{min}(\text{Gauge}_\text{Origin Aura}, \text{Gauge}_\text{Trigger Element})
$$

#### Quicken Aura Duration
$$
\text{Duration (Seconds)}_\text{Quicken Aura} = \text{Gauge}_\text{Quicken Aura} \times 5 + 6
$$

Applying <span class="electro">**Electro**</span> onto a <span class="quicken">**Quicken**</span> Aura triggers **Aggravate**, but this <span class="electro">**Electro**</span> aura is allowed to co-exist with the <span class="quicken">**Quicken**</span> aura as an Underlying Aura. Likewise, applying <span class="dendro">**Dendro**</span> onto a <span class="quicken">**Quicken**</span> aura triggers <span class="quicken">**Spread**</span>, and this <span class="dendro">**Dendro**</span> aura is also allowed to co-exist with the <span class="quicken">**Quicken**</span> aura as an Underlying Aura.

Due to the coexistence of <span class="electro">**Electro**</span> and <span class="dendro">**Dendro**</span> with the <span class="quicken">**Quicken**</span> aura, it is possible to trigger another <span class="quicken">**Quicken**</span> reaction while the <span class="quicken">**Quicken**</span> aura is active.


### Freeze

The Frozen state created by the [Frozen](./elemental_reactions/frozen.md) Elemental Reaction is maintained by the <span class="cryo">**Freeze**</span> aura.

The <span class="cryo">**Freeze**</span> aura, while being a *sibling* element to <span class="cryo">**Cryo**</span>:
- Cannot react with <span class="hydro">**Hydro**</span> to trigger <span class="cryo">**Frozen**</span> again. 
- Triggers [Shatter](./elemental_reactions/frozen.md#shatter) upon being hit with a <span class="geo">**Geo**</span> attack or a [Heavy Weapon](../../developer/data_pack/item_tag.md#heavy_weapon).

#### Freeze Aura Gauge
$$
\text{Gauge}_\text{Freeze Aura} = 2 \times \text{min}(\text{Gauge}_\text{Origin Aura}, \text{Gauge}_\text{Trigger Element})
$$

#### Freeze Aura Duration
$$
\text{Duration (Seconds)}_\text{Freeze Aura} = 2 \sqrt{5 \times \text{Gauge}_\text{Freeze Aura} + 4} - 4
$$

<span class="hydro">**Hydro**</span> and <span class="cryo">**Cryo**</span> may be applied as Underlying Auras to the <span class="cryo">**Freeze**</span> aura, allowing you to trigger another <span class="cryo">**Frozen**</span> reaction while the <span class="cryo">**Freeze**</span> aura still exists.

The <span class="cryo">**Freeze**</span> aura may be extended by triggering another <span class="cryo">**Frozen**</span> reaction, which at that point, introduces a new factor: **decay**, which prevents the concept of "perma-Freeze".

When the <span class="cryo">**Freeze**</span> aura is reapplied, the duration that the entity is frozen and unfrozen for is now recorded, and are both used in our Decay Time Modifier.

$$
\text{Decay Time Modifier (DTM)} = \text{max}(0, \text{Time}_\text{Frozen} - 2 \times \text{Time}_\text{Unfrozen})
$$

The new duration of the Freeze aura is provided by the new formula:

$$
\text{Duration (Seconds)}_\text{Freeze Aura} = \sqrt{20 \times \text{Gauge}_\text{Freeze Aura} + (\text{DTM} \times 4)^2} - \text{DTM} - 4
$$

Once the Decay Time Modifier reaches 0, both $\text{Time}_\text{Frozen}$ and $\text{Time}_\text{Unfrozen}$ are discarded.

### Hydro-Freeze Double Aura

&nbsp; &nbsp; *Not to be confused with the **Underlying** <span class="hydro">**Hydro**</span> aura possible in [Freeze](#freeze)*.

<span class="hydro">**Hydro**</span> may coexist with the <span class="cryo">**Freeze**</span> aura. This double aura interaction is only achieved if a <span class="cryo">**Cryo**</span> attack (the Triggering element) **doesn't** consume the currently applied <span class="hydro">**Hydro**</span> aura (the Aura element). This interaction **cannot** happen in the reverse order.

This specific interaction creates a <span class="cryo">**Freeze**</span> aura that **coexists** alongside the <span class="hydro">**Hydro**</span> aura. However, you may **only** trigger **Swirl** against this double aura, as explained [here](https://genshin-impact.fandom.com/wiki/Elemental_Gauge_Theory/Simultaneous_Reaction_Priority#Freeze_+_Hydro). Applying <span class="pyro">**Pyro**</span> or <span class="electro">**Electro**</span> will only trigger their associated **Freeze** reaction, while <span class="geo">**Geo**</span> will trigger **Shatter** before triggering **Hydro Crystallize**.

### Cryo-Dendro Double Aura

Unlike all the other double aura interactions mentioned up to this point, the <span class="cryo">**Cryo**</span>-<span class="dendro">**Dendro**</span> double aura is the simplest and most unique, as it is simply the byproduct of <span class="cryo">**Cryo**</span> and <span class="dendro">**Dendro**</span> not having a reaction with each other.

When triggering reactions against this double aura, the <span class="cryo">**Cryo**</span> reaction is prioritized over the <span class="dendro">**Dendro**</span> one, allowing you to trigger the <span class="dendro">**Dendro**</span> reactions with the same <span class="hydro">**Hydro**</span> element at a lesser <span class="dendro">**Dendro**</span> gauge consumption. This mechanic allows the original <span class="dendro">**Dendro**</span> aura to last longer while still being able to react with the other elements.

This is actually the interaction that allows the "Fridge" mechanic to work, which takes advantage of the **Frozen** > **Bloom** Reaction priority to trigger more **Bloom** reactions with the same <span class="dendro">**Dendro**</span> aura.
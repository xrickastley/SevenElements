---
outline: false
next: false
prev:
    text: 'Commands'
    link: '/guide/misc/commands'
---

# `/damage`
<sup>&nbsp; &nbsp; < [Commands](../commands.md)</sup>

The `/damage` command can be used to apply a set amount of damage to specified entities.

Original: [`/damage` (Minecraft Wiki)](https://minecraft.wiki/Commands/damage)

:::tip
This command is originally a Minecraft command, and has been modified by **Seven Elements** to add extra functionality to it. You can find the new subcommands below.
:::

### Syntax:

```mcfunction
damage <target> <amount> [<damageType>] [element <element> <gaugeUnits> <tag> <type>] [by <entity>] [from <cause>]
```

Damages the specified target with the specified damage type infused with the specified element. 

- `<entity>` being a target selector, username, or UUID; can only select one at a time.
    - (e.g: `@a[limit = 1]`, `@p`, `_xRickAstley`, `b1b981b6-4081-4abe-afd8-e79269c6a339`)
- `<amount>` being a float (a decimal or whole number) greater than or equal to 0.0.
    - (e.g: `1`, `1.5`, `4`)
- `<element>` being the name of an element.
    - (e.g: `pyro`, `HYDRO`, `eLeCtRo`)
- `<gaugeUnits>` being a double (a decimal or whole number)
    - (e.g: `1`, `1.5`, `4`)
- `<tag>` being a valid [Internal Cooldown Tag](../../elements/internal_cooldown.md) string, defaults to the special `none` Internal Cooldown Tag.
    - (e.g: `damage`, `seven-elements:infusion`, `"Custom Infusion Tag"`, `""` (none))
- `<type>` being a valid [Internal Cooldown Type](../../elements/internal_cooldown.md) id, defaults to `seven-elements:default`.
    - (e.g. `seven-elements:default`, `seven-elements:none`)
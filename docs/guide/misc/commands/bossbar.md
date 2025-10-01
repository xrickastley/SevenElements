---
outline: false
next: false
prev:
    text: 'Commands'
    link: '/guide/misc/commands'
---

# `/bossbar`
<sup>&nbsp; &nbsp; < [Commands](../commands.md)</sup>

The `/bossbar` command can be used to create, modify and list bossbars.

Original: [`/bossbar` (Minecraft Wiki)](https://minecraft.wiki/Commands/bossbar)

:::tip
This command is originally a Minecraft command, and has been modified by **Seven Elements** to add extra functionality to it. You can find the new subcommands below.
:::

### Syntax:

```mcfunction
bossbar set <id> entity <entity>
```

Sets the bossbar's entity. 

This only displays the entity's currently applied elements at the bottom of the bossbar, and will not sync any of the bossbar's values with the specified entity.

- `<id>` being the namespace and ID of a bossbar
- `<entity>` being a target selector, username, or UUID; can only select one at a time.
    - (e.g: `@a[limit = 1]`, `@p`, `_xRickAstley`, `b1b981b6-4081-4abe-afd8-e79269c6a339`)

<br>

```mcfunction
bossbar get <id> entity
```

Gets the bossbar's entity. 

- `<id>` being the namespace and ID of a bossbar
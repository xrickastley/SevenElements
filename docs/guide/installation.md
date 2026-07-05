# Installing Seven Elements

This page will guide you on installing the Seven Elements mod depending on your mod loader.

## On Fabric

When adding the mod on Fabric, simply grab the mod from <a href="https://modrinth.com/mod/seven-elements">Modrinth</a> or <a href="https://www.curseforge.com/minecraft/mc-mods/seven-elements">CurseForge</a>, install <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> and <a href="https://modrinth.com/mod/cloth-config">Cloth Config</a> if you haven't yet, <a href="https://modrinth.com/mod/modmenu">Modmenu</a> to edit the config (this is optional!) and you're all set!

## On Forge (via Sinytra Connector)

Here, we explain how to get Seven Elements on Forge via Sinytra Connector, which only works for the 1.20.1-LTS version. Sinytra Connector is the only way (as of now) to run Seven Elements on Forge. **Any other versions** of Seven Elements will **not** run on Forge and are not planned to be ported.

### Downloading the mod

Like Fabric, simply grab the mod from <a href="https://modrinth.com/mod/seven-elements">Modrinth</a> or <a href="https://www.curseforge.com/minecraft/mc-mods/seven-elements">CurseForge</a>.

**Ensure** that you grab the **LTS** version, which can be seen in the version number, i.e. "1.0.3-LTS.1". Lower versions will **not work** with Sinytra Connector and will result in a crash.

Finally, install Sinytra's <a href="https://modrinth.com/mod/forgified-fabric-api">Forgified Fabric API</a> and <a href="https://modrinth.com/mod/connector">Sinytra Connector</a>, as well as <a href="https://modrinth.com/mod/cloth-config">Cloth Config</a> (the Forge version) if you don't have it yet.

### Downloading MixinExtras Forge

Now, here comes the *somewhat* hard part.

Since Forge 1.20.1 does not have native support for [MixinExtras](https://github.com/LlamaLad7/MixinExtras) v0.5.0, you will have to download it directly and add it to your mods folder.

You can grab MixinExtras v0.5.0 from its official Maven Central repository link, which you can access through this link: https://mvnrepository.com/artifact/io.github.llamalad7/mixinextras-forge

Alternatively, you can use this link to directly download the .jar file: https://repo1.maven.org/maven2/io/github/llamalad7/mixinextras-forge/0.5.0/mixinextras-forge-0.5.0.jar

Once you've obtained the `mixinextras-forge-0.5.0.jar` file, simply add it to your mods folder and Seven Elements should be able to run.

## On NeoForge (via Sinytra Connector)

Unfortunately, Seven Elements cannot run on NeoForge *yet*. This is discussed in greater detail in [Seven Elements Issue #3](https://github.com/xrickastley/SevenElements/issues/3#issuecomment-3421916749).
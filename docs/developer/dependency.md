---
prev: false
next: false
---

# Depending on Seven Elements

This page will guide you on how to add Seven Elements as a dependency to your project.

::: warning
Since Seven Elements is a Fabric mod, this guide will only work on Fabric!

If you want your mod to work on Forge, the suggested option is to make it a Fabric mod and include [Sinytra Connector](https://modrinth.com/mod/connector) support. However, there may be other methods of achieving the same result!
:::

## Adding Seven Elements

Seven Elements doesn't have a dedicated Maven repository, so you're gonna have to use the Modrinth maven.

First, add the Modrinth maven to your `build.gradle` script inside the `repositories` block.
```groovy
repositories {
	// ...

	exclusiveContent {
		forRepository {
			maven {
				name = "Modrinth"
				url = "https://api.modrinth.com/maven"
			}
		}
		filter {
			includeGroup "maven.modrinth"
		}
	}

	// ...
}
```

We use `exclusiveContent` and `filter.includeGroup` to tell Gradle that we should only search the Modrinth maven if the provided Maven group is `maven.modrinth`. This allows Gradle to resolve dependencies faster, and is used since the `maven.modrinth` group is used for Modrinth mods only.

Next, we simply add the Maven coordinates of Seven Elements as a dependency in the `dependencies` block.

```groovy
dependencies {
	// ...

	modImplementation "maven.modrinth:seven-elements:${project.seven_elements_version}"
}
```

::: tip
It's good practice to have the mod's version as a variable in case of future updates so that you edit the `gradle.properties` file instead of finding the mod inside the `build.gradle` file and replacing the version per update you want to support. 
:::

For more information, refer to the [Fabric Loom](https://fabricmc.net/wiki/documentation:fabric_loom) documentation.

Finally, we declare the version of Seven Elements we want inside the `gradle.properties` file.

```
seven_elements_version=<version>
```

Simply replace `<version>` with the version you want.

For more information on how to use the Modrinth maven, check out their [Support article](https://support.modrinth.com/en/articles/8801191-modrinth-maven) on it.

## Adding Transitive Dependencies

Since Modrinth's maven has no support for transitive dependencies, you will need to resolve them yourself.

### MixinExtras

Seven Elements relies on a later version of MixinExtras, which Fabric has not added until Minecraft 1.21.9.

For developing on lower Minecraft versions, simply add its Maven coordinates to your `dependencies` block.

```groovy
dependencies {
    modImplementation annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.0")
}
```

### Cloth Config API

If you haven't installed Cloth Config yet, you may do so in two ways.

#### Installing as a mod

You can install an appropriate version of Cloth Config from [Modrinth](https://modrinth.com/mod/cloth-config) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config). Once you have the `.jar` file, simply add it to your `mods` folder inside the `run` folder.

#### Installing via Gradle

You can also add Cloth Config to your `build.gradle` script.

First, add shedaniel's Maven to your `repositories` block.

```groovy
repositories {
	// ...

	maven { 
		url = "https://maven.shedaniel.me/"
	}

	// ...
}
```

Next, add Cloth Config's Maven coordinates to your build script. You can also see this via [Linkie](https://linkie.shedaniel.dev/dependencies?loader=fabric).

```groovy
dependencies {
	// ...

	// This is set to modRuntimeOnly since your mod doesn't depend on/interact with Cloth Config, Seven Elements does.
	// Change the dependency configuration as needed.
	modRuntimeOnly "me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}"
}
```

Configure the Cloth Config version in your `gradle.properties` file. Alternatively, you can find the version Seven Elements uses in its [GitHub repository](https://github.com/xrickastley/SevenElements/). Simply choose the branch with the Minecraft version you intend to target and click the `gradle.properties` file.

### Cardinal Components API

If you haven't installed Cardinal Components API yet, you may do so in two ways.

#### Installing as a mod

::: warning
This method is not suggested when you want to interact with the Element system in Seven Elements.
:::

Like Cloth Config, you can install an appropriate version of Cardinal Components API from [Modrinth](https://modrinth.com/mod/cardinal-components-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cardinal-components-api). Once you have the `.jar` file, simply add it to your `mods` folder inside the `run` folder.

#### Installing via Gradle

You can also add Cardinal Components API to your `build.gradle` script.

First, add the Ladysnake Maven to your `repositories` block.

```groovy
repositories {
	// ...
`
    maven {
        name = "Ladysnake Mods"
        url = 'https://maven.ladysnake.org/releases'
    }`

	// ...
}
```

Next, add the Maven coordinates for Cardinal Components API to your build script.

```groovy
dependencies {
	// ...

	// This is set to modImplementation since your mod could possibly interact with CCA via Seven Elements.
	// Change the dependency configuration as needed.
	
	// Use this for versions above or equal to Minecraft 1.20.5
    modImplementation "org.ladysnake.cardinal-components-api:cardinal-components-base:${project.cca_version}"
    modImplementation "org.ladysnake.cardinal-components-api:cardinal-components-entity:${project.cca_version}"
	modImplementation "org.ladysnake.cardinal-components-api:cardinal-components-item:${project.cca_version}"

	// Use this for versions below Minecraft 1.20.5
    modImplementation "dev.onyxstudios.cardinal-components-api:cardinal-components-base:${project.cca_version}"
    modImplementation "dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${project.cca_version}"
	modImplementation "dev.onyxstudios.cardinal-components-api:cardinal-components-item:${project.cca_version}"
}
```

Configure the Cardinal Components API version in your `gradle.properties` file. Alternatively, you can find the version Seven Elements uses in its [GitHub repository](https://github.com/xrickastley/SevenElements/). Simply choose the branch with the Minecraft version you intend to target and click the `gradle.properties` file.
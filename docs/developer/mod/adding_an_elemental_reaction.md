# Adding an Elemental Reaction

This page will introduce you into creating your own custom Elemental Reaction.

Elemental Reactions are the core of Seven Elements, allowing elements to interact and react with one another. Much like everything else in Minecraft, they are stored in a registry.

## Creating your Elemental Reaction

First, we need to extend the abstract `ElementalReaction` class. This class is the base of all Elemental Reactions in Seven Elements, and you must use it to create your own.

For this example, we will use [Rimegrass](https://genshin-impact.fandom.com/wiki/Imaginarium_Theater/Events#Rimegrass) as an example, one of the *custom reactions* added in the [Imaginarium Theater](https://genshin-impact.fandom.com/wiki/Imaginarium_Theater). We won't be implementing this fully, only enough for you to understand the process behind creating a custom reaction.

Similarly to blocks and items, Elemental Reactions take an `ElementalReaction.Settings` in their constructor, which specify properties of the reaction, such as its [Aura Element](../../guide/elements/elemental_gauge_theory.md#elemental-auras-and-the-aura-tax) and [Triggering Element](../../guide/elements/elemental_gauge_theory.md#triggering-elements-and-elemental-reactions), priority, etc.

```java
public final class RimegrassElementalReaction extends ElementalReaction {
	public RimegrassElementalReaction() {
		super(
			new ElementalReaction.Settings("Rimegrass", Identifier.of("tutorial", "rimegrass"), TextHelper.reaction("reaction.your-mod.rimegrass", "#c6f7b4"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.CRYO, 5)
				.setTriggeringElement(Element.DENDRO, 5)
				.reversable(true)
		);
	}
}
```	

This is quite a lot to digest, so let's go over it one-by-one:

- We create a new instance of `ElementalReaction.Settings`, which requires three parameters: `String name`, `Identifier id` and `Text text`.
	- `name` is the name we are giving this Elemental Reaction.
	- `id` is the unique `Identifier` of the Elemental Reaction.
	- `text` is an instance of `Text` describing how the reaction is displayed.

**Seven Elements** has a helper function for `Text` instances specifically for Elemental Reactions: `TextHelper.reaction`. This takes in a `String translationKey` and a `String color` or a `Color color`. We'll work on this [later](#adding-translations).

- We set the [Reaction Coefficient](../../guide/elements/elemental_gauge_theory.md#triggering-elements-and-elemental-reactions) to `1.0`.
- We set the Aura Element to `CRYO` and give it a priority of `5`
- We set the Triggering Element to `DENDRO` and give it a priority of `5`
- We set `reversable` to `true`.

In Seven Elements, priority is sorted in natural order, in order of decreasing priority, i.e. least (most priority) to greatest (least priority).

As for our assigned priority values, `5` is the priority of the **Rimegrass** reaction over the other reactions when `CRYO` or `DENDRO` is the triggering element.

We only assigned a priority to `CRYO` since the reaction is marked as **reversable**. If the reaction is **not** reversable, we do not need to assign a priority to it.

It is also good practice to modify your custom Elemental Reactions as `final`, unless you intend for other reactions to extend your custom reaction.

## Adding Translations

To add a translation, you must create a translation key in your mod's translation file: `assets/mod-id/lang/en_us.json`.

Minecraft will use this translation when the reaction is displayed as text.

Luckily for us, we already used the helper function: `TextHelper.reaction` in our `Settings` code. All we need to do now is create the translation key.

```json
{
	"reaction.my-mod.rimegrass": "Rimegrass"
}
```

Note that it isn't required to follow the format of `reaction.mod.name`. In fact, you could use **any** translation key, just ensure that the key provided in `TextHelper.reaction` is the same as the one in your translation file.

## Implementing the callback action

As you've probably seen, our custom reaction still has an error. That's because we haven't implemented the `ElementalReaction#onReaction` method.

Back to the `RimegrassElementalReaction` file, implement the `onReaction` method.

```java
public final class RimegrassElementalReaction extends ElementalReaction {
	public RimegrassElementalReaction() {
		super(
			new ElementalReaction.Settings("Rimegrass", Identifier.of("tutorial", "rimegrass"), TextHelper.reaction("reaction.your-mod.rimegrass", "#c6f7b4"))
				.setReactionCoefficient(1.0)
				.setAuraElement(Element.CRYO, 5)
				.setTriggeringElement(Element.DENDRO, 5)
				.reversable(true)
		);
	}

	
	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement,
		ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		// ...
	}
}
```

To briefly explain the parameters of this method:

- `LivingEntity entity` is the entity the reaction was triggered on.
- `ElementalApplication auraElement` is the elemental application of the Aura Element.
	- Note that if your Elemental Reaction is **reversable**, this can be the Triggering Element!
- `ElementalApplication triggeringElement` is the elemental application of the Triggering Element.
	- Note that if your Elemental Reaction is **reversable**, this can be the Aura Element!
- `double reducedGauge` is the reduced amount of [gauge units](../../guide/elements/elemental_gauge_theory.md#elemental-auras-and-the-aura-tax) from both elemental applications.
- `@Nullable LivingEntity origin` is the living entity that triggered the reaction
	- This is annotated as `@Nullable` since reactions triggered by [Natural Element Sources](../../guide/elements/elemental_combat.md#natural-element-sources) have no `origin`.

Here, we add the logic of the elemental reaction: what happens when it is triggered.

Of course, if there is no logic, it may be left blank.

## Using predefined abstract Elemental Reactions

There may be instances where you don't want to add another Transformative Elemental Reaction. You may inherit the predefined abstract reaction classes to add reactions of that type.

### Amplifying Elemental Reactions

For custom [Amplifying Elemental Reactions](../../guide/elements/elemental_reactions.md#amplifying-reactions), simply extend `AmplifyingElementalReaction`.

This one requires an `amplifier` argument with the `ElementalReaction.Settings` argument.

Additionally, a blank implementation has been provided for `ElementalReaction#onReaction`, so you don't need to override it in your own custom reaction. However, if your reaction needs it, you may override it.

```java
public final class ThunderstrikeElementalReaction extends AmplifyingElementalReaction {
	public ThunderstrikeElementalReaction() {
		super(
			new ElementalReaction.Settings("Thunderstrike", Identifier.of("tutorial", "thunderstrike"), TextHelper.reaction("reaction.your-mod.thunderstrike", "#eab4d4"))
				.setReactionCoefficient(2)
				.setAuraElement(Element.PYRO)
				.setTriggeringElement(Element.ELECTRO, 2),
			2.0
		);
	}
}
```

### Additive Elemental Reactions

For custom [Additive Elemental Reactions](../../guide/elements/elemental_reactions.md#additive-reactions), simply extend `AdditiveElementalReaction`.

This one requires an `amplifier` argument with the `ElementalReaction.Settings` argument.

Like [Amplifying Elemental Reactions](#amplifying-elemental-reactions), a blank implementation has been provided for `ElementalReaction#onReaction`, so you don't need to override it in your own custom reaction. However, if your reaction needs it, you may override it.

```java
public final class HydrolyzeElementalReaction extends AdditiveElementalReaction {
	public HydrolyzeElementalReaction() {
		super(
			new ElementalReaction.Settings("Hydrolyze", Identifier.of("tutorial", "hydrolyze"), TextHelper.reaction("reaction.your-mod.hydrolyze", "#acc4ff"))
				.setReactionCoefficient(2)
				.setAuraElement(Element.HYDRO)
				.setTriggeringElement(Element.ELECTRO, 4),
			1.10
		);
	}
}
```

## Advanced Elemental Reaction Triggers

There may be instances where you want to modify *how* an Elemental Reaction is triggered. This is easily done by overriding the `ElementalReaction#trigger` method.

```java
public final class RimegrassElementalReaction extends ElementalReaction {
	// ...

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		// custom logic here
	}
}
```

This is done by both **Electro-Charged** and the derivatives of the **Bloom** reaction: **Hyperbloom** and **Burgeon**.

When modifying the trigger condition, ensure that you call the `ElementalReaction#onTrigger` method, which handles reaction display and sound, calling the `onReaction` method and Reaction-related events.

If you cannot call `ElementalReaction#onTrigger` due to custom mechanics, like the derivatives of the **Bloom** reaction, ensure that you call the necessary methods called by `ElementalReaction#onTrigger` in your `ElementalReaction#trigger` method, as other mods may rely on it being triggered to properly function.

## Registering your Elemental Reaction

To register your Elemental Reaction, simply call `Registry.register` with the proper arguments in your mod initializer.

```java
public class MyMod implements ModInitializer {
	public static final String MOD_ID = "tutorial";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ElementalReaction RIMEGRASS = new RimegrassElementalReaction();

	@Override
	public void onInitialize() {
		Registry.register(SevenElementsRegistries.ELEMENTAL_REACTION, RIMEGRASS.getId(), RIMEGRASS);
	}
```
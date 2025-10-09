---
prev: false
---

# Disabling Entity Elements

Elements are only applied to inheritors of `LivingEntity`, as only living entities are able to take damage. As such, they are also the only entities who can have the Elements applied to them.

However, there may be instances where you extend `LivingEntity` but do not want them to have Elements applied on them. This can be easily done by calling the `ElementComponent#denyElementsFor` method and passing the class of the entity you want to disable Elements for.

This is done by both `DendroCoreEntity` and `CrystallizeShardEntity`:

```java{5}
public final class DendroCoreEntity extends SevenElementsEntity { 
	// ...

	static {
		ElementComponent.denyElementsFor(DendroCoreEntity.class);
	}

	// ...
}
```

```java{5}
public final class CrystallizeShardEntity extends SevenElementsEntity { 
	// ...

	static {
		ElementComponent.denyElementsFor(CrystallizeShardEntity.class);
	}

	// ...
}
```

Technically, you can pass `SevenElementsEntity.class` instead. However, that blocks all inheritors of `SevenElementsEntity` from having Elements applied to them. As such, always consider the possible **inheritors** of the entity class you are blocking Elements for.

If you decide to block Elements for an abstract entity class, decide in the future that only a specific inheritor should have elements, you need to unblock Elements for said abstract entity class and add calls to `ElementComponent#denyElementsFor` for **all** inheritors of the abstract entity class except the specific inheritor.
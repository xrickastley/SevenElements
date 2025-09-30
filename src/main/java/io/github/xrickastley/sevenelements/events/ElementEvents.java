package io.github.xrickastley.sevenelements.events;

import io.github.xrickastley.sevenelements.annotation.ExpectedEnvironment;
import io.github.xrickastley.sevenelements.element.Element;
import io.github.xrickastley.sevenelements.element.ElementalApplication;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@ExpectedEnvironment(EnvType.SERVER)
public final class ElementEvents {
	/**
	 * Called when an element is applied. <br> <br>
	 *
	 * An element is "applied" when its corresponding Elemental Application is first applied to the
	 * entity.
	 */
	public static final Event<ElementApplied> APPLIED = EventFactory.createArrayBacked(ElementApplied.class,
		listeners -> (element, application) -> {
			for (final ElementApplied listener : listeners) listener.onElementApplied(element, application);
		}
	);

	/**
	 * Called when an element is reapplied. Not to be confused with {@link ElementEvents#REFRESHED}. <br> <br>
	 *
	 * An element is "reapplied" when its corresponding Elemental Application is reapplied through
	 * the {@link io.github.xrickastley.sevenelements.element.ElementalApplication#reapply(Element, double) ElementalApplication#reapply()}
	 * method.
	 */
	public static final Event<ElementReapplied> REAPPLIED = EventFactory.createArrayBacked(ElementReapplied.class,
		listeners -> (element, result) -> {
			for (final ElementReapplied listener : listeners) listener.onElementReapplied(element, result);
		}
	);

	/**
	 * Called when an element is refreshed. Not to be confused with {@link ElementEvents#REAPPLIED}. <br> <br>
	 *
	 * An element is "refreshed" when its corresponding Elemental Application is replaced with a
	 * new Elemental Application.
	 */
	public static final Event<ElementRefreshed> REFRESHED = EventFactory.createArrayBacked(ElementRefreshed.class,
		listeners -> (element, cur, prev) -> {
			for (final ElementRefreshed listener : listeners) listener.onElementRefreshed(element, cur, prev);
		}
	);

	/**
	 * Called when an element is removed. <br> <br>
	 *
	 * An element is "removed" when its corresponding Elemental Application expires or is removed.
	 */
	public static final Event<ElementRemoved> REMOVED = EventFactory.createArrayBacked(ElementRemoved.class,
		listeners -> (element, application) -> {
			for (final ElementRemoved listener : listeners) listener.onElementRemoved(element, application);
		}
	);

	@FunctionalInterface
	public interface ElementApplied {
		void onElementApplied(Element element, ElementalApplication application);
	}

	@FunctionalInterface
	public interface ElementReapplied {
		/**
		 * Event signature for {@link ElementEvents#REAPPLIED}
		 *
		 * @param element The reapplied element.
		 * @param result The resulting elemental application <b>after</b> reapplication.
		 */
		void onElementReapplied(Element element, ElementalApplication result);
	}

	@FunctionalInterface
	public interface ElementRefreshed {
		void onElementRefreshed(Element element, ElementalApplication current, ElementalApplication previous);
	}

	@FunctionalInterface
	public interface ElementRemoved {
		void onElementRemoved(Element element, ElementalApplication application);
	}
}

---
next: false
---

# Events

Seven Elements provides Events using the Fabric API that your mod can listen to.

For more information on Events, you may click [here](https://docs.fabricmc.net/develop/events).

## List of events

- `ElementEvents` - A class for all events relating to the Elements.
	- `APPLIED` - Callback for an element being **applied**. This is called when **no previous** elemental application exists.
	- `REAPPLIED`- Callback for an element being **reapplied**. This is called when an **existing previous** elemental application is reapplied.
	- `REFRESHED`- Callback for an element being **refreshed**. This is called when an **existing previous** elemental application is refreshed with a new one.
	- `REMOVED` - Callback for an element being **removed**. This is called when the elemental application is removed, or has expired.
- `ReactionTriggered` - Callback for an Elemental Reaction being triggered. Note that multiple reactions may be triggered in the same tick, meaning that this event can be called more than once per tick.


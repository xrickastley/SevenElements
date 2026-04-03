package io.github.xrickastley.sevenelements.registry.dynamic;

import io.netty.util.internal.logging.MessageFormatter;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class UnmodifiableEntryOverwriteException extends RuntimeException {
	public UnmodifiableEntryOverwriteException(Resource resource, Identifier path, Identifier resourceId) {
		super(
			MessageFormatter.arrayFormat("The data pack (\"{}\") with file at path ({}/{}) attempted to overwrite the preloaded entry {}, ignoring!", new Object[] { resource.sourcePackId(), path.getNamespace(), path.getPath(), resourceId }).toString()
		);
	}
}

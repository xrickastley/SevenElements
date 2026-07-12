package io.github.xrickastley.sevenelements.interfaces;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.sevenelements.util.AbstractDataAttachments;

public interface DataAttachmentAccess {
	default @Nullable AbstractDataAttachments.ReadView sevenelements$getAttachments() {
		return null;
	}
}

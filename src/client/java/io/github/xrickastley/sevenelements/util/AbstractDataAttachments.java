package io.github.xrickastley.sevenelements.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;

public final class AbstractDataAttachments {
	private static final Map<Object, AbstractDataAttachments> DATA_ATTACHMENTS = new HashMap<>();

	private final Map<Identifier, Object> attachments = new HashMap<>();

	private AbstractDataAttachments() {}

	public static AbstractDataAttachments getAttachments(Object object) {
		return AbstractDataAttachments.DATA_ATTACHMENTS.computeIfAbsent(object, p -> new AbstractDataAttachments());
	}

	@ApiStatus.Internal
	public static @Nullable AbstractDataAttachments.ReadView getAttachmentsReadView(Object object) {
		return Optional.ofNullable(AbstractDataAttachments.DATA_ATTACHMENTS.remove(object))
			.map(partAttachment -> partAttachment.new ReadView())
			.orElse(null);
	}

	public void addAttachment(Identifier id, Object value) {
		this.attachments.put(id, value);
	}

	public final class ReadView {
		private ReadView() {};

		@SuppressWarnings("unchecked")
		public <T> T getAttachment(Identifier id) {
			return (T) AbstractDataAttachments.this.attachments.get(id);
		}

		public <T> T getAttachment(Identifier id, Class<T> type) {
			return type.cast(AbstractDataAttachments.this.attachments.get(id));
		}
	}
}

package ghast;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
public class EventContext implements Listener {

	private static final BooleanSupplier EMPTY_FILTER = () -> true;

	private final Map<Class<? extends Event>, Consumer<?>> eventMap = new HashMap<>();
	private BooleanSupplier filter = EMPTY_FILTER;

	public EventContext filter(BooleanSupplier filter) {
		this.filter = (filter != null ? filter : EMPTY_FILTER);
		return this;
	}

	public <T extends Event> EventContext onEvent(Class<T> eventType, EventPriority eventPriority, Consumer<T> consumer) {
		if (consumer == null) {
			eventMap.remove(eventType);
		} else {
			eventMap.put(eventType, consumer);
			Bukkit.getPluginManager().registerEvent(eventType, this, eventPriority,
					this::eventExecute, GhastTools.getPlugin());
		}
		return this;
	}

	public <T extends Event> EventContext onEvent(Class<T> eventType, Consumer<T> consumer) {
		return onEvent(eventType, EventPriority.NORMAL, consumer);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void eventExecute(Listener listener, Event event) {
		Consumer consumer = eventMap.get(event.getClass());
		if (consumer != null && filter.getAsBoolean()) {
			consumer.accept(event);
		}
	}

	public static EventContext create() {
		return new EventContext();
	}
}

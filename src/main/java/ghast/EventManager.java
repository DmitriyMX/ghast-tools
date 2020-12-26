package ghast;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@UtilityClass
@SuppressWarnings("unused")
public class EventManager {

	public Builder createContext(Plugin plugin) {
		return new Builder(plugin);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder {

		private static final BooleanSupplier EMPTY_FILTER = () -> true;

		private final EventContext eventContext = new EventContext();
		private final Plugin plugin;

		public Builder filter(BooleanSupplier filter) {
			eventContext.setFilter(filter != null ? filter : EMPTY_FILTER);
			return this;
		}

		public <T extends Event> Builder onEvent(Class<T> eventType, EventPriority eventPriority, Consumer<T> consumer) {
			eventContext.getEventMap().put(eventType, consumer);
			Bukkit.getPluginManager().registerEvent(eventType, eventContext, eventPriority,
					eventContext::eventExecute, plugin);
			return this;
		}

		public <T extends Event> Builder onEvent(Class<T> eventType, Consumer<T> consumer) {
			return onEvent(eventType, EventPriority.NORMAL, consumer);
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private static class EventContext implements Listener {

		private final Map<Class<? extends Event>, Consumer<?>> eventMap = new HashMap<>();
		private BooleanSupplier filter;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void eventExecute(Listener listener, Event event) {
			Consumer consumer = eventMap.get(event.getClass());
			if (consumer != null && filter.getAsBoolean()) {
				consumer.accept(event);
			}
		}
	}
}

package ghast;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
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
    private static final Consumer<Cancellable> CANCEL_EVENT = event -> event.setCancelled(true);

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
            bukkitRegisterEvent(eventType, eventPriority);
        }
        return this;
    }

    public <T extends Event> EventContext onEvent(Class<T> eventType, Consumer<T> consumer) {
        return onEvent(eventType, EventPriority.NORMAL, consumer);
    }

    public <T extends Event & Cancellable> EventContext cancelEvent(Class<T> eventType, EventPriority eventPriority) {
        eventMap.put(eventType, CANCEL_EVENT);
        bukkitRegisterEvent(eventType, eventPriority);
        return this;
    }

    public <T extends Event & Cancellable> EventContext cancelEvent(Class<T> eventType) {
        return cancelEvent(eventType, EventPriority.NORMAL);
    }

    private void bukkitRegisterEvent(Class<? extends Event> eventType, EventPriority eventPriority) {
        Bukkit.getPluginManager().registerEvent(eventType, this, eventPriority,
                this::eventExecute, GhastTools.getPlugin());
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

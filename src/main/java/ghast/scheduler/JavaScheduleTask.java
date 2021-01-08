package ghast.scheduler;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class JavaScheduleTask implements ScheduleTask {

    private final Supplier<Future<?>> generator;
    private Future<?> future;

    @Override
    public void start() {
        if (future == null || future.isDone()) {
            future = generator.get();
        }
    }

    @Override
    public boolean isCanceled() {
        return future == null || future.isCancelled();
    }

    @Override
    public void cancel() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }
}

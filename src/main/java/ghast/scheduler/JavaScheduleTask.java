package ghast.scheduler;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;

@RequiredArgsConstructor
public class JavaScheduleTask implements ScheduleTask {

	private final Future<?> future;

	@Override
	public boolean isCanceled() {
		return future.isCancelled();
	}

	@Override
	public void cancel() {
		future.cancel(true);
	}
}

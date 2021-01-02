package ghast.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class BukkitScheduleTask implements ScheduleTask {

	private final Supplier<BukkitTask> generator;
	private BukkitTask bukkitTask;

	@Override
	public void start() {
		if (isCanceled()) {
			bukkitTask = generator.get();
		}
	}

	@Override
	public boolean isCanceled() {
		return bukkitTask == null || bukkitTask.isCancelled();
	}

	@Override
	public void cancel() {
		if (bukkitTask != null) {
			bukkitTask.cancel();
			bukkitTask = null;
		}
	}
}

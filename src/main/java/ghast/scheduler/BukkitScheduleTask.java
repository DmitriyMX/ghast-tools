package ghast.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class BukkitScheduleTask implements ScheduleTask {

	private final BukkitTask bukkitTask;

	@Override
	public boolean isCanceled() {
		return bukkitTask.isCancelled();
	}

	@Override
	public void cancel() {
		bukkitTask.cancel();
	}
}

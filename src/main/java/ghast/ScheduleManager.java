package ghast;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.*;

@UtilityClass
@SuppressWarnings("unused")
public class ScheduleManager {

	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
			.setNameFormat("ScheduleManager-Thread-%d")
			.setDaemon(true)
			.build();

	public Builder createTask() {
		return new Builder();
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder {

		private static final long MS_PER_ONE_TICK = 1000L/*one second by ms*/ / 20L/*tick per second*/;
		private boolean useBukkitScheduler = false;
		private Long afterMs;
		private Long everyMs;

		public Builder useBukkitScheduler() {
			this.useBukkitScheduler = true;
			return this;
		}

		public Builder after(long value, TimeUnit unit) {
			this.afterMs = unit.toMillis(value);
			return this;
		}

		public Builder every(long value, TimeUnit unit) {
			this.everyMs = unit.toMillis(value);
			return this;
		}

		public void execute(Runnable runnable) {
			if (useBukkitScheduler) {
				createBukkitSchedule(runnable);
			} else {
				createSchedule(runnable);
			}
		}

		private void createBukkitSchedule(Runnable runnable) {
			BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

			if (this.afterMs == null && this.everyMs == null) {
				bukkitScheduler.runTask(GhastTools.getPlugin(), runnable);
			} else if (this.everyMs != null) {
				long everyTicks = this.everyMs / MS_PER_ONE_TICK;
				long afterTicks = this.afterMs != null ? this.afterMs / MS_PER_ONE_TICK : 0;
				bukkitScheduler.runTaskTimer(GhastTools.getPlugin(), runnable, afterTicks, everyTicks);
			} else {
				long ticks = this.afterMs / MS_PER_ONE_TICK;
				bukkitScheduler.runTaskLater(GhastTools.getPlugin(), runnable, ticks);
			}
		}

		private void createSchedule(Runnable runnable) {
			ExecutorService executorService;

			if (this.afterMs == null && this.everyMs == null) {
				executorService = Executors.newSingleThreadExecutor(THREAD_FACTORY);
				executorService.execute(runnable);
			} else if (this.everyMs != null) {
				ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				scheduledExecutorService.scheduleAtFixedRate(runnable,
						this.afterMs != null ? this.afterMs : 0,
						everyMs, TimeUnit.MILLISECONDS);
				executorService = scheduledExecutorService;
			} else {
				ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				scheduledExecutorService.schedule(runnable, afterMs, TimeUnit.MILLISECONDS);
				executorService = scheduledExecutorService;
			}

			executorService.shutdown();
		}
	}
}

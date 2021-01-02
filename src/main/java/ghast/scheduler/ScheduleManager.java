package ghast.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ghast.GhastTools;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

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

		public ScheduleTask execute(Runnable runnable) {
			if (useBukkitScheduler) {
				return createBukkitSchedule(runnable);
			} else {
				return createSchedule(runnable);
			}
		}

		private ScheduleTask createBukkitSchedule(Runnable runnable) {
			BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
			BukkitScheduleTask resultTask;

			if (this.afterMs == null && this.everyMs == null) {
				BukkitTask bukkitTask = bukkitScheduler.runTask(GhastTools.getPlugin(), runnable);
				resultTask = new BukkitScheduleTask(bukkitTask);
			} else if (this.everyMs != null) {
				long everyTicks = this.everyMs / MS_PER_ONE_TICK;
				long afterTicks = this.afterMs != null ? this.afterMs / MS_PER_ONE_TICK : 0;
				BukkitTask bukkitTask = bukkitScheduler.runTaskTimer(GhastTools.getPlugin(), runnable, afterTicks,
						everyTicks);
				resultTask = new BukkitScheduleTask(bukkitTask);
			} else {
				long ticks = this.afterMs / MS_PER_ONE_TICK;
				BukkitTask bukkitTask = bukkitScheduler.runTaskLater(GhastTools.getPlugin(), runnable, ticks);
				resultTask = new BukkitScheduleTask(bukkitTask);
			}

			return resultTask;
		}

		private ScheduleTask createSchedule(Runnable runnable) {
			ExecutorService executorService;
			JavaScheduleTask resultTask;

			if (this.afterMs == null && this.everyMs == null) {
				executorService = Executors.newSingleThreadExecutor(THREAD_FACTORY);
				Future<?> future = executorService.submit(runnable);
				resultTask = new JavaScheduleTask(future);
			} else if (this.everyMs != null) {
				ScheduledExecutorService scheduledExecutorService
						= Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable,
						this.afterMs != null ? this.afterMs : 0,
						everyMs, TimeUnit.MILLISECONDS);

				resultTask = new JavaScheduleTask(scheduledFuture);
				executorService = scheduledExecutorService;
			} else {
				ScheduledExecutorService scheduledExecutorService
						= Executors.newScheduledThreadPool(1, THREAD_FACTORY);
				ScheduledFuture<?> schedule = scheduledExecutorService
						.schedule(runnable, afterMs, TimeUnit.MILLISECONDS);

				resultTask = new JavaScheduleTask(schedule);
				executorService = scheduledExecutorService;
			}

			executorService.shutdown();
			return resultTask;
		}
	}
}

package ghast.scheduler;

public interface ScheduleTask {

	boolean isCanceled();

	void cancel();
}

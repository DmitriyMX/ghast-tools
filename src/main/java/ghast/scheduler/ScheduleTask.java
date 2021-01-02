package ghast.scheduler;

public interface ScheduleTask {

	void start();

	boolean isCanceled();

	void cancel();
}

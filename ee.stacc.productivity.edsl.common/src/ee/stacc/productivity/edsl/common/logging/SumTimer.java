package ee.stacc.productivity.edsl.common.logging;

public class SumTimer {
	private String name;
	private long timeSpent = 0;
	private long startTime = 0;
	
	public SumTimer(String name) {
		this.name = name;
	}
	
	public void start() {
		if (startTime != 0) {
			throw new IllegalStateException();
		}
		startTime = System.nanoTime();
	}
	
	public void stop() {
		if (startTime == 0) {
			throw new IllegalStateException();
		}
		timeSpent += System.nanoTime() - startTime;
		startTime = 0;
	}
	
	public void reset() {
		timeSpent = 0;
		startTime = 0;
	}
	
	@Override
	public String toString() {
		long t = timeSpent;
		if (startTime != 0) {
			t += System.nanoTime() - startTime;
		}
		return "TIME SPENT (" + name + ") = " + (t / 1000000000.0);
	}
}

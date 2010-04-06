package ee.stacc.productivity.edsl.common.logging;

public class Timer {

	private long time;
	private String message;
	
	public Timer() {
	}

	public Timer(String message) {
		start(message);
	}
	
	public final void start(String message) {
		this.message = message;
		this.time = System.nanoTime();
	}
	
	public void printTime() {
		long t = System.nanoTime() - time;
		System.out.println(message + ": " + (t / 1000000000.0));
	}
	
	public void printTimeAndStart(String message) {
		printTime();
		start(message);
	}
}
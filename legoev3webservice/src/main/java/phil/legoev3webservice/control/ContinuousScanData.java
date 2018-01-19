package phil.legoev3webservice.control;

public class ContinuousScanData {
	public ContinuousScanData(int[] is, int[] is2) {
		this.steps = is;
		this.irSensor = is2;
	}

	public final int[] steps;
	public final int[] irSensor;
}

package phil.legoev3webservice.control;

public class RotateResult {
	public ContinuousScanData scanData;
	public int ticksRotated;

	public RotateResult(ContinuousScanData scanData, int ticksRotated) {
		super();
		this.scanData = scanData;
		this.ticksRotated = ticksRotated;
	}

}

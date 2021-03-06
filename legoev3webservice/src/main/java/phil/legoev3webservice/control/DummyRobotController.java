package phil.legoev3webservice.control;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.server.Session;

public class DummyRobotController implements RobotController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);

	public RotateResult rotate(int iclicks) {
		logger.info("Got command to rotate : " + iclicks);
		return new RotateResult(new ContinuousScanData(new int[10], new int[10]), iclicks);
	}

	public AdvanceResults advanceWithoutCollision(int clicks) {
		logger.info("Got command to advance : " + clicks);
		return new AdvanceResults(clicks,clicks,  0, 0, false, false);
	}

	public int reverse(int clicks) {
		logger.info("Got command to reverse : " + clicks);
		return clicks;
	}

	public ScanData fullScannerSweep(int scanSize, int scanStep) {
		logger.info("Got command to do a full scanner sweep: " + scanSize + ", step " + scanStep);
		return new ScanData(new int[scanSize], new int[scanSize], new int[scanSize], new int[scanSize]);
	}

	public void blockingSensorArrayMove(int target) {
		logger.info("Got command to do a blocking sensor array move : " + target);
	}

	@Override
	public ContinuousScanData continuousScannerSweep(int scanSteps) {
		return new ContinuousScanData(new int[scanSteps], new int[scanSteps]);
	}

}

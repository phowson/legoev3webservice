package phil.legoev3webservice.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class StateUpdatingRobotController implements RobotController {
	private static final Logger logger = LoggerFactory.getLogger(StateUpdatingRobotController.class);
	private final RobotController controller;
	private final RobotState state;
	private final ScanDataFilter filter = new ScanDataFilter();
	private EnvironmentMap map;

	public StateUpdatingRobotController(RobotController controller, RobotState state, EnvironmentMap map) {
		this.controller = controller;
		this.state = state;
		this.map = map;
	}

	public EnvironmentMap getMap() {
		return map;
	}

	public RobotState getState() {
		return state;
	}

	public RobotController getController() {
		return controller;
	}

	public synchronized int rotate(int iclicks) {
		int r = controller.rotate(iclicks);

		state.rotate(r * RobotCalibration.ROTATE_DEGREES_PER_CLICK);

		return r;
	}

	public synchronized AdvanceResults advanceWithoutCollision(int clicks) {
		AdvanceResults res = controller.advanceWithoutCollision(clicks);

		state.advance(res.getDistance() * RobotCalibration.MOVE_CM_PER_CLICK);
		double rotate = res.getRotation() * RobotCalibration.ROTATE_DEGREES_PER_CLICK;
		logger.info("Rotated by : "+ rotate +" degrees");
		state.rotate(rotate);

		if (res.pressed) {
			map.hitHardObsticle(state, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		}

		return res;
	}

	public synchronized int reverse(int clicks) {
		int c = controller.reverse(clicks);
		state.advance(-c * RobotCalibration.MOVE_CM_PER_CLICK);
		return c;
	}

	public synchronized ScanData fullScannerSweep(int scanSize, int scanStep) throws InterruptedException {
		ScanData sweep = controller.fullScannerSweep(scanSize, scanStep);
		logger.info("Sweep : " + sweep);
		map.apply(state, filter.filter(sweep));
		return sweep;
	}

	public synchronized void blockingSensorArrayMove(int target) {
		controller.blockingSensorArrayMove(target);
	}

}

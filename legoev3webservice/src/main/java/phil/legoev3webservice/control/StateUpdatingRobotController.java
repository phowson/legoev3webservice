package phil.legoev3webservice.control;

import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class StateUpdatingRobotController implements RobotController {
	private final RobotController controller;
	private final RobotState state;
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

	public int rotate(int iclicks) {
		int r = controller.rotate(iclicks);

		state.rotate(r * RobotCalibration.ROTATE_DEGREES_PER_CLICK);

		return r;
	}

	public AdvanceResults advanceWithoutCollision(int clicks) {
		AdvanceResults res = controller.advanceWithoutCollision(clicks);

		state.advance(res.clicksAdvanced * RobotCalibration.MOVE_CM_PER_CLICK);

		if (res.pressed) {
			map.hitHardObsticle(state, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		}

		return res;
	}

	public int reverse(int clicks) {
		int c = controller.reverse(clicks);
		state.advance(-c * RobotCalibration.MOVE_CM_PER_CLICK);
		return c;
	}

	public ScanData fullScannerSweep(int scanSize, int scanStep) throws InterruptedException {
		return controller.fullScannerSweep(scanSize, scanStep);
	}

	public void blockingSensorArrayMove(int target) {
		controller.blockingSensorArrayMove(target);
	}

}

package phil.legoev3webservice.control;

import java.awt.Robot;

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

	public synchronized RotateResult rotate(int iclicks) {
		RotateResult results = controller.rotate(iclicks);

		map.apply(state, filter.filter(results), false);

		int r = results.ticksRotated;
		int sn = 1;
		if (iclicks < 0) {
			sn = -1;
		}
		state.rotate(r * RobotCalibration.ROTATE_DEGREES_PER_CLICK * sn);
		if (r < iclicks - 10) {
			map.hitHardObsticle(state, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		}

		return results;
	}

	public synchronized AdvanceResults advanceWithoutCollision(int clicks) {
		AdvanceResults res = controller.advanceWithoutCollision(clicks);

		double x = state.x_CM;
		double y = state.y_CM;

		double vx = Math.cos(state.heading_DEG * Math.PI / 180.0);
		double vy = Math.sin(state.heading_DEG * Math.PI / 180.0);

		double dcm = res.getDistance() * RobotCalibration.MOVE_CM_PER_CLICK;

		for (int i = 0; i < dcm; ++i) {
			map.fillVisited((int) x, (int) y, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
			x += vx;
			y += vy;
		}

		state.advance(dcm);
		double rotate = res.getRotation() * RobotCalibration.ROTATE_DEGREES_PER_CLICK;
		logger.info("Rotated by : " + rotate + " degrees");

		logger.info("res.endProximity = " + res.endProximity / RobotCalibration.SENSOR_POINTS_PER_CM + " cm");
		logger.info("res.startProximity = " + res.startProximity / RobotCalibration.SENSOR_POINTS_PER_CM + " cm");
		logger.info("Ultrasound says we moved : "
				+ (res.endProximity - res.startProximity) / RobotCalibration.SENSOR_POINTS_PER_CM);

		state.rotate(rotate);

		 map.applySingleSensorReading(state, res.endProximity /
		 RobotCalibration.SENSOR_POINTS_PER_CM);

		if (res.pressed || res.edgeSensor) {
			map.hitHardObsticle(state, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		}

		return res;
	}

	public synchronized int reverse(int clicks) {
		int c = controller.reverse(clicks);
		state.advance(-c * RobotCalibration.MOVE_CM_PER_CLICK);
		return c;
	}

	public synchronized void blockingSensorArrayMove(int target) {
		controller.blockingSensorArrayMove(target);
	}

	@Override
	public ContinuousScanData continuousScannerSweep(int scanSteps) {
		ContinuousScanData data = controller.continuousScannerSweep(scanSteps);
		map.apply(state, filter.filter(data), true);
		map.fillVisited((int) state.x_CM, (int) state.y_CM, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		return data;
	}

}

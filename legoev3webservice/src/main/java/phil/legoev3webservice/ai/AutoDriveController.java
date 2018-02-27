package phil.legoev3webservice.ai;

import java.awt.Point;
import java.awt.Robot;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.RotateResult;
import phil.legoev3webservice.control.StateUpdatingRobotController;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class AutoDriveController {
	private static final Logger logger = LoggerFactory.getLogger(AutoDriveController.class);
	private static final int COLLISION_REVERSE_CLICKS = (int) (3 * RobotCalibration.MOVE_CLICKS_PER_CM);
	private final AStarAlgorithm aStarAlgorithm;
	private final RobotController robotController;
	private final RobotState robotState;
	private final LinearisePath linearisePath;
	private final EnvironmentMap environmentMap;

	public AutoDriveController(AStarAlgorithm aStarAlgorithm, RobotController robotController, RobotState robotState,
			LinearisePath linearisePath, EnvironmentMap map) {
		super();
		this.aStarAlgorithm = aStarAlgorithm;
		this.robotController = robotController;
		this.robotState = robotState;
		this.linearisePath = linearisePath;
		this.environmentMap = map;
	}

	public LinearisePath getLinearisePath() {
		return linearisePath;
	}

	public AStarAlgorithm getaStarAlgorithm() {
		return aStarAlgorithm;
	}

	public void initialise() {
		robotController.rotate((int) (360 * RobotCalibration.ROTATE_CLICKS_PER_DEGREE));
	}

	public List<Point> driveOneStep(PathListener listener) {
		List<Point> path = aStarAlgorithm.getAStarPath();
		if (path.isEmpty()) {
			// reached goal.
			return path;
		}

		listener.onNewPath(path, aStarAlgorithm.getTargetX(), aStarAlgorithm.getTargetY());
		RobotMoveCommand lc = linearisePath.getNextLinearCommand(path);
		double d = (lc.heading - robotState.heading_DEG);
		if (d > 180) {
			d = 180 - d;
		}
		if (d < -180) {
			d = d + 360;
		}
		int requested = (int) Math.round(d * RobotCalibration.ROTATE_CLICKS_PER_DEGREE);
		boolean rescanRequired = false;
		if (Math.abs(requested) > 0) {
			RotateResult rotateResults = robotController.rotate(requested);
			int r = rotateResults.ticksRotated;
			if (requested - r >= 10) {
				logger.info("Rotate didn't seem to work");
				listener.stateChanged();
				robotController.reverse(COLLISION_REVERSE_CLICKS);
				listener.stateChanged();
				rescanRequired = true;
			} else {
				listener.stateChanged();

				AdvanceResults res = robotController
						.advanceWithoutCollision((int) Math.round(lc.distance * RobotCalibration.MOVE_CLICKS_PER_CM));
				listener.stateChanged();
				rescanRequired = handleCollision(listener, rescanRequired, res);
			}
		} else {

			AdvanceResults res = robotController
					.advanceWithoutCollision((int) Math.round(lc.distance * RobotCalibration.MOVE_CLICKS_PER_CM));
			listener.stateChanged();
			rescanRequired = handleCollision(listener, rescanRequired, res);
		}

		if (rescanRequired || environmentMap.hasUnknownInfront(this.robotState.x_CM, robotState.y_CM,
				robotState.heading_DEG, RobotCalibration.HARD_OBSTICLE_WIDTH_CM, 10)) {
			robotController.continuousScannerSweep(RobotCalibration.SCAN_CLICKS_IN_FULL_SCAN);
		}

		return path;
	}

	private boolean handleCollision(PathListener listener, boolean rescanRequired, AdvanceResults res) {
		if (res.pressed) {
			logger.info("Stopped. " + res);
			robotController.reverse(Math.max(COLLISION_REVERSE_CLICKS, res.getDistance() / 2));
			listener.stateChanged();
			rescanRequired = true;
		} else if (res.edgeSensor) {
			logger.info("Edge sensor detected collision " + res);
			robotController.reverse(Math.max(COLLISION_REVERSE_CLICKS, res.getDistance() / 2));
			listener.stateChanged();
		} else if (res.endProximity < RobotCalibration.ULTRASOUND_COLLISION_DISTANCE) {
			rescanRequired = true;
		}
		return rescanRequired;
	}

}

package phil.legoev3webservice.ai;

import java.awt.Point;
import java.awt.Robot;
import java.util.List;

import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class AutoDriveController {
	private final AStarAlgorithm aStarAlgorithm;
	private final RobotController robotController;
	private final RobotState robotState;
	private final LinearisePath linearisePath;

	public AutoDriveController(AStarAlgorithm aStarAlgorithm, RobotController robotController, RobotState robotState,
			LinearisePath linearisePath) {
		super();
		this.aStarAlgorithm = aStarAlgorithm;
		this.robotController = robotController;
		this.robotState = robotState;
		this.linearisePath = linearisePath;
	}

	public LinearisePath getLinearisePath() {
		return linearisePath;
	}

	public AStarAlgorithm getaStarAlgorithm() {
		return aStarAlgorithm;
	}

	public void initialise() {
		robotController.fullScannerSweep(RobotCalibration.SCAN_ITERS, RobotCalibration.SCAN_CLICKS_PER_ITER);
		robotController.rotate((int) (180 * RobotCalibration.ROTATE_CLICKS_PER_DEGREE));
		robotController.fullScannerSweep(RobotCalibration.SCAN_ITERS, RobotCalibration.SCAN_CLICKS_PER_ITER);
	}
	
	public List<Point> driveOneStep(PathListener listener) {
		robotController.fullScannerSweep(RobotCalibration.SCAN_ITERS, RobotCalibration.SCAN_CLICKS_PER_ITER);
		List<Point> path = aStarAlgorithm.getAStarPath();
		listener.onNewPath(path);
		RobotMoveCommand lc = linearisePath.getNextLinearCommand(path);
		robotController.rotate(
				(int) Math.round((lc.heading - robotState.heading_DEG) * RobotCalibration.ROTATE_CLICKS_PER_DEGREE));
		listener.stateChanged();

		AdvanceResults res = robotController
				.advanceWithoutCollision((int) Math.round(lc.distance * RobotCalibration.MOVE_CLICKS_PER_CM));
		listener.stateChanged();
		if (res.pressed) {
			robotController.reverse(res.getDistance() / 2);
			listener.stateChanged();
		}

		return path;
	}

}

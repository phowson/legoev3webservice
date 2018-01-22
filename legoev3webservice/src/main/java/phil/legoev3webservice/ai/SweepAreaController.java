package phil.legoev3webservice.ai;

import java.awt.Point;
import java.util.Collections;
import java.util.List;

import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class SweepAreaController {
	private final AutoDriveController autoDriveController;
	private final EnvironmentMap environmentMap;
	private final RobotState state;

	public SweepAreaController(AutoDriveController autoDriveController, EnvironmentMap map, RobotState state) {
		super();
		this.autoDriveController = autoDriveController;
		this.environmentMap = map;
		this.state = state;
	}

	public void initialise() {

		this.autoDriveController.initialise();
		environmentMap.fillVisited((int) state.x_CM, (int) state.y_CM, (int) RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		findNextUnvisitedPoint();

	}

	private boolean findNextUnvisitedPoint() {

		environmentMap.fillVisited((int) state.x_CM, (int) state.y_CM, (int) RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		while (true) {
			Point point = environmentMap.findClosestUnvisited((int) state.x_CM, (int) state.y_CM,
					(int) RobotCalibration.SENSOR_INFINITY_POINT_CM - 1, false);
			if (point == null) {
				point = environmentMap.findClosestUnvisited((int) state.x_CM, (int) state.y_CM,
						(int) RobotCalibration.HARD_OBSTICLE_WIDTH_CM, false);
			}

			if (point == null) {
				return false;
			}

			this.autoDriveController.getaStarAlgorithm().setTargetX(point.x);
			this.autoDriveController.getaStarAlgorithm().setTargetY(point.y);

			int x_CM = this.autoDriveController.getaStarAlgorithm().getTargetX();
			int y_CM = this.autoDriveController.getaStarAlgorithm().getTargetY();
			int v = environmentMap.getAt(x_CM, y_CM);

			if (v == EnvironmentMap.KNOWN_CLEAR) {
				break;
			} else {
				environmentMap.fillVisited(point.x, point.y, RobotCalibration.HARD_OBSTICLE_WIDTH_CM);

			}
		}

		return true;

	}

	public boolean driveOneStep(PathListener listener) {
		AStarAlgorithm as = this.autoDriveController.getaStarAlgorithm();
		if (as.alreadyInTargetArea() || environmentMap.isVisited(as.getTargetX(), as.getTargetY())) {
			if (!findNextUnvisitedPoint()) {
				return false;
			}
		}

		List<Point> path = this.autoDriveController.driveOneStep(listener);
		if (path.isEmpty() || as.getPathCost() > RobotCalibration.AI_DANGER_PENALTY) {
			environmentMap.fillVisited(as.getTargetX(), as.getTargetY(), RobotCalibration.HARD_OBSTICLE_WIDTH_CM);
		}

		listener.onNewPath(path, as.getTargetX(), as.getTargetY());

		return true;
	}

}

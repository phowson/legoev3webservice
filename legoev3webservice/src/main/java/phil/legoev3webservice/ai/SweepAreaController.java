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
		Point point = environmentMap.findClosestUnvisited((int) state.x_CM, (int) state.y_CM,
				(int) RobotCalibration.HARD_OBSTICLE_WIDTH_CM, false);

		if (point == null) {
			return false;
		}

		this.autoDriveController.getaStarAlgorithm().setTargetX(point.x);
		this.autoDriveController.getaStarAlgorithm().setTargetY(point.y);
		return true;

	}

	public boolean driveOneStep(PathListener listener) {
		if (this.autoDriveController.getaStarAlgorithm().alreadyInTargetArea()) {
			if (!findNextUnvisitedPoint()) {
				return false;
			}
		}
		int x_CM = this.autoDriveController.getaStarAlgorithm().getTargetX();
		int y_CM = this.autoDriveController.getaStarAlgorithm().getTargetY();
		int v = environmentMap.getAt(x_CM, y_CM);

		if (v != EnvironmentMap.KNOWN_CLEAR) {
			// No longer unoccupied?
			if (!findNextUnvisitedPoint()) {
				return false;
			}
			listener.onNewPath(Collections.emptyList(), this.autoDriveController.getaStarAlgorithm().getTargetX(),
					this.autoDriveController.getaStarAlgorithm().getTargetY());

		}

		List<Point> path = this.autoDriveController.driveOneStep(listener);
		if (path.isEmpty()) {
			if (!findNextUnvisitedPoint()) {
				return false;
			}
		}
		listener.onNewPath(path, this.autoDriveController.getaStarAlgorithm().getTargetX(),
				this.autoDriveController.getaStarAlgorithm().getTargetY());

		return true;
	}

}

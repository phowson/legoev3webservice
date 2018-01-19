package phil.legoev3webservice.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;

public class AStarAlgorithm {

	private final RobotState state;
	private final EnvironmentMap map;
	private final int targetX;
	private final int targetY;
	private final static double root2 = Math.sqrt(2);

	public AStarAlgorithm(RobotState state, EnvironmentMap map, int tx, int ty) {
		this.state = state;
		this.map = map;
		this.targetX = tx;
		this.targetY = ty;
	}

	public List<Point> getAStarPath() {
		this.map.resetAStarData();
		PriorityQueue<SearchPoint> searchPoints = new PriorityQueue<>();
		this.map.setAStarDist((int) Math.round(state.x_CM), (int) Math.round(state.y_CM), 0);
		addSearchPoints(searchPoints, state.x_CM, state.y_CM, null, 0);
		SearchPoint finalPoint = null;
		while (!searchPoints.isEmpty()) {
			SearchPoint sp = searchPoints.remove();
			if (sp.x == targetX && sp.y == targetY) {
				finalPoint = sp;
				break;
			}

			addSearchPoints(searchPoints, sp.x, sp.y, sp, sp.pathLength);

		}

		List<Point> out = new ArrayList<>();

		while (finalPoint != null) {
			out.add(new Point(finalPoint.x, finalPoint.y));
			finalPoint = finalPoint.pred;
		}
		Collections.reverse(out);
		System.out.print(out);
		return out;

	}

	private void addSearchPoints(PriorityQueue<SearchPoint> searchPoints, double x_CM, double y_CM, SearchPoint pred,
			double pathLen) {
		int x = (int) Math.round(x_CM);
		int y = (int) Math.round(y_CM);

		tryAdd(searchPoints, x - 1, y - 1, pred, pathLen + root2);
		tryAdd(searchPoints, x, y - 1, pred, pathLen + 1);
		tryAdd(searchPoints, x + 1, y - 1, pred, pathLen + root2);

		tryAdd(searchPoints, x - 1, y, pred, pathLen + 1);
		tryAdd(searchPoints, x + 1, y, pred, pathLen + 1);

		tryAdd(searchPoints, x - 1, y + 1, pred, pathLen + root2);
		tryAdd(searchPoints, x, y + 1, pred, pathLen + 1);
		tryAdd(searchPoints, x + 1, y + 1, pred, pathLen + root2);

	}

	private void tryAdd(PriorityQueue<SearchPoint> searchPoints, int x, int y, SearchPoint pred, double pathLen) {
		int c = map.getAt(x, y);

		// A* part. Our euristic is the straight line distance to the target.
		int dx = x - targetX;
		int dy = y - targetY;

		double d = Math.sqrt(dx * dx + dy * dy);

		if (c != EnvironmentMap.OBSTRUCTION && c != EnvironmentMap.HARD_OBSTRUCTION) {
			if (c == EnvironmentMap.DANGER) {
				d += 100;
			}
			double existingDist = map.getAStarDist(x, y);
			if (existingDist > pathLen) {
				searchPoints.add(new SearchPoint(x, y, d, pred, pathLen));
			}
		}

	}

}

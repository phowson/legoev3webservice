package phil.legoev3webservice.ai;

import java.awt.Point;
import java.util.List;

import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.RobotState;

public class LinearisePath {

	private final double eps = 1e-3;
	private final RobotState state;
	private final EnvironmentMap map;
	private double maxAllowableCost;
	private double maxDistInOneStep;

	public void setMaxAllowableCost(double maxAllowableCost) {
		this.maxAllowableCost = maxAllowableCost;
	}

	public void setMaxDistInOneStep(double maxDistInOneStep) {
		this.maxDistInOneStep = maxDistInOneStep;
	}

	public LinearisePath(RobotState state, EnvironmentMap map, double maxAllowableCost, double maxDistInOneStep) {
		this.state = state;
		this.map = map;
		this.maxAllowableCost = maxAllowableCost;
		this.maxDistInOneStep = maxDistInOneStep;
	}

	public RobotMoveCommand getNextLinearCommand(List<Point> path) {
		if (path.isEmpty()) {
			return new RobotMoveCommand(0, 0);
		}
		int i = 0;
		boolean ignoreLineOk = false;
		while (i < path.size() - 1) {
			double cost = computeLineCost(path, i);

			if (!ignoreLineOk && !lineIsOk(path, i)) {
				if (i == 0) {
					ignoreLineOk = true;
				} else {
					i = i - 1;
					break;
				}
			}

			if (cost > maxAllowableCost) {
				break;
			}
			i = i + 1;
		}
		Point pi = path.get(i);
		double finalX = pi.getX();
		double finalY = pi.getY();

		double dx = finalX - state.x_CM;
		double dy = finalY - state.y_CM;

		double heading;
		heading = Math.atan(dy / dx) * 180 / Math.PI;
		if (dx < 0) {
			heading = heading + 180;
		}
		double dist = Math.min(maxDistInOneStep, Math.sqrt(dx * dx + dy * dy));
		if (dist < 1) {
			heading = 0;
			dist = 0;
		}
		return new RobotMoveCommand(heading, dist);

	}

	private boolean lineIsOk(List<Point> path, int i) {
		Point pi = path.get(i);
		double finalX = pi.getX();
		double finalY = pi.getY();

		double dx = finalX - state.x_CM;
		double dy = finalY - state.y_CM;
		double l = Math.round(Math.sqrt(dx * dx + dy * dy));
		double dx1 = dx / l;
		double dy1 = dy / l;
		double x = state.x_CM;
		double y = state.y_CM;
		for (int z = 0; z < l; ++z) {
			int v = map.getAt((int) x, (int) y);
			if (v == EnvironmentMap.OBSTRUCTION || v == EnvironmentMap.HARD_OBSTRUCTION || v==EnvironmentMap.DANGER) {
				return false;
			}
			x += dx1;
			y += dy1;

		}

		return true;
	}

	public double computeLineCost(List<Point> points, int index) {

		Point pi = points.get(index);
		double finalX = pi.getX();
		double finalY = pi.getY();

		double dx = state.x_CM - finalX;
		double dy = state.y_CM - finalY;

		double acc = 0;
		if (Math.abs(dx) > eps) {
			double gradient = dy / dx;
			for (int i = 0; i < index; ++i) {
				Point point = points.get(i);
				double px = point.getX();
				double py = point.getY();

				double lineY = py + (px - state.x_CM) * gradient;

				double d = lineY - py;
				acc += Math.sqrt(d * d);
			}
		} else {
			double gradient = dx / dy;
			for (int i = 0; i < index; ++i) {
				Point point = points.get(i);
				double px = point.getX();
				double py = point.getY();

				double lineX = px + (py - state.y_CM) * gradient;

				double d = lineX - px;
				acc += Math.sqrt(d * d);
			}

		}

		return acc;

	}

}

package phil.legoev3webservice.map;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import phil.legoev3webservice.control.FilteredSensorData;
import phil.legoev3webservice.robot.RobotCalibration;

public class EnvironmentMap {

	public static final int UNKNOWN = 0;
	public static final int KNOWN_CLEAR = 1;
	public static final int OBSTRUCTION = 2;
	public static final int DANGER = 3;
	public static final int HARD_OBSTRUCTION = 4;

	private static final double PI_180 = Math.PI / 180.;

	public final int mapWidth;
	private TIntIntHashMap mapData = new TIntIntHashMap();
	private TIntDoubleHashMap aStarData = new TIntDoubleHashMap();

	public EnvironmentMap(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	public void resetAStarData() {
		aStarData.clear();
	}

	public double getAStarDist(int x_CM, int y_CM) {
		double d = aStarData.get(genKey(x_CM, y_CM));
		if (d == 0) {
			return Double.NaN;
		}
		return d - 1;
	}

	public double setAStarDist(int x_CM, int y_CM, double value) {
		return aStarData.put(genKey(x_CM, y_CM), value + 1);
	}

	public int getAt(int x_CM, int y_CM) {
		return mapData.get(genKey(x_CM, y_CM));
	}

	public int setAt(int x_CM, int y_CM, int value) {
		return mapData.put(genKey(x_CM, y_CM), value);
	}

	private int genKey(int x_CM, int y_CM) {
		return x_CM + y_CM * mapWidth;
	}

	public void hitHardObsticle(RobotState currentState, int obsticleSize) {

		double normalHeading = (currentState.heading_DEG + 90) * PI_180;
		double vecX = Math.cos(normalHeading);
		double vecY = Math.sin(normalHeading);
		int halfSize = obsticleSize / 2;
		for (int z = -obsticleSize / 2; z < halfSize; ++z) {
			int x = (int) Math.round(vecX * z + currentState.x_CM);
			int y = (int) Math.round(vecY * z + currentState.y_CM);
			fillInArea(x, y, HARD_OBSTRUCTION, 2);
		}

	}

	public void apply(RobotState currentState, FilteredSensorData sensorData) {
		for (int i = 0; i < sensorData.distance_CM.length; ++i) {
			double overallHeadingDeg = currentState.heading_DEG + sensorData.headings[i];
			double overallHeadingRad = overallHeadingDeg * PI_180;

			double vecX = Math.cos(overallHeadingRad);
			double vecY = Math.sin(overallHeadingRad);

			double dist = sensorData.distance_CM[i];
			double d2 = Math.min(RobotCalibration.SENSOR_INFINITY_POINT_CM, dist);
			for (int z = 0; z < d2; ++z) {
				int x = (int) Math.round(vecX * z + currentState.x_CM);
				int y = (int) Math.round(vecY * z + currentState.y_CM);
				int v = getAt(x, y);
				if (v != HARD_OBSTRUCTION)
					setAt(x, y, KNOWN_CLEAR);
			}

			if (!Double.isInfinite(dist)) {

				int obstructionX = (int) Math.round(vecX * dist + currentState.x_CM);
				int obstructionY = (int) Math.round(vecY * dist + currentState.y_CM);

				fillInArea(obstructionX, obstructionY, DANGER, RobotCalibration.DANGER_RADIUS_CM);

				setAt(obstructionX, obstructionY, OBSTRUCTION);

			}

		}

	}

	private void fillInArea(int obstructionX, int obstructionY, int vz, double radius) {
		for (int dx = (int) (obstructionX - radius); dx < obstructionX
				+ radius; ++dx) {
			for (int dy = (int) (obstructionY - radius); dy < obstructionY
					+ radius; ++dy) {
				double xdst = (dx - obstructionX);
				double ydst = (dy - obstructionY);
				double zz = Math.sqrt(xdst * xdst + ydst * ydst);
				if (zz < radius) {
					int v = getAt(dx, dy);
					if (v != OBSTRUCTION && v != HARD_OBSTRUCTION && v != DANGER) {
						setAt(dx, dy, vz);
					}
				}
			}

		}
	}
}

package phil.legoev3webservice.map;

import java.awt.Point;
import java.awt.Robot;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import phil.legoev3webservice.control.FilteredSensorData;
import phil.legoev3webservice.robot.RobotCalibration;

public class EnvironmentMap implements Serializable {

	private static final long serialVersionUID = -6127184558344282497L;
	public static final int UNKNOWN = 0;
	public static final int KNOWN_CLEAR = 1;
	public static final int OBSTRUCTION = 2;
	public static final int DANGER = 3;
	public static final int HARD_OBSTRUCTION = 4;

	private static final double PI_180 = Math.PI / 180.;

	public final int mapWidth;
	private TIntIntHashMap mapData = new TIntIntHashMap();
	private TIntByteHashMap visitedData = new TIntByteHashMap();
	private TIntDoubleHashMap aStarData = new TIntDoubleHashMap();

	public EnvironmentMap(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	public boolean hasUnknownInfront(double x, double y, double heading_DEG, int widthCm, int heightCm) {
		double headingRad = heading_DEG * PI_180;
		double vx = Math.cos(headingRad);
		double vy = Math.sin(headingRad);

		double nvx = Math.cos(headingRad + Math.PI / 2);
		double nvy = Math.sin(headingRad + Math.PI / 2);

		for (int i = -widthCm / 2; i < widthCm / 2; ++i) {
			for (int j = 0; j < heightCm; ++j) {

				double x2 = x + vx * j + nvx * i;
				double y2 = y + vy * j + nvy * i;

				if (getAt((int) Math.round(x2), (int) Math.round(y2)) == UNKNOWN) {
					return true;
				}

			}

		}

		return false;
	}

	public Point findClosestUnvisited(int x, int y, int minDist, boolean inverse) {
		int sx = -1;
		int sy = -1;
		double bestSoFar = inverse ? 0 : Double.POSITIVE_INFINITY;

		for (int k : mapData.keys()) {
			int md = mapData.get(k);
			if (visitedData.get(k) == 0 && md == KNOWN_CLEAR) {
				int kx = k % mapWidth;
				int ky = k / mapWidth;
				int dx = x - kx;
				int dy = y - ky;
				double dist = Math.sqrt(dx * dx + dy * dy);
				if (dist > minDist) {

					if ((dist < bestSoFar && !inverse) || (dist > bestSoFar && inverse)) {

						sx = kx;
						sy = ky;
						bestSoFar = dist;
					}
				}
			}

		}
		if (sx != -1)
			return new Point(sx, sy);
		return null;

	}

	public void resetAStarData() {
		aStarData.clear();
	}

	public void setVisited(int x_CM, int y_CM) {
		visitedData.put(genKey(x_CM, y_CM), (byte) 1);
	}

	public boolean isVisited(int x_CM, int y_CM) {
		if (visitedData != null)
			return visitedData.get(genKey(x_CM, y_CM)) == 1;
		return false;
	}

	public double getAStarDist(int x_CM, int y_CM) {
		double d = aStarData.get(genKey(x_CM, y_CM));
		if (d == 0) {
			return Double.POSITIVE_INFINITY;
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

	public void applySingleSensorReading(RobotState currentState, double cm) {

		double overallHeadingDeg = currentState.heading_DEG;
		double overallHeadingRad = overallHeadingDeg * PI_180;

		double vecX = Math.cos(overallHeadingRad);
		double vecY = Math.sin(overallHeadingRad);

		double vecX2 = Math.cos(overallHeadingRad + Math.PI / 2);
		double vecY2 = Math.sin(overallHeadingRad + Math.PI / 2);

		double x_CM = currentState.x_CM + vecX * RobotCalibration.SENSOR_OFFSET_X
				+ vecX2 * RobotCalibration.SENSOR_OFFSET_Y;
		double y_CM = currentState.y_CM + vecY * RobotCalibration.SENSOR_OFFSET_Y
				+ vecY2 * RobotCalibration.SENSOR_OFFSET_Y;

		fillDataAtHeading(cm, overallHeadingRad, x_CM, y_CM);
	}

	public void apply(RobotState currentState, FilteredSensorData sensorData, boolean useOffset) {

		double overallHeadingDeg = currentState.heading_DEG;
		double overallHeadingRad = overallHeadingDeg * PI_180;

		double x_CM;
		double y_CM;
		double vecX = Math.cos(overallHeadingRad);
		double vecY = Math.sin(overallHeadingRad);

		if (useOffset) {

			double vecX2 = Math.cos(overallHeadingRad + Math.PI / 2);
			double vecY2 = Math.sin(overallHeadingRad + Math.PI / 2);

			x_CM = currentState.x_CM + vecX * RobotCalibration.SENSOR_OFFSET_X
					+ vecX2 * RobotCalibration.SENSOR_OFFSET_Y;
			y_CM = currentState.y_CM + vecY * RobotCalibration.SENSOR_OFFSET_Y
					+ vecY2 * RobotCalibration.SENSOR_OFFSET_Y;
		} else {
			x_CM = currentState.x_CM;
			y_CM = currentState.y_CM;
		}

		for (int i = 0; i < sensorData.distance_CM.length; ++i) {
			overallHeadingDeg = currentState.heading_DEG + sensorData.headings[i];
			overallHeadingRad = overallHeadingDeg * PI_180;
			double dist = sensorData.distance_CM[i];
			fillDataAtHeading(dist, overallHeadingRad, x_CM, y_CM);

		}

	}

	private void fillDataAtHeading(double dist, double overallHeadingRad, double x_CM, double y_CM) {
		double vecX;
		double vecY;
		vecX = Math.cos(overallHeadingRad);
		vecY = Math.sin(overallHeadingRad);

		double d2 = Math.min(RobotCalibration.SENSOR_INFINITY_POINT_CM, dist);
		for (int z = 0; z < d2; ++z) {
			int x = (int) Math.round(vecX * z + x_CM);
			int y = (int) Math.round(vecY * z + y_CM);

			if (getAt(x, y) != HARD_OBSTRUCTION)
				setArea(x, y, KNOWN_CLEAR);
		}

		if (!Double.isInfinite(dist)) {

			int obstructionX = (int) Math.round(vecX * dist + x_CM);
			int obstructionY = (int) Math.round(vecY * dist + y_CM);

			fillInArea(obstructionX, obstructionY, DANGER, RobotCalibration.DANGER_RADIUS_CM);
			fillInArea(obstructionX, obstructionY, OBSTRUCTION, RobotCalibration.SENSOR_RESOLUTION);

		}
	}

	private void setArea(int x, int y, int v) {

		safeSetAt(x, y, v);
		safeSetAt(x - 1, y, v);
		safeSetAt(x + 1, y, v);
		safeSetAt(x, y - 1, v);
		safeSetAt(x, y + 1, v);

	}

	private void safeSetAt(int x, int y, int z) {
		int v = getAt(x, y);
		if (v != HARD_OBSTRUCTION) {
			setAt(x, y, z);
		}
	}

	private void fillInArea(int obstructionX, int obstructionY, int vz, double radius) {
		for (int dx = (int) (obstructionX - radius); dx < obstructionX + radius; ++dx) {
			for (int dy = (int) (obstructionY - radius); dy < obstructionY + radius; ++dy) {
				double xdst = (dx - obstructionX);
				double ydst = (dy - obstructionY);
				double zz = Math.sqrt(xdst * xdst + ydst * ydst);
				if (zz < radius) {
					int v = getAt(dx, dy);
					if (v != OBSTRUCTION && v != HARD_OBSTRUCTION) {
						setAt(dx, dy, vz);
					}
				}
			}

		}
	}

	public static void save(String fname, EnvironmentMap map) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(fname);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);) {
			out.writeObject(map);
		}

	}

	public static EnvironmentMap load(String fname) throws FileNotFoundException, IOException, ClassNotFoundException {
		try (FileInputStream fileIn = new FileInputStream(fname);
				ObjectInputStream in = new ObjectInputStream(fileIn);) {
			EnvironmentMap e = (EnvironmentMap) in.readObject();
			return e;
		}

	}

	public void fillVisited(int x, int y, int hardObsticleWidthCm) {
		int halfW = hardObsticleWidthCm / 2;
		for (int sx = x - halfW; sx < x + halfW; ++sx) {
			for (int sy = y - halfW; sy < y + halfW; ++sy) {
				int dx = sx - x;
				int dy = sy - y;

				if (Math.sqrt(dx * dx + dy * dy) < halfW) {
					setVisited(sx, sy);
				}
			}

		}
	}

}

package phil.legoev3webservice.map;

import gnu.trove.map.hash.TIntIntHashMap;
import phil.legoev3webservice.control.FilteredSensorData;

public class EnvironmentMap {

	public static final int UNKNOWN = 0;
	public static final int KNOWN_CLEAR = 1;
	public static final int OBSTRUCTION = 2;
	public static final int DANGER = 3;
	public static final int HARD_OBSCRUTION = 4;

	private final int mapWidth;
	private TIntIntHashMap mapData;

	public EnvironmentMap(int mapWidth) {
		this.mapWidth = mapWidth;
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

	public void apply(RobotState currentState, FilteredSensorData sensorData) {

	}
}

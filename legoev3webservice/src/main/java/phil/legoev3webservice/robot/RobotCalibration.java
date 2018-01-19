package phil.legoev3webservice.robot;

public class RobotCalibration {
	// public static final int SCAN_ITERS = 180;
	// public static final int SCAN_CLICKS_PER_ITER = 27;

	// public static final int SCAN_ITERS = 60;
	// public static final int SCAN_CLICKS_PER_ITER = 27 * 3;

	public static final int SCAN_ITERS = 40;
	public static final int SCAN_CLICKS_PER_ITER = 122;

	public static final double SCAN_DEGREES_PER_VALUE = 180.0 / SCAN_ITERS;

	public static final int ROTATE_CLICKS_PER_90DEGREES = 480;
	public static final double ROTATE_CLICKS_PER_DEGREE = ROTATE_CLICKS_PER_90DEGREES / 90.0;
	public static final double ROTATE_DEGREES_PER_CLICK = 1.0 / ROTATE_CLICKS_PER_DEGREE;

	public static final double MOVE_CLICKS_PER_CM = 2002 / 56.1;
	public static final double MOVE_CM_PER_CLICK = 1.0 / MOVE_CLICKS_PER_CM;

	public static final double[] SENSOR_CALIBRATION_PC = new double[] { 0, 4, 13, 26, 40, 54, 66, 72, 80, 100 };

	public static final double[] SENSOR_CALIBRATION_CM = new double[] { 0, 7, 12.5, 24.5, 36, 51, 68, 83, 102, 220 };

	public static final double SENSOR_CM_PER_UNIT1 = 30.0 / 34.5;
	public static final double SENSOR_CM_PER_UNIT2 = 45 / 50.5;
	public static final double SENSOR_CM_PER_UNIT = (SENSOR_CM_PER_UNIT1 + SENSOR_CM_PER_UNIT2) / 2.0;

	// Anything beyond 60cm count as infinity;
	public static final double SENSOR_INFINITY_POINT_CM = 70;
	public static final int SENSOR_INFINITY_POINT_UNITS = (int) (SENSOR_INFINITY_POINT_CM / SENSOR_CM_PER_UNIT);

	public static final double DANGER_RADIUS_CM = 13;
	public static final int HARD_OBSTICLE_WIDTH_CM = 27;
	public static final double AI_DANGER_PENALTY = 1000;
	public static final double DEFAULT_LINEAR_TOLERANCE = 100;

	public static final double SENSOR_OFFSET_X = 6;
	public static final double SENSOR_OFFSET_Y = -2;
	public static final double AI_OBSTRUCTION_PENALTY = 10000;

}

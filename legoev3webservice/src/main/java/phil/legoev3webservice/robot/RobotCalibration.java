package phil.legoev3webservice.robot;

public class RobotCalibration {
	// public static final int SCAN_ITERS = 180;
	// public static final int SCAN_CLICKS_PER_ITER = 27;

	// public static final int SCAN_ITERS = 60;
	// public static final int SCAN_CLICKS_PER_ITER = 27 * 3;

	public static final int SCAN_CLICKS_IN_FULL_SCAN = 200 * 27;

	public static final int SCAN_ITERS = 40;
	public static final int SCAN_CLICKS_PER_ITER = 122;
	public static final double SCAN_DEGREES_PER_CLICK = 1. / 27.;

	public static final double SCAN_DEGREES_PER_VALUE = 180.0 / SCAN_ITERS;

	public static final int ROTATE_CLICKS_PER_90DEGREES = 510;
	public static final double ROTATE_CLICKS_PER_DEGREE = ROTATE_CLICKS_PER_90DEGREES / 90.0;
	public static final double ROTATE_DEGREES_PER_CLICK = 1.0 / ROTATE_CLICKS_PER_DEGREE;

	public static final double MOVE_CLICKS_PER_CM = 2002 / 56.1;
	public static final double MOVE_CM_PER_CLICK = 1.0 / MOVE_CLICKS_PER_CM;

	public static final double[] SENSOR_CALIBRATION_PC = new double[] { 0, 2550 };

	public static final double[] SENSOR_CALIBRATION_CM = new double[] { 0, 255 };

	// Anything beyond 60cm count as infinity;
	public static final double SENSOR_INFINITY_POINT_CM = 200;

	public static final double SENSOR_POINTS_PER_CM = 10;
	public static final float ULTRASOUND_COLLISION_DISTANCE = 50;
	public static final int SENSOR_INFINITY_POINT_UNITS = (int) (SENSOR_INFINITY_POINT_CM * SENSOR_POINTS_PER_CM);

	public static final double DANGER_RADIUS_CM = 10;
	public static final int SENSOR_RESOLUTION = 4;

	public static final int HARD_OBSTICLE_WIDTH_CM = 27;
	public static final double DEFAULT_LINEAR_TOLERANCE = 150;

	public static final double SENSOR_OFFSET_X = 6;
	public static final double SENSOR_OFFSET_Y = -2;
	public static final double AI_DANGER_PENALTY =      5000;
	public static final double AI_OBSTRUCTION_PENALTY = 100000;

	public static final int SENSOR_COLOR_STOP = 10;

	public static final double MAX_LINEAR_MOVE_CM = 100;

}

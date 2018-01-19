package phil.legoev3webservice.robot;

public class RobotCalibration {
//	public static final int SCAN_ITERS = 180;		
//	public static final int SCAN_CLICKS_PER_ITER = 27;
	
	public static final int SCAN_ITERS = 90;		
	public static final int SCAN_CLICKS_PER_ITER = 27*2;	
	
	public static final double SCAN_DEGREES_PER_VALUE = 180.0 / SCAN_ITERS;

	public static final int ROTATE_CLICKS_PER_90DEGREES = 530;
	public static final double ROTATE_CLICKS_PER_DEGREE = ROTATE_CLICKS_PER_90DEGREES / 90.0;
	public static final double ROTATE_DEGREES_PER_CLICK = 1.0/ROTATE_CLICKS_PER_DEGREE;

	public static final double MOVE_CLICKS_PER_CM = 2002 / 56.1;
	public static final double MOVE_CM_PER_CLICK = 1.0/MOVE_CLICKS_PER_CM;

	public static final double SENSOR_CM_PER_UNIT1 = 30.0 / 34.5;
	public static final double SENSOR_CM_PER_UNIT2 = 45 / 50.5;
	public static final double SENSOR_CM_PER_UNIT = (SENSOR_CM_PER_UNIT1 + SENSOR_CM_PER_UNIT2) / 2.0;

	// Anything beyond 60cm count as infinity;
	public static final double SENSOR_INFINITY_POINT_CM = 70;
	public static final int SENSOR_INFINITY_POINT_UNITS = (int) (SENSOR_INFINITY_POINT_CM / SENSOR_CM_PER_UNIT);

	public static final double DANGER_RADIUS_CM = 5;
	public static final int HARD_OBSTICLE_WIDTH_CM = 27;
	

}

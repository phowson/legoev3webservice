package phil.legoev3webservice.map;

public class RobotState {
	public double x_CM;
	public double y_CM;
	public double heading_DEG;

	private static final double PI_180 = Math.PI / 180.;

	public void advance(double cm) {
		double overallHeadingRad = heading_DEG * PI_180;
		double vecX = Math.cos(overallHeadingRad);
		double vecY = Math.sin(overallHeadingRad);
		x_CM += vecX * cm;
		y_CM += vecY * cm;
	}

	public void rotate(double degrees) {
		heading_DEG += degrees;
	}

}

package phil.legoev3webservice.map;

public class RobotState {
	public double x_CM;
	public double y_CM;
	public double heading_DEG;

	public void advance(double cm) {
		
	}

	public void rotate(double degrees) {
		heading_DEG+=degrees;
	}

}

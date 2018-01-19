package phil.legoev3webservice.ai;

public class RobotMoveCommand {
	public final double heading;
	public final double distance;

	public RobotMoveCommand(double heading, double distance) {
		super();
		this.heading = heading;
		this.distance = distance;
	}

}

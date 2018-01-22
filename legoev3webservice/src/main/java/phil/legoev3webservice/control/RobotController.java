package phil.legoev3webservice.control;

public interface RobotController {

	RotateResult rotate(int iclicks);

	AdvanceResults advanceWithoutCollision(int clicks);

	int reverse(int clicks);

	ContinuousScanData continuousScannerSweep(int scanSteps);

	void blockingSensorArrayMove(int target);

}
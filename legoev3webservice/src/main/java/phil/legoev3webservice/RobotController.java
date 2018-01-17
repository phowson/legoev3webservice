package phil.legoev3webservice;

public interface RobotController {

	int rotate(int iclicks);

	AdvanceResults advanceWithoutCollision(int clicks);
	
	int reverse(int clicks);

	ScanData fullScannerSweep(int scanSize, int scanStep) throws InterruptedException;

	void blockingSensorArrayMove(int target);

}
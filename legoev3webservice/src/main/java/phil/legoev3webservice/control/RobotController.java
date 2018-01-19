package phil.legoev3webservice.control;

public interface RobotController {

	int rotate(int iclicks);

	AdvanceResults advanceWithoutCollision(int clicks);
	
	int reverse(int clicks);

	ScanData fullScannerSweep(int scanSize, int scanStep) ;

	void blockingSensorArrayMove(int target);

}
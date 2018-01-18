package phil.legoev3webservice;

import java.io.IOException;
import java.net.InetSocketAddress;

import phil.legoev3webservice.client.RobotClient;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.ScanData;
import phil.legoev3webservice.robot.RobotCalibration;

public class GUIClient {

	private RobotClient client;

	public GUIClient(RobotClient robotClient) {
		this.client = robotClient;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		new GUIClient(new RobotClient(new InetSocketAddress(args[0], Integer.parseInt(args[1])))).run();
	}

	private void run() {
		ScanData scanResults = this.client.fullScannerSweep(180, 27);
		System.out.println(scanResults);
		
//		this.client.rotate(530*4);
		
		//this.client.reverse(500);
		
//		AdvanceResults res = this.client.advanceWithoutCollision((int) (50*RobotCalibration.MOVE_CLICKS_PER_CM));
//		System.out.println(res);
	}

}

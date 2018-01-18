package phil.legoev3webservice;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.DummyRobotController;
import phil.legoev3webservice.control.LocalRobotController;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.ScanData;
import phil.legoev3webservice.server.RobotControlServer;

public class App {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException, IOException {
		String path = App.class.getClassLoader().getResource("jul-log.properties").getFile();
		System.setProperty("java.util.logging.config.file", path);
		new App().run();

	}

	private void run() throws IOException {

		logger.info("Try to start server on port 5050");
		try (RobotControlServer s = new RobotControlServer(new LocalRobotController(), 5050)) {
			s.run();
		}
	}

	private void example() throws InterruptedException {
		RobotController robot = new LocalRobotController();
		ScanData sweep = robot.fullScannerSweep(80, 60);
		System.out.println("Scan 1 data : ");
		System.out.println(Arrays.toString(sweep.irData));
		System.out.println(Arrays.toString(sweep.colorData));

		System.out.println("Scan 2 data : ");
		System.out.println(Arrays.toString(sweep.irData2));
		System.out.println(Arrays.toString(sweep.colorData2));

		int dist = robot.rotate(500);
		System.out.println("Total distance travelled : " + dist);

		dist = robot.rotate(-500);
		System.out.println("Total distance travelled : " + dist);

		AdvanceResults result = robot.advanceWithoutCollision(1000);
		System.out.println("Total distance travelled in clicks : " + result.clicksAdvanced);
		int d = result.startProximity - result.endProximity;
		System.out.println("Total distance travelled according to IR sensor : " + 70.0 * (d / 100.0) + "cm");
	}
}

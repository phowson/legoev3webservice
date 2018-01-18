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

public class DummyApp {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DummyApp.class);

	public static void main(String[] args) throws InterruptedException, IOException {
		String path = DummyApp.class.getClassLoader().getResource("jul-log.properties").getFile();
		System.setProperty("java.util.logging.config.file", path);
		new DummyApp().run();

	}

	private void run() throws IOException {

		logger.info("Try to start server on port 5050");
		try (RobotControlServer s = new RobotControlServer(new DummyRobotController(), 5050)) {
			s.run();
		}
	}
}

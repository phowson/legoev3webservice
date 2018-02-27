package phil.legoev3webservice;

import java.net.InetSocketAddress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import phil.legoev3webservice.client.RobotClient;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.ContinuousScanData;
import phil.legoev3webservice.control.DummyRobotController;
import phil.legoev3webservice.control.RotateResult;
import phil.legoev3webservice.control.ScanData;
import phil.legoev3webservice.server.RobotControlServer;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

	private RobotControlServer server;
	private volatile Exception excep;

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		excep = null;
		server = new RobotControlServer(new DummyRobotController(), 5050);
		new Thread() {
			public void run() {
				try {
					server.run();
				} catch (Exception e) {
					e.printStackTrace();
					excep = e;
				}
			}
		}.start();
	}

	protected void tearDown() throws Exception {
		server.close();
		if (excep != null) {
			throw excep;
		}
	}

	public void testClientRotate() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			RotateResult r = client.rotate(20);
			assertEquals(20, r.ticksRotated);
			ContinuousScanData res = r.scanData;
			assertEquals(10, res.steps.length);
			assertEquals(10, res.ultrasoundSensor.length);

			for (int i : res.steps) {
				assertEquals(0, i);
			}
		}
	}

	public void testClientReverse() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			int res = client.reverse(10);
			assertEquals(10, res);
		}
	}

	public void testClientSensorArrayMove() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			client.blockingSensorArrayMove(10);
		}
	}

	public void testClientAdvance() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			AdvanceResults res = client.advanceWithoutCollision(10);
			assertEquals(10, res.clicksAdvancedLeft);
			assertEquals(10, res.clicksAdvancedRight);
			assertEquals(false, res.pressed);

		}
	}

	public void testContScan() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			ContinuousScanData res = client.continuousScannerSweep(10);
			assertEquals(10, res.steps.length);
			assertEquals(10, res.ultrasoundSensor.length);

			for (int i : res.steps) {
				assertEquals(0, i);
			}

		}
	}

}

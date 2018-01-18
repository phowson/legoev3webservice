package phil.legoev3webservice;

import java.net.InetSocketAddress;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import phil.legoev3webservice.client.RobotClient;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.DummyRobotController;
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
			int res = client.rotate(10);
			assertEquals(10, res);
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
			assertEquals(10, res.clicksAdvanced);
			assertEquals(false, res.pressed);

		}
	}

	public void testClientScan() throws Exception {
		try (RobotClient client = new RobotClient(new InetSocketAddress("localhost", 5050));) {
			ScanData res = client.fullScannerSweep(10, 5);
			assertEquals(10, res.colorData.length);
			assertEquals(10, res.colorData2.length);

			assertEquals(10, res.irData.length);
			assertEquals(10, res.irData2.length);

			for (int i : res.irData) {
				assertEquals(0, i);
			}

		}
	}

}

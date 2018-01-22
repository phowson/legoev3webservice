package phil.legoev3webservice.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.NetworkMessageConstants;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.ContinuousScanData;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.RotateResult;
import phil.legoev3webservice.control.ScanData;

public class RobotClient implements RobotController, Closeable {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RobotClient.class);
	private final SocketChannel socket;

	private final ByteBuffer outBuffer = ByteBuffer.allocate(1024);
	private final ByteBuffer inBuffer = ByteBuffer.allocate(65535);

	public RobotClient(InetSocketAddress address) throws IOException {
		this.socket = SocketChannel.open(address);
		this.socket.configureBlocking(true);
		this.socket.finishConnect();
	}

	public RotateResult rotate(int iclicks) {

		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_ROTATE);
			outBuffer.putInt(iclicks);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			inBuffer.limit(4);
			while (inBuffer.hasRemaining()) {
				this.socket.read(inBuffer);
			}
			inBuffer.flip();
			int r = inBuffer.getInt();
			inBuffer.clear();
			ContinuousScanData contScanData = readContScanData();
			return new RotateResult(contScanData, r);

		} catch (IOException e) {
			logger.error("IO Error", e);
			throw new RuntimeException(e);
		}
	}

	public AdvanceResults advanceWithoutCollision(int clicks) {
		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_ADVANCE);
			outBuffer.putInt(clicks);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			while (inBuffer.position() < 21) {
				this.socket.read(inBuffer);
			}
			inBuffer.flip();
			int clicksAdvancedL = inBuffer.getInt();
			int clicksAdvancedR = inBuffer.getInt();
			int startProximity = inBuffer.getInt();
			int endProximity = inBuffer.getInt();
			int reflectedLightIntensity = inBuffer.getInt();
			boolean pressed = inBuffer.get() == 1;
			AdvanceResults res = new AdvanceResults(clicksAdvancedL, clicksAdvancedR, startProximity, endProximity,
					reflectedLightIntensity, pressed);
			return res;
		} catch (IOException e) {
			logger.error("IO Error", e);
			throw new RuntimeException(e);
		}
	}

	public int reverse(int clicks) {
		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_REVERSE);
			outBuffer.putInt(clicks);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			while (inBuffer.position() < 4) {
				this.socket.read(inBuffer);
			}
			inBuffer.flip();
			return inBuffer.getInt();
		} catch (IOException e) {
			logger.error("IO Error", e);
			throw new RuntimeException(e);
		}
	}

	private void readIntArray(int[] d) {
		for (int i = 0; i < d.length; ++i) {
			d[i] = inBuffer.getInt();
		}

	}

	public void blockingSensorArrayMove(int target) {
		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_SENSORARRAYMOVE);
			outBuffer.putInt(target);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			while (inBuffer.position() < 4) {
				this.socket.read(inBuffer);
			}
			inBuffer.flip();
		} catch (IOException e) {
			logger.error("IO Error", e);
			throw new RuntimeException(e);
		}
	}

	public void close() throws IOException {
		this.socket.close();

	}

	@Override
	public ContinuousScanData continuousScannerSweep(int scanSteps) {
		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_CONT_SCAN);
			outBuffer.putInt(scanSteps);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			return readContScanData();
		} catch (IOException e) {
			logger.error("IO Error", e);
			throw new RuntimeException(e);
		}
	}

	private ContinuousScanData readContScanData() throws IOException {
		inBuffer.limit(4);
		while (inBuffer.hasRemaining()) {
			this.socket.read(inBuffer);
		}
		inBuffer.flip();
		int len = inBuffer.getInt();
		inBuffer.clear();
		inBuffer.limit(len);
		while (inBuffer.hasRemaining()) {
			this.socket.read(inBuffer);
		}
		inBuffer.flip();

		int[] clicks = new int[len / 8];
		int[] irData = new int[len / 8];

		readIntArray(clicks);
		readIntArray(irData);

		return new ContinuousScanData(clicks, irData);
	}

}

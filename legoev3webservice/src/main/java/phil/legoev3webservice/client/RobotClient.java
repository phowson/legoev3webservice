package phil.legoev3webservice.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.NetworkMessageConstants;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.RobotController;
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

	public int rotate(int iclicks) {

		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_ROTATE);
			outBuffer.putInt(iclicks);
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

	public AdvanceResults advanceWithoutCollision(int clicks) {
		try {
			outBuffer.clear();
			outBuffer.putInt(5);
			outBuffer.put((byte) NetworkMessageConstants.MSG_ADVANCE);
			outBuffer.putInt(clicks);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
			while (inBuffer.position() < 17) {
				this.socket.read(inBuffer);
			}
			inBuffer.flip();
			int clicksAdvanced = inBuffer.getInt();
			int startProximity = inBuffer.getInt();
			int endProximity = inBuffer.getInt();
			int reflectedLightIntensity = inBuffer.getInt();
			boolean pressed = inBuffer.get() == 1;
			AdvanceResults res = new AdvanceResults(clicksAdvanced, startProximity, endProximity,
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

	public ScanData fullScannerSweep(int scanSize, int scanStep) {
		try {
			outBuffer.clear();
			outBuffer.putInt(9);
			outBuffer.put((byte) NetworkMessageConstants.MSG_SCAN);
			outBuffer.putInt(scanSize);
			outBuffer.putInt(scanStep);
			outBuffer.flip();
			this.socket.write(outBuffer);
			inBuffer.clear();
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

			int[] irData = new int[scanSize];
			int[] irData2 = new int[scanSize];
			int[] colorData = new int[scanSize];
			int[] colorData2 = new int[scanSize];
			readIntArray(irData);
			readIntArray(irData2);
			readIntArray(colorData);
			readIntArray(colorData2);

			return new ScanData(irData, colorData, irData2, colorData2);
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

}

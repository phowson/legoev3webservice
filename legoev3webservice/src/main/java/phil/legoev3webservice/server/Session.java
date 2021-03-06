package phil.legoev3webservice.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.NetworkMessageConstants;
import phil.legoev3webservice.control.AdvanceResults;
import phil.legoev3webservice.control.ContinuousScanData;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.RotateResult;
import phil.legoev3webservice.control.ScanData;

public class Session {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);
	private final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);
	private final int MAX_SIZE = 1024;
	private final ByteBuffer inboundBuffer = ByteBuffer.allocateDirect(MAX_SIZE);

	private final ByteBuffer outboundBuffer = ByteBuffer.allocateDirect(65536);

	private RobotControlServer server;
	private SocketChannel channel;
	private int currentLen = -1;
	private RobotController controller;

	public Session(RobotControlServer robotControlServer, SocketChannel c, RobotController controller) {
		this.channel = c;
		this.server = robotControlServer;
		this.controller = controller;
	}

	public void onReadable(SelectableChannel selectableChannel) throws IOException {
		if (this.channel != selectableChannel) {
			throw new RuntimeException("Bug in server class, called on wrong channel");
		}

		while (true) {
			if (currentLen == -1) {
				try {
					int r = this.channel.read(lenBuffer);
					if (r == -1) {
						logger.error("Connection " + selectableChannel + ", closed");
						selectableChannel.close();
						this.server.unregister(selectableChannel);
						return;
					}
					if (r == 0) {
						break;
					}
				} catch (IOException e) {
					logger.error("IO exception on channel " + selectableChannel + ", closing");

					selectableChannel.close();
					this.server.unregister(selectableChannel);
					return;
				}
				if (!lenBuffer.hasRemaining()) {
					lenBuffer.position(0);
					currentLen = lenBuffer.getInt();

					if (currentLen == 0 || currentLen > MAX_SIZE) {
						logger.error("Got an illegal length message from " + selectableChannel + ", closing");
						selectableChannel.close();
						this.server.unregister(selectableChannel);
						return;
					}

					inboundBuffer.position(0);
					inboundBuffer.limit(currentLen);
				}
			} else {

				try {
					int r = this.channel.read(inboundBuffer);
					if (r == -1) {
						logger.error("Connection " + selectableChannel + ", closed");
						selectableChannel.close();
						this.server.unregister(selectableChannel);
					}

					if (r <= 0) {
						break;
					}
					if (!inboundBuffer.hasRemaining()) {
						inboundBuffer.position(0);
						processMessage();
						inboundBuffer.clear();
						lenBuffer.clear();
						currentLen = -1;
					}
				} catch (IOException e) {
					logger.error("IO exception on channel " + selectableChannel + ", closing");
					selectableChannel.close();
					this.server.unregister(selectableChannel);
					return;
				}

			}
		}

	}

	/**
	 * 
	 */
	private void processMessage() {
		outboundBuffer.clear();
		logger.info("Got a new message, len =" + inboundBuffer.remaining());
		try {
			byte messageType = inboundBuffer.get();
			switch (messageType) {
			case NetworkMessageConstants.MSG_ADVANCE:
				onAdvance();
				break;
			case NetworkMessageConstants.MSG_REVERSE:
				onReverse();
				break;
			case NetworkMessageConstants.MSG_ROTATE:
				onRotate();
				break;

			case NetworkMessageConstants.MSG_SENSORARRAYMOVE:
				onSensorArrayMove();
				break;

			case NetworkMessageConstants.MSG_CONT_SCAN:
				onContScan();
				break;

			default:
				logger.error("Unexpected message type : " + messageType);

			}
		} catch (Exception ex) {
			logger.error("Unexpected exception while processing inbound message:", ex);
		}

	}

	private void onSensorArrayMove() throws IOException {
		int iclicks = inboundBuffer.getInt();
		controller.blockingSensorArrayMove(iclicks);
		outboundBuffer.position(0);
		outboundBuffer.putInt(iclicks);
		outboundBuffer.flip();
		while (outboundBuffer.hasRemaining()) {
			this.channel.write(outboundBuffer);
		}
	}

	private void onContScan() throws IOException {
		int scanClicks = inboundBuffer.getInt();
		ContinuousScanData results = controller.continuousScannerSweep(scanClicks);
		outboundBuffer.position(0);
		outboundBuffer.putInt(results.ultrasoundSensor.length * 8);
		write(outboundBuffer, results.steps);
		write(outboundBuffer, results.ultrasoundSensor);

		outboundBuffer.flip();
		while (outboundBuffer.hasRemaining()) {
			this.channel.write(outboundBuffer);
		}

	}

	private void write(ByteBuffer b, int[] irData) {
		for (int i : irData) {
			b.putInt(i);
		}

	}

	private void onRotate() throws IOException {
		int iclicks = inboundBuffer.getInt();
		RotateResult rotate = controller.rotate(iclicks);
		outboundBuffer.position(0);
		outboundBuffer.putInt(rotate.ticksRotated);
		ContinuousScanData results = rotate.scanData;
		outboundBuffer.putInt(results.ultrasoundSensor.length * 8);
		write(outboundBuffer, results.steps);
		write(outboundBuffer, results.ultrasoundSensor);
		outboundBuffer.flip();
		while (outboundBuffer.hasRemaining()) {
			this.channel.write(outboundBuffer);
		}

	}

	private void onReverse() throws IOException {
		int iclicks = inboundBuffer.getInt();
		int clicksMoved = controller.reverse(iclicks);
		outboundBuffer.position(0);
		outboundBuffer.putInt(clicksMoved);
		outboundBuffer.flip();
		while (outboundBuffer.hasRemaining()) {
			this.channel.write(outboundBuffer);
		}
	}

	private void onAdvance() throws IOException {
		int iclicks = inboundBuffer.getInt();
		AdvanceResults res = controller.advanceWithoutCollision(iclicks);
		outboundBuffer.position(0);
		outboundBuffer.putInt(res.clicksAdvancedLeft);
		outboundBuffer.putInt(res.clicksAdvancedRight);
		outboundBuffer.putInt(res.startProximity);
		outboundBuffer.putInt(res.endProximity);
		outboundBuffer.put((byte) (res.edgeSensor ? 1 : 0));
		outboundBuffer.put((byte) (res.pressed ? 1 : 0));
		outboundBuffer.flip();
		while (outboundBuffer.hasRemaining()) {
			this.channel.write(outboundBuffer);
		}
	}

}

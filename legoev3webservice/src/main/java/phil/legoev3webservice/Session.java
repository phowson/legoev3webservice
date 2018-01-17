package phil.legoev3webservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

public class Session {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Session.class);
	private final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);
	private final int MAX_SIZE = 1024;
	private final ByteBuffer inboundBuffer = ByteBuffer.allocateDirect(MAX_SIZE);

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

			}
		}

	}

	private void processMessage() {
		logger.info("Got a new message, len =" + inboundBuffer.remaining());
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
		case NetworkMessageConstants.MSG_SCAN:
			onScan();
			break;

		}

	}

	private void onScan() {
		// TODO Auto-generated method stub

	}

	private void onRotate() {
		// TODO Auto-generated method stub

	}

	private void onReverse() {
		// TODO Auto-generated method stub

	}

	private void onAdvance() {
		// TODO Auto-generated method stub

	}

}

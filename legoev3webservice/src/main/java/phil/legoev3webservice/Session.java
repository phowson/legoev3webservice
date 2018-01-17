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

	public Session(RobotControlServer robotControlServer, SocketChannel c) {
		this.channel = c;
		this.server = robotControlServer;
	}

	public void onReadable(SelectableChannel selectableChannel) throws IOException {
		if (this.channel != selectableChannel) {
			throw new RuntimeException("Bug in server class, called on wrong channel");
		}

		while (true) {
			if (currentLen == -1) {
				int r = this.channel.read(lenBuffer);
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
					}

					inboundBuffer.position(0);
					inboundBuffer.limit(currentLen);
				}
			} else {
				int r = this.channel.read(inboundBuffer);
				if (r == 0) {
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

	}

}

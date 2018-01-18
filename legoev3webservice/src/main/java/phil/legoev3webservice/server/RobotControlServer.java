package phil.legoev3webservice.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.control.RobotController;

public class RobotControlServer implements Closeable {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RobotControlServer.class);
	private final Selector sel;
	private ServerSocketChannel chan;
	private SelectionKey acceptKey;
	private volatile boolean stopped;
	private RobotController robotController;

	public RobotControlServer(RobotController robotController, int port) throws IOException {
		sel = Selector.open();
		chan = ServerSocketChannel.open().bind(new InetSocketAddress(port));
		chan.configureBlocking(false);
		this.robotController = robotController;
		acceptKey = chan.register(sel, SelectionKey.OP_ACCEPT);
	}

	public void run() throws IOException {
		// We can get away with this being blocking and single threaded as
		// you can't really get the robot to do more than one thing at once.
		logger.info("Starting server");
		while (!stopped) {
			try {
				int s = sel.select();
				if (s > 0) {
					checkSelector();
				}
			} catch (ClosedSelectorException ex) {
				logger.info("Selector closed. Exiting.");
				break;
			}

		}

	}

	private void checkSelector() throws IOException, ClosedChannelException {
		for (Iterator<SelectionKey> it = sel.selectedKeys().iterator(); it.hasNext();) {
			SelectionKey k = it.next();
			final SelectableChannel channel = k.channel();
			if (k.isValid()) {

				if (k.isAcceptable()) {
					ServerSocketChannel c2 = (ServerSocketChannel) channel;
					SocketChannel c = c2.accept();
					c.configureBlocking(false);
					c.register(sel, SelectionKey.OP_READ, new Session(this, c, robotController));
					logger.info("Accepting : " + c);
				} else if (k.isReadable()) {
					try {
						((Session) k.attachment()).onReadable(channel);
					} catch (Exception ex) {
						logger.error("Unexpected Exception on channel " + channel, ex);
						try {
							channel.close();
						} catch (Exception ex2) {
						}

						k.cancel();
					}
				} else {

					SocketChannel c = (SocketChannel) channel;
					logger.info("Closing and deregistering " + c);
					try {
						c.close();
					} catch (Exception ex2) {
					}
					k.cancel();
				}
			}

			it.remove();
		}
	}

	public void unregister(SelectableChannel selectableChannel) {
		SelectionKey k = selectableChannel.keyFor(sel);
		if (k != null)
			k.cancel();

	}

	public void close() throws IOException {
		stopped = true;
		acceptKey.cancel();
		Set<SelectionKey> z = new HashSet<>(sel.keys());
		for (SelectionKey k : z) {
			k.channel().close();
		}
		sel.close();

	}

}

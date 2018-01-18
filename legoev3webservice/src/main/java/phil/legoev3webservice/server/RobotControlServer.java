package phil.legoev3webservice.server;

import java.awt.Robot;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import phil.legoev3webservice.control.RobotController;

public class RobotControlServer {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RobotControlServer.class);
	private final Selector sel;
	private ServerSocketChannel chan;
	private SelectionKey acceptKey;
	private volatile boolean stopped;
	private RobotController robotController;

	public RobotControlServer(RobotController robotController) throws IOException {
		sel = Selector.open();
		chan = ServerSocketChannel.open().bind(new InetSocketAddress(5050));
		chan.configureBlocking(false);
		this.robotController = robotController;
		acceptKey = chan.register(sel, SelectionKey.OP_ACCEPT);
	}

	public void run() throws IOException {
		// We can get away with this being blocking and single threaded as
		// you can't really get the robot to do more than one thing at once.
		logger.info("Starting server");
		while (!stopped) {
			int s = sel.select();
			if (s > 0) {

				for (Iterator<SelectionKey> it = sel.selectedKeys().iterator(); it.hasNext();) {
					SelectionKey k = it.next();
					if (k.isAcceptable()) {
						ServerSocketChannel c2 = (ServerSocketChannel) k.channel();
						SocketChannel c = c2.accept();
						c.configureBlocking(false);
						c.register(sel, SelectionKey.OP_READ, new Session(this, c, robotController));
						logger.info("Accepting : " + c);
					} else if (k.isReadable()) {
						((Session) k.attachment()).onReadable(k.channel());
					} else {

						SocketChannel c = (SocketChannel) k.channel();
						logger.info("Closing and deregistering " + c);
						c.close();
						k.cancel();
					}

					it.remove();
				}

			}

		}

	}

	public void unregister(SelectableChannel selectableChannel) {
		SelectionKey k = selectableChannel.keyFor(sel);
		k.cancel();

	}

}

package phil.legoev3webservice;

import java.io.IOException;
import java.net.InetSocketAddress;

import phil.legoev3webservice.client.RobotClient;

public class GUIClient {

	private RobotClient client;

	public GUIClient(RobotClient robotClient) {
		this.client = robotClient;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		new GUIClient(new RobotClient(new InetSocketAddress(args[0], Integer.parseInt(args[1])))).run();
	}

	private void run() {
		this.client.advanceWithoutCollision(1000);
	}

}

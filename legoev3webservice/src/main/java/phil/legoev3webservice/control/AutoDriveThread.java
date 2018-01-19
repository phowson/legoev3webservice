package phil.legoev3webservice.control;

import phil.legoev3webservice.ai.AutoDriveController;
import phil.legoev3webservice.ai.PathListener;

public class AutoDriveThread extends Thread {

	private AutoDriveController controller;
	private PathListener listener;
	private volatile boolean stop;

	public AutoDriveThread(AutoDriveController controller, PathListener listener) {
		super("AutoDriver");
		setDaemon(false);
		this.controller = controller;
		this.listener = listener;
	}

	public void requestStop() {
		stop = true;
	}

	@Override
	public void run() {
		try {
			controller.initialise();
			listener.stateChanged();
			while (!stop) {
				if (controller.driveOneStep(listener).isEmpty()) {
					return;
				}

			}
		} catch (Exception ex) {

		}
	}

}

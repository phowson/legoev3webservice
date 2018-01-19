package phil.legoev3webservice.control;

import phil.legoev3webservice.ai.PathListener;
import phil.legoev3webservice.ai.SweepAreaController;

public class SweepAreaThread extends Thread {

	private SweepAreaController controller;
	private PathListener listener;
	private volatile boolean stop;

	public SweepAreaThread(SweepAreaController controller, PathListener listener) {
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
				if (!controller.driveOneStep(listener)) {
					return;
				}

			}
		} catch (Exception ex) {

		}
	}

}

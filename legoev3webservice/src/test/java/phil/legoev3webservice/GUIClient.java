package phil.legoev3webservice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import phil.legoev3webservice.ai.AStarAlgorithm;
import phil.legoev3webservice.ai.AutoDriveController;
import phil.legoev3webservice.ai.LinearisePath;
import phil.legoev3webservice.ai.PathListener;
import phil.legoev3webservice.client.RobotClient;
import phil.legoev3webservice.control.AutoDriveThread;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.StateUpdatingRobotController;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.MapImageRenderer;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class GUIClient implements PathListener {

	private RobotController contoller;
	private JFrame frame;
	private BufferedImage mapImage;
	private RobotState state;
	private EnvironmentMap map;
	private MapImageRenderer renderer = new MapImageRenderer();
	private final Executor bgExec = Executors.newSingleThreadExecutor();
	private AutoDriveController autoDriveController;
	private volatile List<Point> currentPath;
	private JLabel mapLabel;
	private volatile AutoDriveThread autoDriveThread;
	private JButton scanButton;
	private JButton advanceButton;
	private JButton rotateButton;
	private JButton reverseButton;
	private JButton trimButton;
	private JButton autoDriveOne;
	private JButton autoDrive;

	public GUIClient(RobotController controller, RobotState state, EnvironmentMap map,
			AutoDriveController autoDriveController) {
		this.state = state;
		this.map = map;
		this.contoller = controller;
		this.autoDriveController = autoDriveController;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		String path = App.class.getClassLoader().getResource("jul-log.properties").getFile();
		System.setProperty("java.util.logging.config.file", path);

		RobotClient client = new RobotClient(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
		RobotState state = new RobotState();
		state.x_CM = 500;
		state.y_CM = 500;
		EnvironmentMap map = new EnvironmentMap(1000);
		LinearisePath linearisePath = new LinearisePath(state, map, 80, RobotCalibration.SENSOR_INFINITY_POINT_CM);

		StateUpdatingRobotController controller = new StateUpdatingRobotController(client, state, map);
		AutoDriveController adc = new AutoDriveController(new AStarAlgorithm(state, map, 999, 500), controller, state,
				linearisePath);
		new GUIClient(controller, state, map, adc).run();
	}

	private void run() {

		mapImage = renderer.render(state, map, null, currentPath);

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel mapPanel = new JPanel();
		mapLabel = new JLabel(new ImageIcon(mapImage));
		mapPanel.add(mapLabel);
		JScrollPane mapScrollPane = new JScrollPane(mapPanel);
		mainPanel.add(mapScrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new FlowLayout());

		scanButton = new JButton("Scan");
		scanButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onScan();
			}
		});
		southPanel.add(scanButton);

		advanceButton = new JButton("Advance");
		advanceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onAdvance();
			}
		});
		southPanel.add(advanceButton);

		rotateButton = new JButton("Rotate");
		rotateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onRotate();
			}
		});
		southPanel.add(rotateButton);

		reverseButton = new JButton("Reverse");
		reverseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onReverse();
			}
		});

		southPanel.add(reverseButton);

		trimButton = new JButton("Trim sensors");
		trimButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onTrim();
			}
		});

		southPanel.add(trimButton);

		autoDriveOne = new JButton("Auto drive one step");
		autoDriveOne.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onAutoDriveOne();
			}
		});

		southPanel.add(autoDriveOne);

		autoDrive = new JButton("Auto drive");
		autoDrive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onAutoDrive();
			}
		});

		southPanel.add(autoDrive);

		JButton stopAutoDrive = new JButton("Stop");
		stopAutoDrive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onStop();
			}
		});

		southPanel.add(stopAutoDrive);

		JButton saveMap = new JButton("Save Map");
		saveMap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onSaveMap();
			}
		});

		southPanel.add(saveMap);

		mainPanel.add(southPanel, BorderLayout.SOUTH);

		frame = new JFrame("EV3 Robot Control Panel");
		frame.add(mainPanel);
		frame.setPreferredSize(new Dimension(1200, 800));
		frame.pack();
		frame.setVisible(true);

	}

	protected void onSaveMap() {

		try {
			EnvironmentMap.save(JOptionPane.showInputDialog("File name"), map);
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
		}
	}

	protected void onStop() {
		autoDriveThread.requestStop();
		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				try {
					autoDriveThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				autoDriveThread = null;

				setControlsEnabled(true);
			}
		});

	}

	protected void setControlsEnabled(boolean b) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				scanButton.setEnabled(b);
				advanceButton.setEnabled(b);
				rotateButton.setEnabled(b);
				reverseButton.setEnabled(b);
				trimButton.setEnabled(b);
				autoDriveOne.setEnabled(b);
				autoDrive.setEnabled(b);
			}
		});
	}

	protected void onAutoDrive() {
		if (autoDriveThread == null) {
			setControlsEnabled(false);
			autoDriveThread = new AutoDriveThread(autoDriveController, this);
			autoDriveThread.start();
		}
	}

	protected void onAutoDriveOne() {
		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				try {
					autoDriveController.driveOneStep(GUIClient.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	protected void onTrim() {
		final double trim = Double.parseDouble(JOptionPane.showInputDialog(frame, "Trim by how many clicks?"));

		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				contoller.blockingSensorArrayMove((int) Math.round(trim));
				updateGui();
			}
		});
	}

	protected void onReverse() {
		final double qtyRev = Double.parseDouble(JOptionPane.showInputDialog(frame, "Reverse how many centimeters?"));

		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				contoller.reverse((int) Math.round(qtyRev * RobotCalibration.MOVE_CLICKS_PER_CM));
				updateGui();
			}
		});

	}

	protected void updateGui() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				renderer.render(state, map, mapImage, currentPath);
				mapLabel.repaint();
				frame.repaint();
			}
		});
	}

	protected void onRotate() {
		final double qtyRev = Double.parseDouble(JOptionPane.showInputDialog(frame, "Rotate how many degrees?"));

		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				contoller.rotate((int) Math.round(qtyRev * RobotCalibration.ROTATE_CLICKS_PER_DEGREE));
				updateGui();
			}
		});
	}

	protected void onAdvance() {
		final double qtyRev = Double.parseDouble(JOptionPane.showInputDialog(frame, "Advance how many centimeters?"));

		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				contoller.advanceWithoutCollision((int) Math.round(qtyRev * RobotCalibration.MOVE_CLICKS_PER_CM));
				updateGui();
			}
		});
	}

	protected void onScan() {
		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				try {
					contoller.fullScannerSweep(RobotCalibration.SCAN_ITERS, RobotCalibration.SCAN_CLICKS_PER_ITER);
					updateGui();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onNewPath(List<Point> path) {
		currentPath = path;
		updateGui();
	}

	@Override
	public void stateChanged() {

		updateGui();
	}

}

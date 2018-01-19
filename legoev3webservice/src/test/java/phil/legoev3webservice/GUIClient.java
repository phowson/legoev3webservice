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

	public GUIClient(RobotClient robotClient, RobotState state, EnvironmentMap map,
			AutoDriveController autoDriveController) {
		this.state = state;
		this.map = map;
		this.contoller = new StateUpdatingRobotController(robotClient, state, map);
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
		LinearisePath linearisePath = new LinearisePath(state, map, 50, 30);
		AutoDriveController adc = new AutoDriveController(new AStarAlgorithm(state, map, 999, 500), client, state,
				linearisePath);
		new GUIClient(client, state, map, adc).run();
	}

	private void run() {

		mapImage = renderer.render(state, map, null, currentPath);

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel mapPanel = new JPanel();
		JLabel mapLabel = new JLabel(new ImageIcon(mapImage));
		mapPanel.add(mapLabel);
		JScrollPane mapScrollPane = new JScrollPane(mapPanel);
		mainPanel.add(mapScrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new FlowLayout());

		JButton scanButton = new JButton("Scan");
		scanButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onScan();
			}
		});
		southPanel.add(scanButton);

		JButton advanceButton = new JButton("Advance");
		advanceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onAdvance();
			}
		});
		southPanel.add(advanceButton);

		JButton rotateButton = new JButton("Rotate");
		rotateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onRotate();
			}
		});
		southPanel.add(rotateButton);

		JButton reverseButton = new JButton("Reverse");
		reverseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onReverse();
			}
		});

		southPanel.add(reverseButton);

		JButton trimButton = new JButton("Trim sensors");
		trimButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onTrim();
			}
		});

		southPanel.add(trimButton);

		JButton autoDriveOne = new JButton("Auto drive one step");
		autoDriveOne.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onAutoDriveOne();
			}
		});

		southPanel.add(autoDriveOne);

		JButton autoDrive = new JButton("Auto drive");
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
		// TODO Auto-generated method stub

	}

	protected void onAutoDrive() {
		// TODO Auto-generated method stub

	}

	protected void onAutoDriveOne() {
		this.autoDriveController.driveOneStep(this);

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
				renderer.render(state, map, mapImage, null);
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
		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				currentPath = path;
				updateGui();
			}
		});
	}

	@Override
	public void stateChanged() {
		bgExec.execute(new Runnable() {

			@Override
			public void run() {
				updateGui();
			}
		});
	}

}

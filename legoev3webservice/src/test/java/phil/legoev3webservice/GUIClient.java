package phil.legoev3webservice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
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

import phil.legoev3webservice.client.RobotClient;
import phil.legoev3webservice.control.RobotController;
import phil.legoev3webservice.control.StateUpdatingRobotController;
import phil.legoev3webservice.map.EnvironmentMap;
import phil.legoev3webservice.map.MapImageRenderer;
import phil.legoev3webservice.map.RobotState;
import phil.legoev3webservice.robot.RobotCalibration;

public class GUIClient {

	private RobotController contoller;
	private JFrame frame;
	private BufferedImage mapImage;
	private RobotState state;
	private EnvironmentMap map;
	private MapImageRenderer renderer = new MapImageRenderer();
	private final Executor bgExec = Executors.newSingleThreadExecutor();

	public GUIClient(RobotClient robotClient, RobotState state, EnvironmentMap map) {
		this.state = state;
		this.map = map;
		this.contoller = new StateUpdatingRobotController(robotClient, state, map);
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		String path = App.class.getClassLoader().getResource("jul-log.properties").getFile();
		System.setProperty("java.util.logging.config.file", path);
		RobotState state = new RobotState();
		state.x_CM = 500;
		state.y_CM = 500;
		new GUIClient(new RobotClient(new InetSocketAddress(args[0], Integer.parseInt(args[1]))), state,
				new EnvironmentMap(1000)).run();
	}

	private void run() {

		mapImage = renderer.render(state, map, null);

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

		mainPanel.add(southPanel, BorderLayout.SOUTH);

		frame = new JFrame("EV3 Robot Control Panel");
		frame.add(mainPanel);
		frame.setPreferredSize(new Dimension(800, 800));
		frame.pack();
		frame.setVisible(true);

	}

	protected void onAutoDrive() {
		// TODO Auto-generated method stub

	}

	protected void onAutoDriveOne() {
		// TODO Auto-generated method stub

	}

	protected void onTrim() {

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
				renderer.render(state, map, mapImage);
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

}

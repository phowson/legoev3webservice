package phil.legoev3webservice.control;

import org.ev3dev.hardware.motors.Motor;
import org.ev3dev.hardware.ports.LegoPort;
import org.ev3dev.hardware.sensors.ColorSensor;
import org.ev3dev.hardware.sensors.InfraredSensor;
import org.ev3dev.hardware.sensors.TouchSensor;
import org.ev3dev.hardware.sensors.UltrasonicSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TIntArrayList;
import phil.legoev3webservice.robot.RobotCalibration;

public class LocalRobotController implements RobotController {
	private static final int SENSOR_MOTOR_SPEED = 500;
	private static final Logger logger = LoggerFactory.getLogger(LocalRobotController.class);
	private static final int MAIN_MOTOR_SPEED = 100;
	private static final int MAIN_MOTOR_SPEED2 = 500;
	private static final int MAIN_MOTOR_SPEED_ROTATE = 66;
	private static final String POSITION = "position";
	private static final long PAUSE_MILLIS = 150;

	private UltrasonicSensor movableSensor = new UltrasonicSensor(new LegoPort(LegoPort.INPUT_1));
	private InfraredSensor irSensor = new InfraredSensor(new LegoPort(LegoPort.INPUT_2));
	private ColorSensor colorSensor = new ColorSensor(new LegoPort(LegoPort.INPUT_4));
	private Motor sensorArrayMotor = new Motor(new LegoPort(LegoPort.OUTPUT_C));
	private TouchSensor touchSensor = new TouchSensor(new LegoPort(LegoPort.INPUT_3));

	private Motor leftMotor = new Motor(new LegoPort(LegoPort.OUTPUT_A));
	private Motor rightMotor = new Motor(new LegoPort(LegoPort.OUTPUT_B));

	public LocalRobotController() {
		resetSensorMotor();
		movableSensor.setMode(UltrasonicSensor.SYSFS_CM_MODE);
		irSensor.setMode(InfraredSensor.SYSFS_PROXIMITY_REQUIRED_MODE);
		colorSensor.setMode(ColorSensor.SYSFS_REFLECTED_LIGHT_INTENSITY_MODE);

	}

	private void resetSensorMotor() {
		sensorArrayMotor.reset();
		sensorArrayMotor.setStopAction("brake");
		sensorArrayMotor.setSpeed_SP(SENSOR_MOTOR_SPEED);
	}

	public RotateResult rotate(int iclicks) {
		setupMainMotors(true);

		int clicks;
		if (iclicks > 0) {
			clicks = iclicks;
			leftMotor.setPolarity("normal");
			rightMotor.setPolarity("inversed");
		} else {
			clicks = -iclicks;
			leftMotor.setPolarity("inversed");
			rightMotor.setPolarity("normal");
		}
		boolean originallyTriggered = edgeSensorsTriggered();
		int initalPosL = getLeftMotorPosition();
		int initalPosR = getRightMotorPosition();

		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);

		leftMotor.runToRelPos();
		rightMotor.runToRelPos();
		TIntArrayList clickData = new TIntArrayList();
		TIntArrayList irData = new TIntArrayList();
		int initialPosition = getLeftMotorPosition();

		while (motorsRunning()) {
			if (touchSensor.isPressed() || (edgeSensorsTriggered() && !originallyTriggered)) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			}
			clicks = getLeftMotorPosition() - initialPosition;
			if (iclicks < 0) {
				clicks = -clicks;
			}
			clickData.add(clicks);
			irData.add((int) movableSensor.getDistanceCentimeters());

		}
		// Sleep breifly to allow motors to fully stop turning
		try {
			Thread.sleep(PAUSE_MILLIS);
		} catch (InterruptedException e) {
		}

		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return new RotateResult(new ContinuousScanData(clickData.toArray(), irData.toArray()), d);
	}

	private boolean edgeSensorsTriggered() {
		return irSensor.getProximity() < 2
				|| colorSensor.getReflectedLightIntensity() > RobotCalibration.SENSOR_COLOR_STOP;
	}

	private void setupMainMotors(boolean slow) {
		leftMotor.reset();
		rightMotor.reset();
		if (slow) {
			leftMotor.setSpeed_SP(MAIN_MOTOR_SPEED_ROTATE);
			rightMotor.setSpeed_SP(MAIN_MOTOR_SPEED_ROTATE);
		} else {
			leftMotor.setSpeed_SP(MAIN_MOTOR_SPEED2);
			rightMotor.setSpeed_SP(MAIN_MOTOR_SPEED2);
		}
		leftMotor.setStopAction("hold");
		rightMotor.setStopAction("hold");
	}

	public int reverse(int clicks) {
		setupMainMotors(true);
		leftMotor.setPolarity("inversed");
		rightMotor.setPolarity("inversed");

		int initalPosL = getLeftMotorPosition();
		int initalPosR = getRightMotorPosition();
		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);
		leftMotor.runToRelPos();
		rightMotor.runToRelPos();

		waitMotorTermination();
		// Sleep breifly to allow motors to fully stop turning
		try {
			Thread.sleep(PAUSE_MILLIS);
		} catch (InterruptedException e) {
		}
		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return d;
	}

	private void waitMotorTermination() {
		while (motorsRunning()) {
			Thread.yield();
		}
	}

	private boolean motorsRunning() {
		return leftMotor.getStateViaString().contains("running") || rightMotor.getStateViaString().contains("running");
	}

	private int getRightMotorPosition() {
		return Integer.parseInt(rightMotor.getAttribute(POSITION));
	}

	private int getLeftMotorPosition() {
		return Integer.parseInt(leftMotor.getAttribute(POSITION));
	}

	public AdvanceResults advanceWithoutCollision(int clicks) {

		setupMainMotors(false);
		leftMotor.setPolarity("normal");
		rightMotor.setPolarity("normal");

		int initalPosL = getLeftMotorPosition();
		int initalPosR = getRightMotorPosition();

		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);

		int prox = (int) movableSensor.getDistanceCentimeters();
		int startProx = prox;

		if (prox > RobotCalibration.ULTRASOUND_COLLISION_DISTANCE && !edgeSensorsTriggered() && !touchSensor.isPressed()) {
			leftMotor.runToRelPos();
			rightMotor.runToRelPos();
			while (motorsRunning()) {

				// Querying the sensor takes a bit of time, so act as soon as we
				// have a value
				if (movableSensor.getDistanceCentimeters() < RobotCalibration.ULTRASOUND_COLLISION_DISTANCE) {
					leftMotor.stop();
					rightMotor.stop();
					logger.info("Stopping due to collision");
					break;
				}

				// Querying the sensor takes a bit of time, so act as soon as we
				// have a value
				if (edgeSensorsTriggered() || touchSensor.isPressed()) {
					leftMotor.stop();
					rightMotor.stop();
					if (touchSensor.isPressed()) {
						logger.info("Stopping due to collision");
					} else {
						logger.info("Stopping due to color sensor");
					}
					break;
				}

			}

			// Sleep breifly to allow motors to fully stop turning
			try {
				Thread.sleep(PAUSE_MILLIS);
			} catch (InterruptedException e) {
			}
		}
		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		return new AdvanceResults((finalPosL - initalPosL), (finalPosR - initalPosR), startProx,
				(int) movableSensor.getDistanceCentimeters(), edgeSensorsTriggered(),
				touchSensor.isPressed());

	}

	private void contSensorSweep(int clicks, TIntArrayList clickData, TIntArrayList irData) {

		sensorArrayMotor.setPosition_SP(clicks);
		sensorArrayMotor.runToAbsPos();

		while (sensorArrayMotor.getStateViaString().contains("running")) {
			clickData.add((getSensorArrayPosition()));
			irData.add((int) movableSensor.getDistanceCentimeters());
		}
		// Sleep breifly to allow motors to fully stop turning
		try {
			Thread.sleep(PAUSE_MILLIS);
		} catch (InterruptedException e) {
		}

	}

	private int getSensorArrayPosition() {
		return Integer.parseInt(sensorArrayMotor.getAttribute(POSITION));
	}

	public void blockingSensorArrayMove(int target) {
		logger.info("Got sensor array move : " + target + " clicks");
		if (target > 0) {
			sensorArrayMotor.setPolarity("normal");
		} else {
			logger.info("Inverse polarity");
			sensorArrayMotor.setPolarity("inversed");
			target = -target;
		}
		sensorArrayMotor.setPosition_SP(target);
		sensorArrayMotor.runToRelPos();

		while (sensorArrayMotor.getStateViaString().contains("running")) {
			Thread.yield();
		}
		resetSensorMotor();
		logger.info("Move complete");
	}

	@Override
	public ContinuousScanData continuousScannerSweep(int scanSteps) {
		TIntArrayList clickData = new TIntArrayList();
		TIntArrayList irData = new TIntArrayList();
		sensorArrayMotor.setPolarity("normal");

		int halfClicks = scanSteps / 2;
		contSensorSweep(-halfClicks, clickData, irData);
		contSensorSweep(halfClicks, clickData, irData);
		contSensorSweep(0, clickData, irData);

		return new ContinuousScanData(clickData.toArray(), irData.toArray());
	}

}

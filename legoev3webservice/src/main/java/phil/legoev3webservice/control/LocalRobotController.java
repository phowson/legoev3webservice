package phil.legoev3webservice.control;

import org.ev3dev.hardware.motors.Motor;
import org.ev3dev.hardware.ports.LegoPort;
import org.ev3dev.hardware.sensors.ColorSensor;
import org.ev3dev.hardware.sensors.InfraredSensor;
import org.ev3dev.hardware.sensors.TouchSensor;
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
	private static final long PAUSE_MILLIS = 250;
	private InfraredSensor irSensor = new InfraredSensor(new LegoPort(LegoPort.INPUT_1));
	private ColorSensor colorSensor = new ColorSensor(new LegoPort(LegoPort.INPUT_2));
	private Motor sensorArrayMotor = new Motor(new LegoPort(LegoPort.OUTPUT_C));
	private TouchSensor touchSensor = new TouchSensor(new LegoPort(LegoPort.INPUT_3));

	private Motor leftMotor = new Motor(new LegoPort(LegoPort.OUTPUT_A));
	private Motor rightMotor = new Motor(new LegoPort(LegoPort.OUTPUT_B));

	public LocalRobotController() {
		resetSensorMotor();
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

		int initalPosL = getLeftMotorPosition();
		int initalPosR = getRightMotorPosition();

		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);

		leftMotor.runToRelPos();
		rightMotor.runToRelPos();
		TIntArrayList clickData = new TIntArrayList();
		TIntArrayList irData = new TIntArrayList();
		int initialPosition = leftMotor.getPosition();
		while (motorsRunning()) {
			if (touchSensor.isPressed()) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			}
			clicks = leftMotor.getPosition() - initialPosition;
			if (iclicks < 0) {
				clicks = -clicks;
			}
			clickData.add(clicks);
			irData.add(irSensor.getProximity());

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
		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;
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

		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;

		int prox = irSensor.getProximity();
		int intensity = colorSensor.getReflectedLightIntensity();
		int startProx = prox;

		if (prox > 0 && intensity < 10 && !touchSensor.isPressed()) {
			leftMotor.runToRelPos();
			rightMotor.runToRelPos();
			boolean slow = false;
			while (motorsRunning()) {

				// Querying the sensor takes a bit of time, so act as soon as we
				// have a value
				prox = irSensor.getProximity();
				if (prox == 0) {
					leftMotor.stop();
					rightMotor.stop();
					break;
				}

				if (!slow) {
					int lp = getLeftMotorPosition();
					int rp = getRightMotorPosition();
					if (prox < 5 || leftTarget - lp < RobotCalibration.MOVE_CLICKS_PER_CM * 5
							|| rightTarget - rp < RobotCalibration.MOVE_CLICKS_PER_CM * 5) {
						leftMotor.setSpeed_SP(MAIN_MOTOR_SPEED);
						rightMotor.setSpeed_SP(MAIN_MOTOR_SPEED);
					}

				}

				intensity = colorSensor.getReflectedLightIntensity();
				// Querying the sensor takes a bit of time, so act as soon as we
				// have a value
				if (intensity > 10 || touchSensor.isPressed()) {
					leftMotor.stop();
					rightMotor.stop();
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

		return new AdvanceResults((finalPosL - initalPosL), (finalPosR - initalPosR), startProx, prox,
				colorSensor.getReflectedLightIntensity(), touchSensor.isPressed());

	}

	private void contSensorSweep(int clicks, int startVal, int multiplier, TIntArrayList clickData,
			TIntArrayList irData) {

		int startPos = getSensorArrayPosition();
		sensorArrayMotor.setPosition_SP(clicks);
		sensorArrayMotor.runToAbsPos();

		while (sensorArrayMotor.getStateViaString().contains("running")) {
			clickData.add((getSensorArrayPosition() - startPos) * multiplier + startVal);
			irData.add(irSensor.getProximity());
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

		contSensorSweep(-scanSteps / 2, 0, 1, clickData, irData);
		contSensorSweep(scanSteps, clickData.get(clickData.size() - 1), -1, clickData, irData);
		contSensorSweep(0, clickData.get(clickData.size() - 1), 1, clickData, irData);

		return new ContinuousScanData(clickData.toArray(), irData.toArray());
	}

}

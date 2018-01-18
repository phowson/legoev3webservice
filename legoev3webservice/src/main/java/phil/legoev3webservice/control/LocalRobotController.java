package phil.legoev3webservice.control;

import org.ev3dev.hardware.motors.Motor;
import org.ev3dev.hardware.ports.LegoPort;
import org.ev3dev.hardware.sensors.ColorSensor;
import org.ev3dev.hardware.sensors.InfraredSensor;
import org.ev3dev.hardware.sensors.TouchSensor;

public class LocalRobotController implements RobotController {
	private static final int MAIN_MOTOR_SPEED = 100;
	private static final String POSITION = "position";
	private InfraredSensor irSensor = new InfraredSensor(new LegoPort(LegoPort.INPUT_1));
	private ColorSensor colorSensor = new ColorSensor(new LegoPort(LegoPort.INPUT_2));
	private Motor sensorArrayMotor = new Motor(new LegoPort(LegoPort.OUTPUT_C));
	private TouchSensor touchSensor = new TouchSensor(new LegoPort(LegoPort.INPUT_3));

	private Motor leftMotor = new Motor(new LegoPort(LegoPort.OUTPUT_A));
	private Motor rightMotor = new Motor(new LegoPort(LegoPort.OUTPUT_B));

	public int rotate(int iclicks) {
		setupMainMotors();

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

		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;

		leftMotor.runToRelPos();
		rightMotor.runToRelPos();

		while (getLeftMotorPosition() < leftTarget || getRightMotorPosition() < rightTarget) {
			if (touchSensor.isPressed()) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			}
		}
		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return d;
	}

	private void setupMainMotors() {
		leftMotor.reset();
		leftMotor.setStopAction("brake");
		leftMotor.setSpeed_SP(MAIN_MOTOR_SPEED);

		rightMotor.reset();
		rightMotor.setStopAction("brake");
		rightMotor.setSpeed_SP(MAIN_MOTOR_SPEED);
	}

	public int reverse(int clicks) {
		setupMainMotors();

		int initalPosL = getLeftMotorPosition();
		int initalPosR = getRightMotorPosition();
		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;
		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);
		leftMotor.runToRelPos();
		rightMotor.runToRelPos();
		while (getLeftMotorPosition() < leftTarget || getRightMotorPosition() < rightTarget) {
			Thread.yield();
		}
		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return d;
	}

	private int getRightMotorPosition() {
		return Integer.parseInt(rightMotor.getAttribute(POSITION));
	}

	private int getLeftMotorPosition() {
		return Integer.parseInt(leftMotor.getAttribute(POSITION));
	}

	public AdvanceResults advanceWithoutCollision(int clicks) {

		setupMainMotors();

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
			while (getLeftMotorPosition() < leftTarget || getRightMotorPosition() < rightTarget) {

				// Querying the sensor takes a bit of time, so act as soon as we
				// have a value
				prox = irSensor.getProximity();
				if (prox == 0) {
					leftMotor.stop();
					rightMotor.stop();
					break;
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
		}
		int finalPosL = getLeftMotorPosition();
		int finalPosR = getRightMotorPosition();

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return new AdvanceResults(d, startProx, prox, colorSensor.getReflectedLightIntensity(),
				touchSensor.isPressed());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see phil.legoev3webservice.IRobotController#fullScannerSweep(int, int)
	 */
	public ScanData fullScannerSweep(int scanSize, int scanStep) throws InterruptedException {
		int halfScan = scanSize / 2;
		sensorArrayMotor.reset();

		int[] irData = new int[scanSize];
		int[] irData2 = new int[scanSize];

		int[] colorData = new int[scanSize];
		int[] colorData2 = new int[scanSize];

		sensorArrayMotor.setPolarity("normal");
		sensorSweep(halfScan, irData, colorData, halfScan, 1, scanStep);
		sensorArrayMotor.setPolarity("inversed");
		sensorSweep(scanSize, irData2, colorData2, scanSize - 1, -1, scanStep);
		sensorArrayMotor.setPolarity("normal");
		sensorSweep(halfScan, irData, colorData, 0, 1, scanStep);

		return new ScanData(irData, colorData, irData2, colorData2);

	}

	private void sensorSweep(int steps, int[] irData, int[] colorData, int startIdx, int incr, int sensorScanStep)
			throws InterruptedException {
		irSensor.setMode("IR-PROX");
		colorSensor.setMode(ColorSensor.SYSFS_REFLECTED_LIGHT_INTENSITY_MODE);
		sensorArrayMotor.setStopAction("brake");
		sensorArrayMotor.setSpeed_SP(200);

		int idx = startIdx;

		int pos = getSensorArrayPosition();
		int target = pos;
		for (int i = 0; i < steps; ++i) {
			irData[idx] = irSensor.getProximity();
			colorData[idx] = colorSensor.getReflectedLightIntensity();

			target += sensorScanStep;
			blockingSensorArrayMoveImpl(target);
			idx += incr;
		}
	}

	private void blockingSensorArrayMoveImpl(int target) {
		sensorArrayMotor.setPosition_SP(target - getSensorArrayPosition());
		sensorArrayMotor.runToRelPos();
		while (getSensorArrayPosition() < target) {
			Thread.yield();
		}
	}

	private int getSensorArrayPosition() {
		return Integer.parseInt(sensorArrayMotor.getAttribute(POSITION));
	}

	public void blockingSensorArrayMove(int target) {
		sensorArrayMotor.reset();
		if (target > 0) {
			sensorArrayMotor.setPolarity("normal");
		} else {
			sensorArrayMotor.setPolarity("inversed");
		}
		blockingSensorArrayMoveImpl(target);
	}

}

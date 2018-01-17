package phil.legoev3webservice;

import java.util.Arrays;

import org.ev3dev.hardware.motors.Motor;
import org.ev3dev.hardware.ports.LegoPort;
import org.ev3dev.hardware.sensors.ColorSensor;
import org.ev3dev.hardware.sensors.InfraredSensor;
import org.ev3dev.hardware.sensors.TouchSensor;

/**
 * Hello world!
 *
 */
public class App {
	private static final String POSITION = "position";
	private InfraredSensor irSensor = new InfraredSensor(new LegoPort(LegoPort.INPUT_1));
	private ColorSensor colorSensor = new ColorSensor(new LegoPort(LegoPort.INPUT_2));
	private Motor sensorArrayMotor = new Motor(new LegoPort(LegoPort.OUTPUT_C));
	private TouchSensor touchSensor = new TouchSensor(new LegoPort(LegoPort.INPUT_3));

	private Motor leftMotor = new Motor(new LegoPort(LegoPort.OUTPUT_A));
	private Motor rightMotor = new Motor(new LegoPort(LegoPort.OUTPUT_B));

	public static void main(String[] args) throws InterruptedException {
		new App().run();

	}

	private int rotate(int iclicks) {
		leftMotor.reset();
		leftMotor.setStopAction("brake");
		leftMotor.setSpeed_SP(100);

		rightMotor.reset();
		rightMotor.setStopAction("brake");
		rightMotor.setSpeed_SP(100);

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

		int initalPosL = Integer.parseInt(leftMotor.getAttribute(POSITION));
		int initalPosR = Integer.parseInt(rightMotor.getAttribute(POSITION));

		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);

		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;

		leftMotor.runToRelPos();
		rightMotor.runToRelPos();

		while (Integer.parseInt(leftMotor.getAttribute(POSITION)) < leftTarget
				|| Integer.parseInt(rightMotor.getAttribute(POSITION)) < rightTarget) {
			Thread.yield();
		}
		int finalPosL = Integer.parseInt(leftMotor.getAttribute(POSITION));
		int finalPosR = Integer.parseInt(rightMotor.getAttribute(POSITION));

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return d;
	}

	private int advanceWithoutCollision(int clicks) {

		leftMotor.reset();
		leftMotor.setStopAction("brake");
		leftMotor.setSpeed_SP(100);

		rightMotor.reset();
		rightMotor.setStopAction("brake");
		rightMotor.setSpeed_SP(100);

		int initalPosL = Integer.parseInt(leftMotor.getAttribute(POSITION));
		int initalPosR = Integer.parseInt(rightMotor.getAttribute(POSITION));

		leftMotor.setPosition_SP(clicks);
		rightMotor.setPosition_SP(clicks);

		int leftTarget = initalPosL + clicks;
		int rightTarget = initalPosR + clicks;

		int prox = irSensor.getProximity();
		int intensity = colorSensor.getReflectedLightIntensity();

		if (prox > 0 && intensity < 10 && !touchSensor.isPressed()) {
			leftMotor.runToRelPos();
			rightMotor.runToRelPos();
			while (Integer.parseInt(leftMotor.getAttribute(POSITION)) < leftTarget
					|| Integer.parseInt(rightMotor.getAttribute(POSITION)) < rightTarget) {

				prox = irSensor.getProximity();
				intensity = colorSensor.getReflectedLightIntensity();

				if (prox == 0 || intensity > 10 || touchSensor.isPressed()) {
					leftMotor.stop();
					rightMotor.stop();
					break;
				}

			}
		}
		int finalPosL = Integer.parseInt(leftMotor.getAttribute(POSITION));
		int finalPosR = Integer.parseInt(rightMotor.getAttribute(POSITION));

		int d = ((finalPosL - initalPosL) + (finalPosR - initalPosR)) / 2;
		return d;

	}

	private void run() throws InterruptedException {

		fullSweep();

		int dist = rotate(500);
		System.out.println("Total distance travelled : " + dist);

		dist = rotate(-500);
		System.out.println("Total distance travelled : " + dist);

		dist = advanceWithoutCollision(1000);
		System.out.println("Total distance travelled : " + dist);

	}

	private void fullSweep() throws InterruptedException {
		sensorArrayMotor.reset();

		sensorArrayMotor.setPolarity("normal");
		sensorSweep(40);
		sensorArrayMotor.setPolarity("inversed");
		sensorSweep(80);
		sensorArrayMotor.setPolarity("normal");
		sensorSweep(40);
	}

	private void sensorSweep(int steps) throws InterruptedException {
		irSensor.setMode("IR-PROX");
		colorSensor.setMode(ColorSensor.SYSFS_REFLECTED_LIGHT_INTENSITY_MODE);
		sensorArrayMotor.setStopAction("brake");
		sensorArrayMotor.setSpeed_SP(300);

		int pos = getSensorArrayPosition();
		int target = pos;
		for (int i = 0; i < steps; ++i) {

			int prox = irSensor.getProximity();
			System.out.println("Proximity = " + prox + "%");
			System.out.println("Approx = " + 70.0 * (prox / 100.0) + "cm");

			System.out.println("Light intensity = " + colorSensor.getReflectedLightIntensity());

			target += 60;
			blockingSensorArrayMove(target);

		}
	}

	private void blockingSensorArrayMove(int target) {
		sensorArrayMotor.setPosition_SP(target- getSensorArrayPosition());
		sensorArrayMotor.runToRelPos();
		while (getSensorArrayPosition() < target) {
			Thread.yield();
		}
	}

	private int getSensorArrayPosition() {
		return Integer.parseInt(sensorArrayMotor.getAttribute(POSITION));
	}
}

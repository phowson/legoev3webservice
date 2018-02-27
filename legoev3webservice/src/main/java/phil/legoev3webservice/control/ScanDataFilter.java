package phil.legoev3webservice.control;

import java.awt.Robot;
import java.util.Arrays;

import phil.legoev3webservice.robot.RobotCalibration;

public class ScanDataFilter {

	public FilteredSensorData filter(ScanData data) {
		double[] d = new double[6];
		double[] d2 = new double[4];

		FilteredSensorData out = new FilteredSensorData(new double[data.ultrasoundData.length], new double[data.ultrasoundData.length]);
		double heading = 90;
		for (int i = 0; i < data.ultrasoundData.length; ++i) {
			double dst;
			if (i == 0) {
				d2[0] = data.ultrasoundData[i];
				d2[1] = data.ultrasoundData2[i];
				d2[2] = data.ultrasoundData[i + 1];
				d2[3] = data.ultrasoundData2[i + 1];

				Arrays.sort(d2);

				dst = interpolate(0.5 * (d2[1] + d2[2]));

			} else if (i == data.ultrasoundData.length - 1) {
				d2[0] = data.ultrasoundData[i];
				d2[1] = data.ultrasoundData2[i];
				d2[2] = data.ultrasoundData[i - 1];
				d2[3] = data.ultrasoundData2[i - 1];
				Arrays.sort(d2);
				out.headings[i] = heading;
				dst = interpolate(0.5 * (d2[1] + d2[2]));
			} else {
				d[0] = data.ultrasoundData[i];
				d[1] = data.ultrasoundData2[i];
				d[2] = data.ultrasoundData[i - 1];
				d[3] = data.ultrasoundData2[i - 1];
				d[4] = data.ultrasoundData[i + 1];
				d[5] = data.ultrasoundData2[i + 1];

				Arrays.sort(d);

				out.headings[i] = heading;
				dst = interpolate(0.25 * (d[1] + d[2] + d[3] + d[4]));
			}

			if (dst >= RobotCalibration.SENSOR_INFINITY_POINT_CM) {
				dst = Double.POSITIVE_INFINITY;
			}
			out.headings[i] = heading;
			out.distance_CM[i] = dst;

			heading -= RobotCalibration.SCAN_DEGREES_PER_VALUE;
		}

		return out;

	}

	public static void main(String[] args) {
		System.out.println(interpolate(1));
	}

	public static double interpolate(double pc) {
		for (int i = 1; i < RobotCalibration.SENSOR_CALIBRATION_CM.length; ++i) {
			if (RobotCalibration.SENSOR_CALIBRATION_PC[i] >= pc) {
				double prev = RobotCalibration.SENSOR_CALIBRATION_PC[i - 1];
				double z = pc - prev;
				double d = RobotCalibration.SENSOR_CALIBRATION_PC[i] - prev;
				double dC = RobotCalibration.SENSOR_CALIBRATION_CM[i] - RobotCalibration.SENSOR_CALIBRATION_CM[i - 1];
				return dC * (z / d) + RobotCalibration.SENSOR_CALIBRATION_CM[i - 1];
			}

		}
		return Double.POSITIVE_INFINITY;
	}

	public FilteredSensorData filter(ContinuousScanData data) {
		return filterImpl(data, -RobotCalibration.SCAN_DEGREES_PER_CLICK);
	}

	private FilteredSensorData filterImpl(ContinuousScanData data, double mult) {
		double[] headings = new double[data.steps.length];
		double[] distance_CM = new double[data.steps.length];
		for (int i = 0; i < headings.length; ++i) {
			headings[i] = data.steps[i] * mult;
			double dst = interpolate(data.ultrasoundSensor[i]);
			if (dst >= RobotCalibration.SENSOR_INFINITY_POINT_CM) {
				dst = Double.POSITIVE_INFINITY;
			}
			distance_CM[i] = dst;
		}

		FilteredSensorData out = new FilteredSensorData(headings, distance_CM);
		return out;
	}

	public FilteredSensorData filter(RotateResult results) {
		return filterImpl(results.scanData, RobotCalibration.ROTATE_DEGREES_PER_CLICK);
	}

}
